package be.objectify.deadbolt.scala.views.dynamicTest

import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.{DeadboltModule, DeadboltHandler, DynamicResourceHandler}
import be.objectify.deadbolt.scala.views.html.dynamicTest.dynamicContent
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Results, Request, Result}
import play.api.test.{Helpers, FakeRequest, PlaySpecification, WithApplication}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class DynamicTest extends PlaySpecification {

   "when allowed by the dynamic handler, the view" should {
     "show constrained content" in new WithApplication(new GuiceApplicationBuilder()
                                                         .bindings(new DeadboltModule())
                                                         .build()) {
       val html = dynamicContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future(None)
         override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = Future(Some(new DynamicResourceHandler() {
           override def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(true) // allow
           override def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(false)
         }))
         override def getSubject[A](request: Request[A]): Future[Option[Subject]] = Future(None)
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, name = "the name of this constraint", meta = "some additional info")(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must contain("This is after the constraint.")
     }
   }

   "when denied by the dynamic handler, the view" should {
     "hide constrained content" in new WithApplication(new GuiceApplicationBuilder()
                                                         .bindings(new DeadboltModule())
                                                         .build()) {
       val html = dynamicContent(handler = new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future(None)
         override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = Future(Some(new DynamicResourceHandler() {
           override def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(false) // deny
           override def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(false)
         }))
         override def getSubject[A](request: Request[A]): Future[Option[Subject]] = Future(None)
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, name = "the name of this constraint", meta = "some additional info")(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is after the constraint.")
     }
   }
 }
