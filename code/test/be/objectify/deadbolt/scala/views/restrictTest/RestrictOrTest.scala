package be.objectify.deadbolt.scala.views.restrictTest

import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.testhelpers.{SecurityRole, User}
import be.objectify.deadbolt.scala.views.html.restrictTest.restrictOrContent
import be.objectify.deadbolt.scala.{DeadboltHandler, DynamicResourceHandler}
import play.api.mvc.{Request, Result, Results}
import play.api.test.{FakeRequest, Helpers, PlaySpecification, WithApplication}
import play.libs.Scala

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class RestrictOrTest extends PlaySpecification {

   "when protected by a single role, the view" should {
     "hide constrained content and show fallback content when subject is not present" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = None
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("foo")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject does not have necessary role" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("user"))), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject does not have any roles" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List.empty), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject has other roles" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List()), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hode fallback content when subject has necessary role" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("admin"))), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

   "when protected by two ANDed roles, the view" should {

     "hide constrained content and show fallback content when subject is not present" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = None
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin", "watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject does not have necessary role" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("user"))), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin", "watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject does not have any roles" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List.empty), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin", "watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject has both roles" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("admin"), new SecurityRole("watchdog"))), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin", "watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

   }

   "when protected by two ORed roles, the view" should {

     "hide constrained content and show fallback content when subject is not present" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = None
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin"), Array("watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject does not have either of the necessary roles" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("user"))), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin"), Array("watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject does not have any roles" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List.empty), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin"), Array("watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject has both roles" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("admin"), new SecurityRole("watchdog"))), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin"), Array("watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject has one of the roles" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("admin"))), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin"), Array("watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject has another one of the roles" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("watchdog"))), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("admin"), Array("watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

   "when a single role is present and negated, the view" should {
     "hide constrained content and show fallback content when subject is not present" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = None
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("!foo")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject has the negated role" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("admin"))), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("!admin")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject does not have the negated role" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("user"))), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("!admin")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

   "when ANDed roles contain a negated role, the view" should {
     "show constrained content and hide fallback content when subject is not present" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = None
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("!admin", "watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject has the negated role" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("admin"), new SecurityRole("watchdog"))), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("!admin", "watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject has the non-negated role but does not have the negated role" in new WithApplication {
       val html = restrictOrContent(new DeadboltHandler() {
         override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
         override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
         override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("watchdog"))), Scala.asJava(List.empty)))
         override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
       }, List(Array("!admin", "watchdog")))(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }
 }
