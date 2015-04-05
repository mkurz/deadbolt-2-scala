package be.objectify.deadbolt.scala.views.subjectTest

import play.libs.Scala
import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.testhelpers.{SecurityRole, SecurityPermission, User}
import be.objectify.deadbolt.scala.{DynamicResourceHandler, DeadboltHandler}
import play.api.mvc.{Results, Result, Request}
import play.api.test.{FakeRequest, Helpers, PlaySpecification, WithApplication}
import be.objectify.deadbolt.scala.views.html.subjectTest.{subjectPresentContent, subjectPresentOrContent}

import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class SubjectPresentTest extends PlaySpecification {

  "show constrained content when subject is present" in new WithApplication {
    val html = subjectPresentContent(new DeadboltHandler() {
      override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
      override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
      override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("hurdy"))), Scala.asJava(List(new SecurityPermission("gurdy")))))
      override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
    })(FakeRequest())

    private val content: String = Helpers.contentAsString(html)
    content must contain("This is before the constraint.")
    content must contain("This is protected by the constraint.")
    content must contain("This is after the constraint.")
  }

  "hide constrained content when subject is not present" in new WithApplication {
    val html = subjectPresentContent(new DeadboltHandler() {
      override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
      override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
      override def getSubject[A](request: Request[A]): Option[Subject] = None
      override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
    })(FakeRequest())

    private val content: String = Helpers.contentAsString(html)
    content must contain("This is before the constraint.")
    content must not contain("This is protected by the constraint.")
    content must contain("This is after the constraint.")
  }

  "show constrained content and hide fallback content when subject is present" in new WithApplication {
    val html = subjectPresentOrContent(new DeadboltHandler() {
      override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
      override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
      override def getSubject[A](request: Request[A]): Option[Subject] = Some(new User("foo", Scala.asJava(List(new SecurityRole("hurdy"))), Scala.asJava(List(new SecurityPermission("gurdy")))))
      override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
    })(FakeRequest())

    private val content: String = Helpers.contentAsString(html)
    content must contain("This is before the constraint.")
    content must contain("This is protected by the constraint.")
    content must not contain("This is default content in case the constraint denies access to the protected content.")
    content must contain("This is after the constraint.")
  }

  "hide constrained content and show fallback content when subject is not present" in new WithApplication {
    val html = subjectPresentOrContent(new DeadboltHandler() {
      override def beforeAuthCheck[A](request: Request[A]): Option[Future[Result]] = None
      override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
      override def getSubject[A](request: Request[A]): Option[Subject] = None
      override def onAuthFailure[A](request: Request[A]): Future[Result] = Future(Results.Forbidden)
    })(FakeRequest())

    private val content: String = Helpers.contentAsString(html)
    content must contain("This is before the constraint.")
    content must not contain("This is protected by the constraint.")
    content must contain("This is default content in case the constraint denies access to the protected content.")
    content must contain("This is after the constraint.")
  }
}
