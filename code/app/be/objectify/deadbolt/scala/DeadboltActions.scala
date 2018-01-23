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
class DeadboltActions @Inject()(actionBuilders: DeadboltActionBuilders,
                                analyzer: StaticConstraintAnalyzer,
                                handlers: HandlerCache,
                                ecProvider: ExecutionContextProvider,
                                logic: ConstraintLogic,
                                bodyParsers: PlayBodyParsers)
  extends Results {

  private val ec = ecProvider.get()

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
    (bodyParser: BodyParser[A] = bodyParsers.anyContent)
    (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
      actionBuilders.RestrictAction(roleGroups)(handler).async(bodyParser)(block)

  /**
    * Check if the subject has at least one of the permissions defined by [[DeadboltHandler.getPermissionsForRole()]].
    *
    * @param roleName the constraints
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @param block the action functionality
    * @return
    */
  def RoleBasedPermissions[A](roleName: String,
    handler: DeadboltHandler = handlers())
    (bodyParser: BodyParser[A] = bodyParsers.anyContent)
    (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
      actionBuilders.RoleBasedPermissions(roleName)(handler).async(bodyParser)(block)

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
    (bodyParser: BodyParser[A] = bodyParsers.anyContent)
    (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
      actionBuilders.DynamicAction(name, meta)(handler).async(bodyParser)(block)

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
    (bodyParser: BodyParser[A] = bodyParsers.anyContent)
    (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
      actionBuilders.PatternAction(value, patternType, meta, invert).async(bodyParser)(block)

  /**
    * Allows access to the action if there is a subject present.
    *
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @param block the action functionality
    * @return the action to take
    */
  def SubjectPresent[A](handler: DeadboltHandler = handlers())
    (bodyParser: BodyParser[A] = bodyParsers.anyContent)
    (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
      actionBuilders.SubjectPresentAction(handler).async(bodyParser)(block)

  /**
    * Denies access to the action if there is a subject present.
    *
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @return the action to take
    */
  def SubjectNotPresent[A](handler: DeadboltHandler = handlers())
    (bodyParser: BodyParser[A] = bodyParsers.anyContent)
    (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
      actionBuilders.SubjectNotPresentAction(handler).async(bodyParser)(block)

  /**
    * Allows access if the composite constraint resolves to true..
    *
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @return the action to take
    */
  def Composite[A](handler: DeadboltHandler = handlers(),
    constraint: Constraint[A])
    (bodyParser: BodyParser[A] = bodyParsers.anyContent)
    (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
    execute(handler,
      bodyParser,
      authRequest => handler.getSubject(authRequest).flatMap { maybeSubject =>
        val ar = new AuthenticatedRequest(authRequest, maybeSubject)
        constraint(ar,
          handler)
        .flatMap(passed =>
          if (passed) {
            handler.onAuthSuccess(ar, "composite", ConstraintPoint.CONTROLLER)
            block(ar)
          }
          else handler.onAuthFailure(ar))(ec)
      }(ec))

  def WithAuthRequest[A](handler: DeadboltHandler = handlers())
    (bodyParser: BodyParser[A] = bodyParsers.anyContent)
    (block: AuthenticatedRequest[A] => Future[Result]): Action[A] =
      actionBuilders.WithAuthRequestAction(handler).async(bodyParser)(block)

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
    SubjectActionBuilder(None, ec, bodyParsers.anyContent).async(bodyParser) { authRequest =>
      handler.beforeAuthCheck(authRequest).flatMap {
        case Some(result) => Future.successful(result)
        case None => block(authRequest)
      }(ec)
    }
}
