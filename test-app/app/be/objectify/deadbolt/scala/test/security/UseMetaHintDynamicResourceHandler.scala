package be.objectify.deadbolt.scala.test.security

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}

import scala.concurrent.Future

/**
 * Uses the meta parameter to determine if access is allowed.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
class UseMetaHintDynamicResourceHandler extends DynamicResourceHandler {

  // the composite handler doesn't delegate checkPermission, so we can just return false here
  override def checkPermission[A](permissionValue: String,
                                  meta: Option[Any],
                                  deadboltHandler: DeadboltHandler,
                                  request: AuthenticatedRequest[A]): Future[Boolean] = Future.successful{false}

  override def isAllowed[A](name: String,
                            meta: Option[Any],
                            deadboltHandler: DeadboltHandler,
                            request: AuthenticatedRequest[A]): Future[Boolean] =
  meta match {
    case metaInf: Some[String] => Future.successful{meta.exists(m => "passDyn".equals(m))}
    case _ => Future.successful{false}
  }
}
