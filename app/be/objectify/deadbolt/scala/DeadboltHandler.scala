package be.objectify.deadbolt.scala

import play.api.mvc.{Request, Result}
import be.objectify.deadbolt.core.models.Subject

/**
 *
 * @author Steve Chaloner (steve@objectify.be)
 */

trait DeadboltHandler
{
  /**
   * Gets the current subject e.g. the current user.
   *
   * @return an option containing the current subject
   */
  def getSubject[A](request: Request[A]): Option[Subject]

  /**
   * Invoked when an access failure is detected on <i>controllerClassName</i>.
   *
   * @return the action
   */
  def onAccessFailure[A](request: Request[A]): Result

  /**
   * Gets the handler used for dealing with resources restricted to specific users/groups.
   *
   * @return an option containing the handler for restricted resources
   */
  def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler]
}