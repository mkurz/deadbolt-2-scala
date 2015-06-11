package be.objectify.deadbolt.scala.views.restrictTest

import be.objectify.deadbolt.scala.testhelpers.SecurityRole
import be.objectify.deadbolt.scala.views.AbstractViewTest
import be.objectify.deadbolt.scala.views.html.restrictTest.restrictContent
import play.api.test.{FakeRequest, Helpers, WithApplication}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class RestrictTest extends AbstractViewTest {

  "when protected by a single role, the view" should {
    "hide constrained content when subject is not present" in new WithApplication(testApp(handler())) {
      val html = restrictContent(List(Array("foo")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "hide constrained content when subject does not have necessary role" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("user"))))))) {
      val html = restrictContent(List(Array("admin")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "hide constrained content when subject does not have any roles" in new WithApplication(testApp(handler(subject = Some(user())))) {
      val html = restrictContent(List(Array("admin")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "hide constrained content when subject has other roles" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("user"))))))) {
      val html = restrictContent(List(Array("admin")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "show constrained content when subject has necessary role" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("admin"))))))) {
      val html = restrictContent(List(Array("admin")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }
  }

  "when protected by two ANDed roles, the view" should {
    "hide constrained content when subject is not present" in new WithApplication(testApp(handler())) {
      val html = restrictContent(List(Array("admin", "watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "hide constrained content when subject does not have necessary role" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("user"))))))) {
      val html = restrictContent(List(Array("admin", "watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "hide constrained content when subject does not have any roles" in new WithApplication(testApp(handler(subject = Some(user())))) {
      val html = restrictContent(List(Array("admin", "watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "show constrained content when subject has both roles" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("admin"), new SecurityRole("watchdog"))))))) {
      val html = restrictContent(List(Array("admin", "watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

  }

  "when protected by two ORed roles, the view" should {

    "hide constrained content when subject is not present" in new WithApplication(testApp(handler())) {
      val html = restrictContent(List(Array("admin"), Array("watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "hide constrained content when subject does not have either of the necessary roles" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("user"))))))) {
      val html = restrictContent(List(Array("admin"), Array("watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "hide constrained content when subject does not have any roles" in new WithApplication(testApp(handler(subject = Some(user())))) {
      val html = restrictContent(List(Array("admin"), Array("watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "show constrained content when subject has both roles" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("admin"), new SecurityRole("watchdog"))))))) {
      val html = restrictContent(List(Array("admin"), Array("watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "show constrained content when subject has one of the roles" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("admin"))))))) {
      val html = restrictContent(List(Array("admin"), Array("watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "show constrained content when subject has another one of the roles" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("watchdog"))))))) {
      val html = restrictContent(List(Array("admin"), Array("watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }
  }

  "when a single role is present and negated, the view" should {
    "hide constrained content when subject is not present" in new WithApplication(testApp(handler())) {
      val html = restrictContent(List(Array("!foo")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "hide constrained content when subject has the negated role" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("admin"))))))) {
      val html = restrictContent(List(Array("!admin")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "show constrained content when subject does not have the negated role" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("user"))))))) {
      val html = restrictContent(List(Array("!admin")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }
  }

  "when ANDed roles contain a negated role, the view" should {
    "show constrained content when subject is not present" in new WithApplication(testApp(handler())) {
      val html = restrictContent(List(Array("!admin", "watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "hide constrained content when subject has the negated role" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("admin"), new SecurityRole("watchdog"))))))) {
      val html = restrictContent(List(Array("!admin", "watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }

    "show constrained content when subject has the non-negated role but does not have the negated role" in new WithApplication(testApp(handler(subject = Some(user(roles = List(new SecurityRole("watchdog"))))))) {
      val html = restrictContent(List(Array("!admin", "watchdog")))(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }
  }
}
