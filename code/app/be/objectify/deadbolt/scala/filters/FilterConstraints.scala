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
package be.objectify.deadbolt.scala.filters

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.composite._
import be.objectify.deadbolt.scala.models.PatternType
import be.objectify.deadbolt.scala._
import play.api.mvc.{AnyContent, RequestHeader, Result}

import scala.concurrent.Future

/**
  * Provides helpers for creating filter-based constraints.
  *
  * @param constraintLogic the common constraint logic used throughout Deadbolt
  * @param ecProvider provides the execution context for futures
  * @author Steve Chaloner (steve@objectify.be)
  * @since 2.5.1
  */
@Singleton
class FilterConstraints @Inject()(constraintLogic: ConstraintLogic,
                                  ecProvider: ExecutionContextProvider) {

  val ec = ecProvider.get()

  val subjectPresent: FilterFunction = new FilterFunction {
    override def apply(requestHeader: RequestHeader, authRequest: AuthenticatedRequest[AnyContent], handler: DeadboltHandler, next: (RequestHeader) => Future[Result]): Future[Result] =
      execute(handler,
               authRequest,
               requestHeader,
               (ar: AuthenticatedRequest[AnyContent], rh: RequestHeader) =>
                 constraintLogic.subjectPresent(ar,
                                                 handler,
                                                 (ar: AuthenticatedRequest[AnyContent]) => next(rh),
                                                 (ar: AuthenticatedRequest[AnyContent]) => handler.onAuthFailure(ar)))
  }

  val subjectNotPresent: FilterFunction = new FilterFunction {
    override def apply(requestHeader: RequestHeader, authRequest: AuthenticatedRequest[AnyContent], handler: DeadboltHandler, next: (RequestHeader) => Future[Result]): Future[Result] =
      execute(handler,
               authRequest,
               requestHeader,
               (ar: AuthenticatedRequest[AnyContent], rh: RequestHeader) =>
                 constraintLogic.subjectPresent(ar,
                                                 handler,
                                                 (ar: AuthenticatedRequest[AnyContent]) => handler.onAuthFailure(ar),
                                                 (ar: AuthenticatedRequest[AnyContent]) => next(rh)))
  }

  def restrict(roleGroups: RoleGroups): FilterFunction = new FilterFunction {
    override def apply(requestHeader: RequestHeader, authRequest: AuthenticatedRequest[AnyContent], handler: DeadboltHandler, next: (RequestHeader) => Future[Result]): Future[Result] =
      execute(handler,
               authRequest,
               requestHeader,
               (ar: AuthenticatedRequest[AnyContent], rh: RequestHeader) =>
                 constraintLogic.restrict(ar,
                                           handler,
                                           roleGroups,
                                           (ar: AuthenticatedRequest[AnyContent]) => next(rh),
                                           (ar: AuthenticatedRequest[AnyContent]) => handler.onAuthFailure(ar)))
  }

  def dynamic(name: String, meta: Option[Any] = None): FilterFunction = new FilterFunction {
    override def apply(requestHeader: RequestHeader, authRequest: AuthenticatedRequest[AnyContent], handler: DeadboltHandler, next: (RequestHeader) => Future[Result]): Future[Result] =
      execute(handler,
               authRequest,
               requestHeader,
               (ar: AuthenticatedRequest[AnyContent], rh: RequestHeader) =>
                 constraintLogic.dynamic(ar,
                                          handler,
                                          name,
                                          meta,
                                          (ar: AuthenticatedRequest[AnyContent]) => next(rh),
                                          (ar: AuthenticatedRequest[AnyContent]) => handler.onAuthFailure(ar)))
  }

  def pattern(value: String, patternType: PatternType, meta: Option[Any] = None, invert: Boolean = false): FilterFunction = new FilterFunction {
    override def apply(requestHeader: RequestHeader, authRequest: AuthenticatedRequest[AnyContent], handler: DeadboltHandler, next: (RequestHeader) => Future[Result]): Future[Result] =
      execute(handler,
               authRequest,
               requestHeader,
               (ar: AuthenticatedRequest[AnyContent], rh: RequestHeader) =>
                 constraintLogic.pattern(ar,
                                          handler,
                                          value,
                                          patternType,
                                          meta,
                                          invert,
                                          (ar: AuthenticatedRequest[AnyContent]) => next(requestHeader),
                                          (ar: AuthenticatedRequest[AnyContent]) => handler.onAuthFailure(ar)))
  }

  def composite(constraint: Constraint[AnyContent]): FilterFunction = new FilterFunction {
    override def apply(requestHeader: RequestHeader, authRequest: AuthenticatedRequest[AnyContent], handler: DeadboltHandler, next: (RequestHeader) => Future[Result]): Future[Result] =
      execute(handler,
               authRequest,
               requestHeader,
               (ar: AuthenticatedRequest[AnyContent], rh: RequestHeader) =>
                 constraint(ar, handler).flatMap(passed =>
                   if (passed) next(requestHeader)
                   else handler.onAuthFailure(authRequest))(ec))
  }

  private def execute(handler: DeadboltHandler,
                      authRequest: AuthenticatedRequest[AnyContent],
                      requestHeader: RequestHeader,
                      block: (AuthenticatedRequest[AnyContent], RequestHeader) => Future[Result]) = handler.beforeAuthCheck(authRequest).flatMap {
    case Some(result) => Future.successful(result)
    case None => block(authRequest, requestHeader)
  }(ec)
}
