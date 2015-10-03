package be.objectify.deadbolt.scala.test.security

import be.objectify.deadbolt.scala.{DeadboltHandler, DynamicResourceHandler}
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Dedicated handler to look for people with the same name as my wife.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
class NiceNameDynamicResourceHandler extends DynamicResourceHandler {

  // the composite handler doesn't delegate checkPermission, so we can just return false here
  override def checkPermission[A](permissionValue: String,
                                  deadboltHandler: DeadboltHandler,
                                  request: Request[A]): Future[Boolean] = Future(false)

  override def isAllowed[A](name: String,
                            meta: String,
                            deadboltHandler: DeadboltHandler,
                            request: Request[A]): Future[Boolean] =
    deadboltHandler.getSubject(request).map {
      case Some(subject) => subject.getIdentifier.contains("greet")
      case None => false
    }
}
