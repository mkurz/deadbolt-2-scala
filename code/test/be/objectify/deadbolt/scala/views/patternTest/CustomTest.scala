package be.objectify.deadbolt.scala.views.patternTest

import be.objectify.deadbolt.core.PatternType
import be.objectify.deadbolt.scala.testhelpers.SecurityPermission
import be.objectify.deadbolt.scala.views.{drh, AbstractViewTest}
import be.objectify.deadbolt.scala.views.html.patternTest.patternContent
import play.api.test.{FakeRequest, Helpers, WithApplication}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class CustomTest extends AbstractViewTest {

   "when a custom permission allows it, the view" should {
     "show constrained content" in new WithApplication(testApp(handler(subject = Some(user(permissions = List(SecurityPermission("killer.undead.zombie")))), drh = Some(drh(allowed = true, check = true))))) {
       val html = patternContent(value = "something arbitrary", patternType = PatternType.CUSTOM)(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must contain("This is after the constraint.")
     }
   }

  "when a custom permission denies it, the view" should {
    "hide constrained content" in new WithApplication(testApp(handler(subject = Some(user(permissions = List(SecurityPermission("killer.undead.zombie")))),  drh = Some(drh(allowed = true, check = false))))) {
      val html = patternContent(value = "something arbitrary", patternType = PatternType.CUSTOM)(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }
  }
 }
