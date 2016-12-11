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

import be.objectify.deadbolt.scala.ConstraintPoint.ConstraintPoint
import be.objectify.deadbolt.scala.models.{Permission, Subject}
import play.api.mvc.{Request, Result}

import scala.concurrent.Future

/**
 *
 * @author Steve Chaloner (steve@objectify.be)
 */

trait DeadboltHandler {

  /**
   * Invoked prior to a constraint's test.  If Option.None is returned, the constraint is applied. If
   * the option contains a result, the constraint will not be applied and the wrapped action will not
   * be invoked.
   *
   * @return an option possible containing a Result.
   */
  def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]]

  /**
   * Gets the current subject e.g. the current user.
   *
   * @return a future for an option containing the current subject
   */
  def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]]

  /**
   * Invoked when an authorization failure is detected for the request.
   *
   * @param request the authenticated request
   * @return the action
   */
  def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result]

  /**
    * Invoked when authorization succeeds.
    *
    * @param request the authenticated request
    * @param constraintType the type of constraint, e.g. dynamic, restrict, etc
    * @param constraintPoint the point at which the constraint was applied
    * @since 2.5.1
    */
  def onAuthSuccess[A](request: AuthenticatedRequest[A], constraintType: String, constraintPoint: ConstraintPoint): Unit = {}

  /**
   * Gets the handler used for dealing with resources restricted to specific users/groups.
   *
   * @return an option containing the handler for restricted resources
   */
  def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]]
  
  /**
   * Gets the canonical name of the handler.  Defaults to the class name.
   *
   * @return whatever the implementor considers the canonical name of the handler to be
   */
  def handlerName = getClass.getName

  /**
    * Get the permissions associated with a role.
    *
    * @param roleName the role the permissions are associated with
    * @return a non-null list containing the permissions associated with the role
    */
  def getPermissionsForRole(roleName: String): Future[List[String]] = Future.successful { Nil }
}