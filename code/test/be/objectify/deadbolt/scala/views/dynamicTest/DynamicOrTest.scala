package be.objectify.deadbolt.scala.views.dynamicTest

import be.objectify.deadbolt.scala.views.{drh, AbstractViewTest}
import be.objectify.deadbolt.scala.views.html.dynamicTest.dynamicOrContent
import play.api.test.{FakeRequest, Helpers, WithApplication}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class DynamicOrTest extends AbstractViewTest {

  "When using the DynamicOr constraint" should {
    "when allowed by the dynamic handler, the view" should {
      "show constrained content and hide fallback content" in new WithApplication(testApp(handler(drh = Some(drh(allowed = true, check = false))))) {
        val html = dynamicOrContent(name = "the name of this constraint", meta = "some additional info")(FakeRequest())

        private val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must contain("This is protected by the constraint.")
        content must not contain("This is default content in case the constraint denies access to the protected content.")
        content must contain("This is after the constraint.")
      }
    }

    "when denied by the dynamic handler, the view" should {
      "hide constrained content and show fallback content" in new WithApplication(testApp(handler(drh = Some(drh(allowed = false, check = false))))) {
        val html = dynamicOrContent(name = "the name of this constraint", meta = "some additional info")(FakeRequest())

        private val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain("This is protected by the constraint.")
        content must contain("This is default content in case the constraint denies access to the protected content.")
        content must contain("This is after the constraint.")
      }
    }
  }
 }
