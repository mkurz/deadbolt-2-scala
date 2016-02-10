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

import be.objectify.deadbolt.core.PatternType
import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.cache.HandlerCache
import play.api.mvc._

import scala.concurrent.Future

/**
  * Controller-level authorisations for Scala controllers.
  *
  * @author Steve Chaloner
  */
@Singleton
class DeadboltActions @Inject()(analyzer: ScalaAnalyzer,
                                handlers: HandlerCache,
                                ecProvider: ExecutionContextProvider) extends Results with BodyParsers {

  val ec = ecProvider.get()

  /**
    * Restrict access to an action to users that have all the specified roles.
    *
    * @param roleNames the constraints
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @return
    */
  def Restrict[A](roleNames: Array[String],
                  handler: DeadboltHandler)
                 (bodyParser: BodyParser[A])
                 (block: AuthenticatedRequest[A] => Future[Result]): Action[A] = {
    Restrict[A](List(roleNames),
                handler)(bodyParser)(block)
  }

  /**
    * Restrict access to an action to users that have all the specified roles within a given group.  Each group, which is
    * an array of strings, is checked in turn.
    *
    * @param roleGroups the constraints
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @param block the action functionality
    * @return
    */
  def Restrict[A](roleGroups: List[Array[String]],
                  handler: DeadboltHandler = handlers())
                 (bodyParser: BodyParser[A] = parse.anyContent)
                 (block: AuthenticatedRequest[A] => Future[Result]): Action[A] = {
    SubjectActionBuilder(None).async(bodyParser) { authRequest =>

      def check(subject: Option[Subject], current: Array[String], remaining: List[Array[String]]): Boolean = {
        if (analyzer.checkRole(subject, current)) true
        else if (remaining.isEmpty) false
        else check(subject, remaining.head, remaining.tail)
      }

      handler.beforeAuthCheck(authRequest).flatMap((beforeAuthOption: Option[Result]) => {
        beforeAuthOption match {
          case Some(result) => Future(result)(ec)
          case _ =>
            if (roleGroups.isEmpty) handler.onAuthFailure(authRequest)
            else {
              handler.getSubject(authRequest).flatMap((subjectOption: Option[Subject]) =>
                                                        subjectOption match {
                                                          case Some(subject) =>
                                                            val withSubject = AuthenticatedRequest(authRequest, subjectOption)
                                                            if (check(subjectOption, roleGroups.head, roleGroups.tail)) block(withSubject)
                                                            else handler.onAuthFailure(withSubject)
                                                          case _ => handler.onAuthFailure(authRequest)
                                                        })(ec)
            }
        }
      })(ec)}
  }

  /**
    *
    * @param name the name of the dynamic constraint
    * @param meta additional information
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @param block the action functionality
    * @return
    */
  def Dynamic[A](name: String,
                 meta: String = "",
                 handler: DeadboltHandler = handlers())
                (bodyParser: BodyParser[A] = parse.anyContent)
                (block: AuthenticatedRequest[A] => Future[Result]): Action[A] = {
    SubjectActionBuilder(None).async(bodyParser) { authRequest =>
      handler.beforeAuthCheck(authRequest).flatMap((beforeAuthOption: Option[Result]) => {
        beforeAuthOption match {
          case Some(result) => Future(result)(ec)
          case None =>
            handler.getDynamicResourceHandler(authRequest).flatMap((drhOption: Option[DynamicResourceHandler]) => {
              drhOption match {
                case Some(dynamicHandler) =>
                  handler.getSubject(authRequest).flatMap(subjectOption => {
                    val maybeWithSubject = AuthenticatedRequest(authRequest, subjectOption)
                    dynamicHandler.isAllowed(name, meta, handler, maybeWithSubject).flatMap((allowed: Boolean) => allowed match {
                      case true => block(maybeWithSubject)
                      case false => handler.onAuthFailure(maybeWithSubject)
                    })(ec)
                  })(ec)
                case None =>
                  throw new RuntimeException("A dynamic resource is specified but no dynamic resource handler is provided")
              }
            })(ec)
        }
      })(ec)
                                                 }
  }

  /**
    *
    * @param value the value of the pattern, e.g. a regex
    * @param patternType the type of the pattern
    * @param handler the handler to use for constraint testing
    * @param invert if true, invert the constraint, i.e. deny access if the pattern matches
    * @param bodyParser a body parser
    * @param block the action functionality
    * @return
    */
  def Pattern[A](value: String,
                 patternType: PatternType = PatternType.EQUALITY,
                 handler: DeadboltHandler = handlers(),
                 invert: Boolean = false)
                (bodyParser: BodyParser[A] = parse.anyContent)
                (block: AuthenticatedRequest[A] => Future[Result]): Action[A] = {

    SubjectActionBuilder(None).async(bodyParser) { authRequest =>
      handler.beforeAuthCheck(authRequest).flatMap((beforeAuthOption: Option[Result]) => {
        beforeAuthOption match {
          case Some(result) => Future(result)(ec)
          case None =>
            handler.getSubject(authRequest).flatMap((subjectOption: Option[Subject]) => subjectOption match {
              case None => handler.onAuthFailure(authRequest)
              case Some(subject) =>
                val withSubject = AuthenticatedRequest(authRequest, subjectOption)
                patternType match {
                  case PatternType.EQUALITY =>
                    val equal: Boolean = analyzer.checkPatternEquality(subjectOption, Option(value))
                    if (if (invert) !equal else equal) block(withSubject)
                    else handler.onAuthFailure(withSubject)
                  case PatternType.REGEX =>
                    val patternMatch: Boolean = analyzer.checkRegexPattern(subjectOption, value)
                    if (if (invert) !patternMatch else patternMatch) block(withSubject)
                    else handler.onAuthFailure(withSubject)
                  case PatternType.CUSTOM =>
                    handler.getDynamicResourceHandler(authRequest).flatMap((drhOption: Option[DynamicResourceHandler]) => {
                      drhOption match {
                        case Some(dynamicHandler) =>
                          dynamicHandler.checkPermission(value, handler, authRequest).flatMap((allowed: Boolean) => {
                            (if (invert) !allowed else allowed) match {
                              case true => block(withSubject)
                              case false => handler.onAuthFailure(withSubject)
                            }
                          })(ec)
                        case None =>
                          throw new RuntimeException("A custom pattern is specified but no dynamic resource handler is provided")
                      }
                    })(ec)
                }
            })(ec)
        }
      })(ec)
                                                 }
  }

  /**
    * Allows access to the action if there is a subject present.
    *
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @param block the action functionality
    * @return
    */
  def SubjectPresent[A](handler: DeadboltHandler = handlers())
                       (bodyParser: BodyParser[A] = parse.anyContent)
                       (block: AuthenticatedRequest[A] => Future[Result]): Action[A] = {
    SubjectActionBuilder(None).async(bodyParser) { authRequest =>
      handler.beforeAuthCheck(authRequest).flatMap((beforeAuthOption: Option[Result]) =>
                                                     beforeAuthOption match {
                                                       case Some(result) => Future(result)(ec)
                                                       case None =>
                                                         handler.getSubject(authRequest).flatMap((subjectOption: Option[Subject]) => subjectOption match {
                                                           case Some(subject) => block(AuthenticatedRequest(authRequest, subjectOption))
                                                           case None => handler.onAuthFailure(AuthenticatedRequest(authRequest, subjectOption))
                                                         })(ec)
                                                     })(ec)
                                                 }
  }

  /**
    * Denies access to the action if there is a subject present.
    *
    * @param handler the handler to use for constraint testing
    * @param bodyParser a body parser
    * @tparam A
    * @return
    */
  def SubjectNotPresent[A](handler: DeadboltHandler = handlers())
                          (bodyParser: BodyParser[A] = parse.anyContent)
                          (block: AuthenticatedRequest[A] => Future[Result]): Action[A] = {
    SubjectActionBuilder(None).async(bodyParser) { authRequest =>
      handler.beforeAuthCheck(authRequest).flatMap((beforeAuthOption: Option[Result]) =>
                                                     beforeAuthOption match {
                                                       case Some(result) => Future(result)(ec)
                                                       case None =>
                                                         handler.getSubject(authRequest).flatMap((subjectOption: Option[Subject]) => subjectOption match {
                                                           case Some(subject) => handler.onAuthFailure(AuthenticatedRequest(authRequest, subjectOption))
                                                           case None => block(AuthenticatedRequest(authRequest, subjectOption))
                                                         })(ec)
                                                     })(ec)
                                                 }
  }
}