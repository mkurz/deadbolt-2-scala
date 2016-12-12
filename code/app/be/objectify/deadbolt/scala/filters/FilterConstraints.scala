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

  private val ec = ecProvider.get()

  val subjectPresent: FilterFunction = new FilterFunction {
    override def apply(requestHeader: RequestHeader, authRequest: AuthenticatedRequest[AnyContent], handler: DeadboltHandler, next: (RequestHeader) => Future[Result]): Future[Result] =
      execute(handler,
               authRequest,
               requestHeader,
               (ar: AuthenticatedRequest[AnyContent], rh: RequestHeader) =>
                 constraintLogic.subjectPresent(ar,
                                                 handler,
                                                 (ar2: AuthenticatedRequest[AnyContent]) => {
                                                   handler.onAuthSuccess(ar2, "subjectPresent", ConstraintPoint.FILTER)
                                                   next(rh)
                                                 },
                                                 (ar2: AuthenticatedRequest[AnyContent]) => handler.onAuthFailure(ar2)))
  }

  val subjectNotPresent: FilterFunction = new FilterFunction {
    override def apply(requestHeader: RequestHeader, authRequest: AuthenticatedRequest[AnyContent], handler: DeadboltHandler, next: (RequestHeader) => Future[Result]): Future[Result] =
      execute(handler,
               authRequest,
               requestHeader,
               (ar: AuthenticatedRequest[AnyContent], rh: RequestHeader) =>
                 constraintLogic.subjectPresent(ar,
                                                 handler,
                                                 (ar2: AuthenticatedRequest[AnyContent]) => handler.onAuthFailure(ar2),
                                                 (ar2: AuthenticatedRequest[AnyContent]) => {
                                                   handler.onAuthSuccess(ar2, "subjectNotPresent", ConstraintPoint.FILTER)
                                                   next(rh)
                                                 }))
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
                                           (ar2: AuthenticatedRequest[AnyContent]) => {
                                             handler.onAuthSuccess(ar2, "restrict", ConstraintPoint.FILTER)
                                             next(rh)
                                           },
                                           (ar2: AuthenticatedRequest[AnyContent]) => handler.onAuthFailure(ar2)))
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
                                          (ar2: AuthenticatedRequest[AnyContent]) => {
                                            handler.onAuthSuccess(ar2, "dynamic", ConstraintPoint.FILTER)
                                            next(rh)
                                          },
                                          (ar2: AuthenticatedRequest[AnyContent]) => handler.onAuthFailure(ar2)))
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
                                          (ar2: AuthenticatedRequest[AnyContent]) => {
                                            handler.onAuthSuccess(ar2, "pattern", ConstraintPoint.FILTER)
                                            next(rh)
                                          },
                                          (ar2: AuthenticatedRequest[AnyContent]) => handler.onAuthFailure(ar2)))
  }

  def roleBasedPermissions(name: String): FilterFunction = new FilterFunction {
    override def apply(requestHeader: RequestHeader, authRequest: AuthenticatedRequest[AnyContent], handler: DeadboltHandler, next: (RequestHeader) => Future[Result]): Future[Result] =
      execute(handler,
        authRequest,
        requestHeader,
        (ar: AuthenticatedRequest[AnyContent], rh: RequestHeader) =>
          constraintLogic.roleBasedPermissions(ar,
            handler,
            name,
            (ar2: AuthenticatedRequest[AnyContent]) => {
              handler.onAuthSuccess(ar2, "roleBasedPermissions", ConstraintPoint.FILTER)
              next(rh)
            },
            (ar2: AuthenticatedRequest[AnyContent]) => handler.onAuthFailure(ar2)
          )
      )
  }

  def composite(constraint: Constraint[AnyContent]): FilterFunction = new FilterFunction {
    override def apply(requestHeader: RequestHeader, authRequest: AuthenticatedRequest[AnyContent], handler: DeadboltHandler, next: (RequestHeader) => Future[Result]): Future[Result] =
      handler.getSubject(authRequest).flatMap { maybeSubject =>
        val ar = new AuthenticatedRequest(authRequest, maybeSubject)
        execute(handler,
          ar,
          requestHeader,
          (ar2: AuthenticatedRequest[AnyContent], rh: RequestHeader) =>
            constraint(ar2, handler).flatMap(passed =>
              if (passed) {
                handler.onAuthSuccess(ar2, "composite", ConstraintPoint.FILTER)
                next(rh)
              }
              else handler.onAuthFailure(ar2))(ec))
      }(ec)
  }

  private def execute(handler: DeadboltHandler,
                      authRequest: AuthenticatedRequest[AnyContent],
                      requestHeader: RequestHeader,
                      block: (AuthenticatedRequest[AnyContent], RequestHeader) => Future[Result]) = handler.beforeAuthCheck(authRequest).flatMap {
    case Some(result) => Future.successful(result)
    case None => block(authRequest, requestHeader)
  }(ec)
}
