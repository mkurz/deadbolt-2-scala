package be.objectify.deadbolt.scala.views.patternTest

import be.objectify.deadbolt.core.PatternType
import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.testhelpers.{SecurityPermission, User}
import be.objectify.deadbolt.scala.{DynamicResourceHandler, DeadboltHandler}
import be.objectify.deadbolt.scala.views.html.patternTest.patternOrContent
import play.api.mvc.{Results, Result, Request}
import play.api.test.{WithApplication, PlaySpecification, FakeRequest, Helpers}
import play.libs.Scala
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class CustomOrTest extends PlaySpecification {

   "when a custom permission allows it, the view" should {
     "show constrained content and hide fallback content" in new WithApplication {
       Request
       val html = patternOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future(None)
         override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = Future(Some(new DynamicResourceHandler() {
           override def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(true)
           override def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(true) // allow permission
         }))
         override def getSubject[A](request: Request[A]): Future[Option[Subject]] = Future(Some(new User("foo", Scala.asJava(List.empty), Scala.asJava(List(new SecurityPermission("killer.undead.zombie"))))))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, value = "something arbitrary", patternType = PatternType.CUSTOM)(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

  "when a custom permission denies it, the view" should {
    "hide constrained content and show fallback content" in new WithApplication {
      val html = patternOrContent(new DeadboltHandler() {
        override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future(None)
        override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = Future(Some(new DynamicResourceHandler() {
          override def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(true)
          override def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(false) //deny permission
        }))
        override def getSubject[A](request: Request[A]): Future[Option[Subject]] = Future(Some(new User("foo", Scala.asJava(List.empty), Scala.asJava(List(new SecurityPermission("killer.undead.zombie"))))))
        override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
      }, value = "something arbitrary", patternType = PatternType.CUSTOM)(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is default content in case the constraint denies access to the protected content.")
      content must contain("This is after the constraint.")
    }
  }
 }
