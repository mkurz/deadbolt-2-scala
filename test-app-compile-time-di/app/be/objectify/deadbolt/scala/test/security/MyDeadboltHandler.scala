package be.objectify.deadbolt.scala.test.security

import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.test.dao.SubjectDao
import be.objectify.deadbolt.scala.{DeadboltHandler, DynamicResourceHandler}
import play.api.mvc.{Request, Result, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class MyDeadboltHandler(subjectDao: SubjectDao) extends DeadboltHandler {

  val dynamicHandler: Option[DynamicResourceHandler] = Some(new CompositeDynamicResourceHandler(Map("niceName" -> new NiceNameDynamicResourceHandler)))

  override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future(None)

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = Future(dynamicHandler)

  override def getSubject[A](request: Request[A]): Future[Option[Subject]] =
    Future {
      request.headers.get("x-deadbolt-test-user") match {
        case Some(userName) => subjectDao.user(userName)
        case None => None
      }
    }

  override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Unauthorized)
}
