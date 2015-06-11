package be.objectify.deadbolt.scala.views.patternTest

import be.objectify.deadbolt.core.PatternType
import be.objectify.deadbolt.scala.testhelpers.SecurityPermission
import be.objectify.deadbolt.scala.views.{drh, AbstractViewTest}
import be.objectify.deadbolt.scala.views.html.patternTest.patternOrContent
import play.api.test.{FakeRequest, Helpers, WithApplication}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class RegexOrTest extends AbstractViewTest {

   "when the subject has a permission that matches the pattern, the view" should {
     "show constrained content and hide fallback content" in new WithApplication(testApp(handler(subject = Some(user(permissions = List(new SecurityPermission("killer.undead.zombie")))), drh = Some(drh(allowed = true, check = true))))) {
       val html = patternOrContent(value = "killer.undead.*", patternType = PatternType.REGEX)(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

  "when the subject has no permissions that match the pattern, the view" should {
    "hide constrained content and show fallback content" in new WithApplication(testApp(handler(subject = Some(user(permissions = List(new SecurityPermission("killer.foo.bar")))), drh = Some(drh(allowed = true, check = true))))) {
      val html = patternOrContent(value = "killer.undead.*", patternType = PatternType.EQUALITY)(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is default content in case the constraint denies access to the protected content.")
      content must contain("This is after the constraint.")
    }
  }
 }
