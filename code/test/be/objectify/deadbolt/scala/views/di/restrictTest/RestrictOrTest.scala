/*
 * Copyright 2012-2016 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package be.objectify.deadbolt.scala.views.di.restrictTest

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.{anyOf => anyOfRoles, allOf => allOfRoles, allOfGroup}
import be.objectify.deadbolt.scala._
import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.testhelpers.SecurityRole
import be.objectify.deadbolt.scala.views.di.AbstractViewTest
import be.objectify.deadbolt.scala.views.html.di.restrictOr
import be.objectify.deadbolt.scala.views.html.di.restrictTest.restrictOrContent
import play.api.test.{FakeRequest, Helpers, WithApplication}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class RestrictOrTest extends AbstractViewTest {

  val admin: Option[Subject] = Some(user(roles = List(SecurityRole("admin"))))
  val watchdog: Option[Subject] = Some(user(roles = List(SecurityRole("watchdog"))))
  val adminWatchdog: Option[Subject] = Some(user(roles = List(SecurityRole("admin"), SecurityRole("watchdog"))))
  val regularUser: Option[Subject] = Some(user(roles = List(SecurityRole("user"))))
  val noRoles: Option[Subject] = Some(user())

   "when protected by a single role, the view" should {
     "hide constrained content and show fallback content when subject is not present" in new WithApplication(testApp(handler())) {
       val html = constraint(handler())(allOfGroup("foo"))(new AuthenticatedRequest(FakeRequest(), None))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject does not have necessary role" in new WithApplication(testApp(handler(subject = regularUser))) {
       val html = constraint(handler(subject = regularUser))(allOfGroup("admin"))(new AuthenticatedRequest(FakeRequest(), regularUser))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject does not have any roles" in new WithApplication(testApp(handler(subject = noRoles))) {
       val html = constraint(handler(subject = noRoles))(allOfGroup("admin"))(new AuthenticatedRequest(FakeRequest(), noRoles))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject has other roles" in new WithApplication(testApp(handler(subject = regularUser))) {
       val html = constraint(handler(subject = regularUser))(allOfGroup("admin"))(new AuthenticatedRequest(FakeRequest(), regularUser))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject has necessary role" in new WithApplication(testApp(handler(subject = admin))) {
       val html = constraint(handler(subject = admin))(allOfGroup("admin"))(new AuthenticatedRequest(FakeRequest(), admin))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

   "when protected by two ANDed roles, the view" should {
     "hide constrained content and show fallback content when subject is not present" in new WithApplication(testApp(handler())) {
       val html = constraint(handler())(allOfGroup("admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), None))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject does not have necessary role" in new WithApplication(testApp(handler(subject = regularUser))) {
       val html = constraint(handler(subject = regularUser))(allOfGroup("admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), regularUser))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject does not have any roles" in new WithApplication(testApp(handler(subject = noRoles))) {
       val html = constraint(handler(subject = noRoles))(allOfGroup("admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), noRoles))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject has both roles" in new WithApplication(testApp(handler(subject = adminWatchdog))) {
       val html = constraint(handler(subject = adminWatchdog))(allOfGroup("admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), adminWatchdog))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

   "when protected by two ORed roles, the view" should {
     "hide constrained content and show fallback content when subject is not present" in new WithApplication(testApp(handler())) {
       val html = constraint(handler())(anyOfRoles(allOfRoles("admin"), allOfRoles("watchdog")))(new AuthenticatedRequest(FakeRequest(), None))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject does not have either of the necessary roles" in new WithApplication(testApp(handler(subject = regularUser))) {
       val html = constraint(handler(subject = regularUser))(anyOfRoles(allOfRoles("admin"), allOfRoles("watchdog")))(new AuthenticatedRequest(FakeRequest(), regularUser))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject does not have any roles" in new WithApplication(testApp(handler(subject = noRoles))) {
       val html = constraint(handler(subject = noRoles))(anyOfRoles(allOfRoles("admin"), allOfRoles("watchdog")))(new AuthenticatedRequest(FakeRequest(), noRoles))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject has both roles" in new WithApplication(testApp(handler(subject = adminWatchdog))) {
       val html = constraint(handler(subject = adminWatchdog)).apply(anyOfRoles(allOfRoles("admin"), allOfRoles("watchdog")))(new AuthenticatedRequest(FakeRequest(), adminWatchdog))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject has one of the roles" in new WithApplication(testApp(handler(subject = admin))) {
       val html = constraint(handler(subject = admin))(anyOfRoles(allOfRoles("admin"), allOfRoles("watchdog")))(new AuthenticatedRequest(FakeRequest(), admin))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject has another one of the roles" in new WithApplication(testApp(handler(subject = watchdog))) {
       val html = constraint(handler(subject = watchdog))(anyOfRoles(allOfRoles("admin"), allOfRoles("watchdog")))(new AuthenticatedRequest(FakeRequest(), watchdog))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

   "when a single role is present and negated, the view" should {
     "hide constrained content and show fallback content when subject is not present" in new WithApplication(testApp(handler())) {
       val html = constraint(handler())(allOfGroup("!foo"))(new AuthenticatedRequest(FakeRequest(), None))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject has the negated role" in new WithApplication(testApp(handler(subject = admin))) {
       val html = constraint(handler(subject = admin))(allOfGroup("!admin"))(new AuthenticatedRequest(FakeRequest(), admin))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject does not have the negated role" in new WithApplication(testApp(handler(subject = regularUser))) {
       val html = constraint(handler(subject = regularUser))(allOfGroup("!admin"))(new AuthenticatedRequest(FakeRequest(), regularUser))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

   "when ANDed roles contain a negated role, the view" should {
     "show constrained content and hide fallback content when subject is not present" in new WithApplication(testApp(handler())) {
       val html = constraint(handler())(allOfGroup("!admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), None))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "hide constrained content and show fallback content when subject has the negated role" in new WithApplication(testApp(handler(subject = adminWatchdog))) {
       val html = constraint(handler(subject = adminWatchdog))(allOfGroup("!admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), adminWatchdog))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must not contain("This is protected by the constraint.")
       content must contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }

     "show constrained content and hide fallback content when subject has the non-negated role but does not have the negated role" in new WithApplication(testApp(handler(subject = watchdog))) {
       val html = constraint(handler(subject = watchdog))(allOfGroup("!admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), watchdog))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

  def constraint(handler: DeadboltHandler): restrictOrContent = new restrictOrContent(new restrictOr(viewSupport(), handlerCache(handler)))
}
