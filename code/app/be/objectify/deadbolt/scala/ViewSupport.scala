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

import be.objectify.deadbolt.scala.models.PatternType
import play.api.{Configuration, Logger}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

/**
  *
  * @author Steve Chaloner (steve@objectify.be)
  */
@Singleton
class ViewSupport @Inject()(config: Configuration,
                            listenerProvider: TemplateFailureListenerProvider,
                            logic: ConstraintLogic) {

  val logger: Logger = Logger("deadbolt.template")

  val timeout: Long = config.getLong("deadbolt.scala.view-timeout").getOrElse(1000L)
  logger.info(s"Default timeout period for blocking views is [$timeout]ms")

  val defaultTimeout: () => Long = () => timeout

  val listener = listenerProvider.get()

  private def allow[A](request: AuthenticatedRequest[A]): Future[Boolean] = Future.successful(true)

  private def deny[A](request: AuthenticatedRequest[A]): Future[Boolean] = Future.successful(false)

  /**
    * Returns true if [[DeadboltHandler.getSubject()]] results in Some
    *
    * @param deadboltHandler application hook
    * @return true if the view can be accessed, otherwise false
    */
  def subjectPresent[A](deadboltHandler: DeadboltHandler,
                        timeoutInMillis: Long,
                        request: AuthenticatedRequest[A]): Boolean =
    tryToComplete(logic.subjectPresent(request,
                                        deadboltHandler,
                                        (ar: AuthenticatedRequest[A]) => allow(ar),
                                        (ar: AuthenticatedRequest[A]) => deny(ar)),
                   timeoutInMillis)


  /**
    * Returns true if [[DeadboltHandler.getSubject()]] results in None
    *
    * @param deadboltHandler application hook
    * @return true if the view can be accessed, otherwise false
    */
  def subjectNotPresent[A](deadboltHandler: DeadboltHandler,
                           timeoutInMillis: Long,
                           request: AuthenticatedRequest[A]): Boolean =
    tryToComplete(logic.subjectPresent(request,
                                        deadboltHandler,
                                        (ar: AuthenticatedRequest[A]) => deny(ar),
                                        (ar: AuthenticatedRequest[A]) => allow(ar)),
                   timeoutInMillis)

  /**
    * Used for restrict tags in the template.
    *
    * @param roles a List of String arrays.  Within an array, the roles are ANDed.  The arrays in the list are OR'd, so
    *              the first positive hit will allow access.
    * @param deadboltHandler application hook
    * @return true if the view can be accessed, otherwise false
    */
  def restrict[A](roles: RoleGroups,
                  deadboltHandler: DeadboltHandler,
                  timeoutInMillis: Long,
                  request: AuthenticatedRequest[A]): Boolean =
    tryToComplete(logic.restrict(request,
                                  deadboltHandler,
                                  roles,
                                  (ar: AuthenticatedRequest[A]) => allow(ar),
                                  (ar: AuthenticatedRequest[A]) => deny(ar)),
                   timeoutInMillis)

  /**
    * Used for dynamic tags in the template.
    *
    * @param name the name of the resource
    * @param meta meta information on the resource
    * @return true if the view can be accessed, otherwise false
    */
  def dynamic[A](name: String,
                 meta: Option[Any] = None,
                 deadboltHandler: DeadboltHandler,
                 timeoutInMillis: Long,
                 request: AuthenticatedRequest[A]): Boolean =
    tryToComplete(logic.dynamic(request,
                                 deadboltHandler,
                                 name,
                                 meta,
                                 (ar: AuthenticatedRequest[A]) => allow(ar),
                                 (ar: AuthenticatedRequest[A]) => deny(ar)),
                   timeoutInMillis)

  /**
    *
    * @param value the value of the pattern, e.g. the regex
    * @param meta meta information on the resource
    * @param patternType the type of pattern
    * @param deadboltHandler the handler to use for this request
    * @param request the request
    * @return
    */
  def pattern[A](value: String,
                 patternType: PatternType,
                 meta: Option[Any] = None,
                 invert: Boolean = false,
                 deadboltHandler: DeadboltHandler,
                 timeoutInMillis: Long,
                 request: AuthenticatedRequest[A]): Boolean =
    tryToComplete(logic.pattern(request,
                                 deadboltHandler,
                                 value,
                                 patternType,
                                 meta,
                                 invert,
                                 (ar: AuthenticatedRequest[A]) => allow(ar),
                                 (ar: AuthenticatedRequest[A]) => deny(ar)),
                   timeoutInMillis)

  /**
    * Attempts to complete the future within the given number of milliseconds.
    *
    * @param future the future to complete
    * @param timeoutInMillis the number of milliseconds to wait for a result
    * @return false if the future times out, otherwise the result of the future
    */
  private def tryToComplete(future: Future[Boolean], timeoutInMillis: Long): Boolean =
    Try(Await.result(future, timeoutInMillis milliseconds)) match {
      case Success(allowed) => allowed
      case Failure(ex) =>
        logger.error("Error when checking view constraint", ex)
        listener.failure(s"Error when checking view constraint: [${ex.getMessage}]",
                          timeout)
        false
    }
}