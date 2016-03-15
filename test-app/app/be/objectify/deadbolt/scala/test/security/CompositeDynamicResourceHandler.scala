package be.objectify.deadbolt.scala.test.security

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class CompositeDynamicResourceHandler(delegates: Map[String, DynamicResourceHandler]) extends DynamicResourceHandler {

  val logger: Logger = Logger(this.getClass)

  override def checkPermission[A](permissionValue: String,
                                  meta: Option[Any],
                                  deadboltHandler: DeadboltHandler,
                                  request: AuthenticatedRequest[A]): Future[Boolean] = {
    deadboltHandler.getSubject(request)
    .map {
      case Some(subject) =>
        subject.permissions.exists(p => p.value.contains("zombie"))
      case None => false
    }
  }

  override def isAllowed[A](name: String,
                            meta: Option[Any],
                            deadboltHandler: DeadboltHandler,
                            request: AuthenticatedRequest[A]): Future[Boolean] =
    delegates.get(name) match {
      case Some(handler) => handler.isAllowed(name,
                                               meta,
                                               deadboltHandler,
                                               request)
      case None =>
        logger.error(s"No DynamicResourceHandler found for key [$name], denying access")
        Future(false)
    }
}
