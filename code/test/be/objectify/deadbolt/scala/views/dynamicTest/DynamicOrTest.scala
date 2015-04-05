package be.objectify.deadbolt.scala.views.dynamicTest

import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.views.html.dynamicTest.dynamicOrContent
import be.objectify.deadbolt.scala.{DeadboltHandler, DynamicResourceHandler}
import play.api.mvc.{Results, Request, Result}
import play.api.test.{FakeRequest, Helpers, PlaySpecification, WithApplication}

import scala.concurrent.Future

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class DynamicOrTest extends PlaySpecification {

   "when allowed by the dynamic handler, the view" should {
     "show constrained content and hide fallback content" in new WithApplication {
       val html = dynamicOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = Some(new DynamicResourceHandler() {
           override def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]): Boolean = true
           override def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]): Boolean = false
         })
         override def getSubject[A](request: Request[A]): Option[Subject] = None
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, name = "the name of this constraint", meta = "some additional info")(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

   "when denied by the dynamic handler, the view" should {
     "hide constrained content and show fallback content" in new WithApplication {
       val html = dynamicOrContent(handler = new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = Some(new DynamicResourceHandler() {
           override def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]): Boolean = false
           override def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]): Boolean = false
         })
         override def getSubject[A](request: Request[A]): Option[Subject] = None
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, name = "the name of this constraint", meta = "some additional info")(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }
 }
