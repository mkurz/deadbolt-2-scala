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

import javax.inject.{Singleton, Inject}

import be.objectify.deadbolt.scala.models.{PatternType, Subject}

import scala.concurrent.Future

@Singleton
class ConstraintLogic @Inject()(analyzer: StaticConstraintAnalyzer,
                                ecProvider: ExecutionContextProvider) {

  val ec = ecProvider.get()

  def restrict[A, B](authRequest: AuthenticatedRequest[A],
                     handler: DeadboltHandler,
                     roleGroups: RoleGroups,
                     pass: AuthenticatedRequest[A] => Future[B],
                     fail: AuthenticatedRequest[A] => Future[B]): Future[B] = {
    def check(subject: Option[Subject], current: RoleGroup, remaining: RoleGroups): Boolean = {
      if (analyzer.hasAllRoles(subject, current)) true
      else if (remaining.isEmpty) false
      else check(subject, remaining.head, remaining.tail)
    }

    if (roleGroups.isEmpty) fail(authRequest)
    else {
      handler.getSubject(authRequest).flatMap((subjectOption: Option[Subject]) =>
        subjectOption match {
          case Some(subject) =>
            val withSubject = AuthenticatedRequest(authRequest, subjectOption)
            if (check(subjectOption, roleGroups.head, roleGroups.tail)) pass(withSubject)
            else fail(withSubject)
          case _ => fail(authRequest)
        })(ec)
    }
  }

  def dynamic[A, B](authRequest: AuthenticatedRequest[A],
                    handler: DeadboltHandler,
                    name: String,
                    meta: Option[Any] = None,
                    pass: AuthenticatedRequest[A] => Future[B],
                    fail: AuthenticatedRequest[A] => Future[B]): Future[B] =
    handler.getDynamicResourceHandler(authRequest).flatMap((drhOption: Option[DynamicResourceHandler]) => {
      drhOption match {
        case Some(dynamicHandler) =>
          handler.getSubject(authRequest).flatMap(subjectOption => {
            val maybeWithSubject = AuthenticatedRequest(authRequest, subjectOption)
            dynamicHandler.isAllowed(name, meta, handler, maybeWithSubject).flatMap((allowed: Boolean) => allowed match {
              case true => pass(maybeWithSubject)
              case false => fail(maybeWithSubject)
            })(ec)
          })(ec)
        case None =>
          throw new RuntimeException("A dynamic resource is specified but no dynamic resource handler is provided")
      }
    })(ec)

  def pattern[A, B](authRequest: AuthenticatedRequest[A],
                    handler: DeadboltHandler,
                    value: String,
                    patternType: PatternType,
                    meta: Option[Any] = None,
                    invert: Boolean,
                    pass: AuthenticatedRequest[A] => Future[B],
                    fail: AuthenticatedRequest[A] => Future[B]): Future[B] =
    handler.getSubject(authRequest).flatMap((subjectOption: Option[Subject]) => subjectOption match {
      case None => fail(authRequest)
      case Some(subject) =>
        val withSubject = AuthenticatedRequest(authRequest, subjectOption)
        patternType match {
          case PatternType.EQUALITY =>
            val equal: Boolean = analyzer.checkPatternEquality(subjectOption, Option(value))
            if (if (invert) !equal else equal) pass(withSubject)
            else fail(withSubject)
          case PatternType.REGEX =>
            val patternMatch: Boolean = analyzer.checkRegexPattern(subjectOption, Option(value))
            if (if (invert) !patternMatch else patternMatch) pass(withSubject)
            else fail(withSubject)
          case PatternType.CUSTOM =>
            handler.getDynamicResourceHandler(authRequest).flatMap((drhOption: Option[DynamicResourceHandler]) => {
              drhOption match {
                case Some(dynamicHandler) =>
                  dynamicHandler.checkPermission(value, meta, handler, authRequest).flatMap((allowed: Boolean) => {
                    (if (invert) !allowed else allowed) match {
                      case true => pass(withSubject)
                      case false => fail(withSubject)
                    }
                  })(ec)
                case None =>
                  throw new RuntimeException("A custom pattern is specified but no dynamic resource handler is provided")
              }
            })(ec)
        }
    })(ec)

  def subjectPresent[A, B](authRequest: AuthenticatedRequest[A],
                           handler: DeadboltHandler,
                           present: AuthenticatedRequest[A] => Future[B],
                           notPresent: AuthenticatedRequest[A] => Future[B]): Future[B] = {
    val subject1: Future[Option[Subject]] = handler.getSubject(authRequest)
    subject1.flatMap((subjectOption: Option[Subject]) => subjectOption match {
      case Some(subject) => present(AuthenticatedRequest(authRequest, subjectOption))
      case None => notPresent(AuthenticatedRequest(authRequest, subjectOption))
    })(ec)
  }
}
