package be.objectify.deadbolt.scala

import play.api.mvc.Request

import scala.concurrent.Future

/**
 *
 * @author Steve Chaloner (steve@objectify.be)
 */

trait DynamicResourceHandler
{
  /**
   * Check the access of the named resource.
   *
   * @param name the resource name
   * @param meta additional information on the resource
   * @param deadboltHandler the current { @link DeadboltHandler}
   * @param request the current request
   * @return true if access to the resource is allowed, otherwise false
   */
  def isAllowed[A](name: String,
                   meta: String,
                   deadboltHandler: DeadboltHandler,
                   request: Request[A]): Future[Boolean]

  /**
   * Invoked when a pattern constraint with a [[be.objectify.deadbolt.core.PatternType#CUSTOM]] type is used.
   *
   * @param permissionValue the permission value
   * @param deadboltHandler the current { @link DeadboltHandler}
   * @param request the current request
   * @return true if access based on the permission is  allowed, otherwise false
   */
  def checkPermission[A](permissionValue: String,
                         deadboltHandler: DeadboltHandler,
                         request: Request[A]): Future[Boolean]
}