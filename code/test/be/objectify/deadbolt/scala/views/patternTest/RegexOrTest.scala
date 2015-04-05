package be.objectify.deadbolt.scala.views.patternTest

import be.objectify.deadbolt.core.PatternType
import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.testhelpers.{SecurityPermission, User}
import be.objectify.deadbolt.scala.views.html.patternTest.patternOrContent
import be.objectify.deadbolt.scala.{DeadboltHandler, DynamicResourceHandler}
import play.api.mvc.{Results, Request, Result}
import play.api.test.{Helpers, FakeRequest, PlaySpecification, WithApplication}
import play.libs.Scala

import scala.concurrent.Future

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class RegexOrTest extends PlaySpecification {

   "when the subject has a permission that matches the pattern, the view" should {
     "show constrained content and hide fallback content" in new WithApplication {
       val html = patternOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = Some(new DynamicResourceHandler() {
           override def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]): Boolean = true
           override def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]): Boolean = true
         })
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List.empty), Scala.asJava(List(new SecurityPermission("killer.undead.zombie")))))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, value = "killer.undead.*", patternType = PatternType.REGEX)(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

  "when the subject has no permissions that match the pattern, the view" should {
    "hide constrained content and show fallback content" in new WithApplication {
      val html = patternOrContent(new DeadboltHandler() {
        override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
        override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = Some(new DynamicResourceHandler() {
          override def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]): Boolean = true
          override def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]): Boolean = true
        })
        override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List.empty), Scala.asJava(List(new SecurityPermission("killer.foo.bar")))))
        override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
      }, value = "killer.undead.*", patternType = PatternType.EQUALITY)(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is default content in case the constraint denies access to the protected content.")
      content must contain("This is after the constraint.")
    }
  }
 }
