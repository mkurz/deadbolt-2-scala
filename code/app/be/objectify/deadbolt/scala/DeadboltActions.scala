/*
 * Copyright 2012-2016 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.scala

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.composite.Constraint
import be.objectify.deadbolt.scala.models.PatternType
import play.api.mvc._

import scala.concurrent.Future

/**
  * Controller-level authorisations for Scala controllers.
  *
  * @author Steve Chaloner
  */
@Singleton
class DeadboltActions @Inject()(analyzer: StaticConstraintAnalyzer,
                                handlers: HandlerCache,
                                ecProvider: ExecutionContextProvider,
                                logic: ConstraintLogic) extends Results with BodyParsers {

  val ec = ecProvider.get()

  /**
    * Restrict access to an action to users that have all the specified roles.
    *
    * @param roleNames the constraints
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @return
    */
  def Restrict[A](roleNames: RoleGroup,
                  handler: DeadboltHandler)
                 (bodyParser: BodyParser[A])
                 (block: AuthenticatedRequest[A] => Future[Result]): Action[A] = {
    Restrict[A](List(roleNames),
                handler)(bodyParser)(block)
  }

  /**
    * Restrict access to an action to users that have all the specified roles within a given group.
    * Each group is checked in turn.
    *
    * @param roleGroups the constraints
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @param block the action functionality
    * @return
    */
  def Restrict[A](roleGroups: RoleGroups,
                  handler: DeadboltHandler = handlers())
                 (bodyParser: BodyParser[A] = parse.anyContent)
                 (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
    execute(handler,
            bodyParser,
            authRequest =>
              logic.restrict(authRequest,
                             handler,
                             roleGroups,
                             (ar: AuthenticatedRequest[A]) => {
                               handler.onAuthSuccess(authRequest, "restrict", ConstraintPoint.CONTROLLER)
                               block(ar)
                             },
                             (ar: AuthenticatedRequest[A]) => handler.onAuthFailure(ar)))


  /**
    * Apply a dynamic constraint to a controller action.
    *
    * @param name the name of the dynamic constraint
    * @param meta additional information
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @param block the action functionality
    * @return the action to take
    */
  def Dynamic[A](name: String,
                 meta: Option[Any] = None,
                 handler: DeadboltHandler = handlers())
                (bodyParser: BodyParser[A] = parse.anyContent)
                (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
    execute(handler,
            bodyParser,
            authRequest => logic.dynamic(authRequest,
                                         handler,
                                         name,
                                         meta,
                                         (ar: AuthenticatedRequest[A]) => {
                                           handler.onAuthSuccess(authRequest, "dynamic", ConstraintPoint.CONTROLLER)
                                           block(ar)
                                         },
                                         (ar: AuthenticatedRequest[A]) => handler.onAuthFailure(ar)))

  /**
    *
    * @param value the value of the pattern, e.g. a regex
    * @param patternType the type of the pattern
    * @param handler the handler to use for constraint testing
    * @param invert if true, invert the constraint, i.e. deny access if the pattern matches
    * @param bodyParser a body parser
    * @param block the action functionality
    * @return the action to take
    */
  def Pattern[A](value: String,
                 patternType: PatternType = PatternType.EQUALITY,
                 meta: Option[Any] = None,
                 handler: DeadboltHandler = handlers(),
                 invert: Boolean = false)
                (bodyParser: BodyParser[A] = parse.anyContent)
                (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
    execute(handler,
            bodyParser,
            authRequest => logic.pattern(authRequest,
                                         handler,
                                         value,
                                         patternType,
                                         meta,
                                         invert,
                                         (ar: AuthenticatedRequest[A]) => {
                                           handler.onAuthSuccess(authRequest, "pattern", ConstraintPoint.CONTROLLER)
                                           block(ar)
                                         },
                                         (ar: AuthenticatedRequest[A]) => handler.onAuthFailure(ar)))

  /**
    * Allows access to the action if there is a subject present.
    *
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @param block the action functionality
    * @return the action to take
    */
  def SubjectPresent[A](handler: DeadboltHandler = handlers())
                       (bodyParser: BodyParser[A] = parse.anyContent)
                       (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
    execute(handler,
            bodyParser,
            authRequest => logic.subjectPresent(authRequest,
                                                handler,
                                                (ar: AuthenticatedRequest[A]) => {
                                                  handler.onAuthSuccess(authRequest, "subjectPresent", ConstraintPoint.CONTROLLER)
                                                  block(ar)
                                                },
                                                (ar: AuthenticatedRequest[A]) => handler.onAuthFailure(ar)))

  /**
    * Denies access to the action if there is a subject present.
    *
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @return the action to take
    */
  def SubjectNotPresent[A](handler: DeadboltHandler = handlers())
                          (bodyParser: BodyParser[A] = parse.anyContent)
                          (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
    execute(handler,
            bodyParser,
            authRequest => logic.subjectPresent(authRequest,
                                                handler,
                                                (ar: AuthenticatedRequest[A]) => handler.onAuthFailure(ar),
                                                (ar: AuthenticatedRequest[A]) => {
                                                  handler.onAuthSuccess(authRequest, "subjectNotPresent", ConstraintPoint.CONTROLLER)
                                                  block(ar)
                                                }))

  /**
    * Allows access if the composite constraint resolves to true..
    *
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @return the action to take
    */
  def Composite[A](handler: DeadboltHandler = handlers(),
                   constraint: Constraint[A])
                  (bodyParser: BodyParser[A] = parse.anyContent)
                  (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
    execute(handler,
            bodyParser,
            authRequest => constraint(authRequest,
                                      handler)
                           .flatMap(passed =>
                                      if (passed) {
                                        handler.onAuthSuccess(authRequest, "composite", ConstraintPoint.CONTROLLER)
                                        block(authRequest)
                                      }
                                      else handler.onAuthFailure(authRequest)
                           )(ec))

  def WithAuthRequest[A](handler: DeadboltHandler = handlers())
                        (bodyParser: BodyParser[A] = parse.anyContent)
                        (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
    SubjectActionBuilder(None).async(bodyParser) { authRequest =>
      handler.getSubject(authRequest).flatMap{ maybeSubject =>
        block(new AuthenticatedRequest(authRequest, maybeSubject))
      }(ec)
    }

  /**
    *
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @param block the function to call if beforeAuthCheck does not return a result
    * @return the action to take
    */
  def execute[A](handler: DeadboltHandler,
                 bodyParser: BodyParser[A],
                 block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
    SubjectActionBuilder(None).async(bodyParser) { authRequest =>
      handler.beforeAuthCheck(authRequest).flatMap {
                                                     case Some(result) => Future.successful(result)
                                                     case None => block(authRequest)
                                                   }(ec)
                                                 }
}
