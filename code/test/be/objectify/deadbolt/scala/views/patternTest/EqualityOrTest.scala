package be.objectify.deadbolt.scala.views.patternTest

import be.objectify.deadbolt.core.PatternType
import be.objectify.deadbolt.scala.testhelpers.{SecurityPermission, User}
import be.objectify.deadbolt.scala.views.{drh, AbstractViewTest}
import be.objectify.deadbolt.scala.views.html.patternTest.patternOrContent
import be.objectify.deadbolt.scala.{DeadboltHandler, DynamicResourceHandler}
import play.api.mvc.Request
import play.api.test.{FakeRequest, Helpers, WithApplication}
import play.libs.Scala

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class EqualityOrTest extends AbstractViewTest {

   "when the subject has a permission that is equal to the pattern, the view" should {
     "show constrained content and hide fallback content" in new WithApplication(testApp(handler(subject = Some(user(permissions = List(new SecurityPermission("killer.undead.zombie")))), drh = Some(drh(true, true))))) {
       val html = patternOrContent(value = "killer.undead.zombie", patternType = PatternType.EQUALITY)(FakeRequest())

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

  "when the subject has no permissions that are equal to the pattern, the view" should {
    val user = new User("foo", Scala.asJava(List.empty), Scala.asJava(List(new SecurityPermission("killer.undead.vampire"))))
    val drh = new DynamicResourceHandler() {
      override def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(true)
      override def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]): Future[Boolean] = Future(true)
    }
    "hide constrained content and show fallback content" in new WithApplication(testApp(handler(subject = Some(user),  drh = Some(drh)))) {
      val html = patternOrContent(value = "killer.undead.zombie", patternType = PatternType.EQUALITY)(FakeRequest())

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is default content in case the constraint denies access to the protected content.")
      content must contain("This is after the constraint.")
    }
  }
 }
