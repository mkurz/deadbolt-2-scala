package be.objectify.deadbolt.scala.views.dynamicTest

import be.objectify.deadbolt.scala.{DynamicResourceHandler, AuthenticatedRequest}
import be.objectify.deadbolt.scala.views.{drh, AbstractViewTest}
import be.objectify.deadbolt.scala.views.html.dynamicTest.dynamicContent
import play.api.test.{FakeRequest, Helpers, WithApplication}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class DynamicTest extends AbstractViewTest {

  val drhAllow: Option[DynamicResourceHandler] = Some(drh(allowed = true, check = false))
  val drhDeny: Option[DynamicResourceHandler] = Some(drh(allowed = false, check = false))

  "when allowed by the dynamic handler, the view" should {
     "show constrained content" in new WithApplication(testApp(handler(drh = drhAllow))) {
       val html = dynamicContent(name = "the name of this constraint", meta = "some additional info")(AuthenticatedRequest(FakeRequest(), None))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must contain("This is after the constraint.")
     }
   }

   "when denied by the dynamic handler, the view" should {
     "hide constrained content" in new WithApplication(testApp(handler(drh = drhDeny))) {
       val html = dynamicContent(name = "the name of this constraint", meta = "some additional info")(AuthenticatedRequest(FakeRequest(), None))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is after the constraint.")
     }
   }
 }
