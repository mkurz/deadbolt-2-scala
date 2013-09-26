package be.objectify.deadbolt.scala

import play.api.mvc.{SimpleResult, Request, Result}
import be.objectify.deadbolt.core.models.Subject
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
  def beforeAuthCheck[A](request: Request[A]): Option[Future[SimpleResult]]

  /**
   * Gets the current subject e.g. the current user.
   *
   * @return an option containing the current subject
   */
  def getSubject[A](request: Request[A]): Option[Subject]

  /**
   * Invoked when an authorisation failure is detected for the request.
   *
   * @return the action
   */
  def onAuthFailure[A](request: Request[A]): Future[SimpleResult]

  /**
   * Gets the handler used for dealing with resources restricted to specific users/groups.
   *
   * @return an option containing the handler for restricted resources
   */
  def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler]
}