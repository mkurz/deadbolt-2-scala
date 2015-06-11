package be.objectify.deadbolt.scala.views.subjectTest

import be.objectify.deadbolt.scala.views.AbstractViewTest
import be.objectify.deadbolt.scala.views.html.subjectTest.subjectPresentContent
import play.api.test.{FakeRequest, Helpers, WithApplication}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class SubjectPresentTest extends AbstractViewTest {

  "show constrained content when subject is present" in new WithApplication(testApp(handler(subject = Some(user())))) {
    val html = subjectPresentContent(FakeRequest())

    private val content: String = Helpers.contentAsString(html)
    content must contain("This is before the constraint.")
    content must contain("This is protected by the constraint.")
    content must contain("This is after the constraint.")
  }

  "hide constrained content when subject is not present" in new WithApplication(testApp(handler())) {
    val html = subjectPresentContent(FakeRequest())

    private val content: String = Helpers.contentAsString(html)
    content must contain("This is before the constraint.")
    content must not contain("This is protected by the constraint.")
    content must contain("This is after the constraint.")
  }
}
