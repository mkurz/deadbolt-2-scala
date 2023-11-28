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

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, allOfGroup, allOf => allOfRoles, anyOf => anyOfRoles}
import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.testhelpers.SecurityRole
import be.objectify.deadbolt.scala.views.di.AbstractViewTest
import be.objectify.deadbolt.scala.views.html.di.restrict
import be.objectify.deadbolt.scala.views.html.di.restrictTest.restrictContent
import play.api.test.{FakeRequest, Helpers, WithApplication}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class RestrictTest extends AbstractViewTest {

  val admin: Option[Subject] = Some(user(roles = List(SecurityRole("admin"))))
  val watchdog: Option[Subject] = Some(user(roles = List(SecurityRole("watchdog"))))
  val adminWatchdog: Option[Subject] = Some(user(roles = List(SecurityRole("admin"), SecurityRole("watchdog"))))
  val regularUser: Option[Subject] = Some(user(roles = List(SecurityRole("user"))))
  val noRoles: Option[Subject] = Some(user())


  "when protected by a single role, the view" should {
    "hide constrained content when subject is not present" in new WithApplication(testApp(handler())) {
      override def running() = {
        val html = constraint(handler()).apply(allOfGroup("foo"))(new AuthenticatedRequest(FakeRequest(), None))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "hide constrained content when subject does not have necessary role" in new WithApplication(testApp(handler(subject = regularUser))) {
      override def running() = {
        val html = constraint(handler(subject = regularUser)).apply(allOfGroup("admin"))(new AuthenticatedRequest(FakeRequest(), regularUser))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "hide constrained content when subject does not have any roles" in new WithApplication(testApp(handler(subject = noRoles))) {
      override def running() = {
        val html = constraint(handler(subject = noRoles)).apply(allOfGroup("admin"))(new AuthenticatedRequest(FakeRequest(), noRoles))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "hide constrained content when subject has other roles" in new WithApplication(testApp(handler(subject = regularUser))) {
      override def running() = {
        val html = constraint(handler(subject = regularUser)).apply(allOfGroup("admin"))(new AuthenticatedRequest(FakeRequest(), regularUser))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "show constrained content when subject has necessary role" in new WithApplication(testApp(handler(subject = admin))) {
      override def running() = {
        val html = constraint(handler(subject = admin)).apply(allOfGroup("admin"))(new AuthenticatedRequest(FakeRequest(), admin))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must contain("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }
  }

  "when protected by two ANDed roles, the view" should {
    "hide constrained content when subject is not present" in new WithApplication(testApp(handler())) {
      override def running() = {
        val html = constraint(handler()).apply(allOfGroup("admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), None))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "hide constrained content when subject does not have necessary role" in new WithApplication(testApp(handler(subject = regularUser))) {
      override def running() = {
        val html = constraint(handler(subject = regularUser)).apply(allOfGroup("admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), regularUser))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "hide constrained content when subject does not have any roles" in new WithApplication(testApp(handler(subject = noRoles))) {
      override def running() = {
        val html = constraint(handler(subject = noRoles)).apply(allOfGroup("admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), noRoles))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "show constrained content when subject has both roles" in new WithApplication(testApp(handler(subject = adminWatchdog))) {
      override def running() = {
        val html = constraint(handler(subject = adminWatchdog)).apply(allOfGroup("admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), adminWatchdog))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must contain("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

  }

  "when protected by two ORed roles, the view" should {

    "hide constrained content when subject is not present" in new WithApplication(testApp(handler())) {
      override def running() = {
        val html = constraint(handler()).apply(anyOfRoles(allOfRoles("admin"), allOfRoles("watchdog")))(new AuthenticatedRequest(FakeRequest(), None))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "hide constrained content when subject does not have either of the necessary roles" in new WithApplication(testApp(handler(subject = regularUser))) {
      override def running() = {
        val html = constraint(handler(subject = regularUser)).apply(anyOfRoles(allOfRoles("admin"), allOfRoles("watchdog")))(new AuthenticatedRequest(FakeRequest(), regularUser))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "hide constrained content when subject does not have any roles" in new WithApplication(testApp(handler(subject = noRoles))) {
      override def running() = {
        val html = constraint(handler(subject = noRoles)).apply(anyOfRoles(allOfRoles("admin"), allOfRoles("watchdog")))(new AuthenticatedRequest(FakeRequest(), noRoles))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "show constrained content when subject has both roles" in new WithApplication(testApp(handler(subject = adminWatchdog))) {
      override def running() = {
        val html = constraint(handler(subject = adminWatchdog)).apply(anyOfRoles(allOfRoles("admin"), allOfRoles("watchdog")))(new AuthenticatedRequest(FakeRequest(), adminWatchdog))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must contain("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "show constrained content when subject has one of the roles" in new WithApplication(testApp(handler(subject = admin))) {
      override def running() = {
        val html = constraint(handler(subject = admin)).apply(anyOfRoles(allOfRoles("admin"), allOfRoles("watchdog")))(new AuthenticatedRequest(FakeRequest(), admin))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must contain("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "show constrained content when subject has another one of the roles" in new WithApplication(testApp(handler(subject = watchdog))) {
      override def running() = {
        val html = constraint(handler(subject = watchdog)).apply(anyOfRoles(allOfRoles("admin"), allOfRoles("watchdog")))(new AuthenticatedRequest(FakeRequest(), watchdog))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must contain("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }
  }

  "when a single role is present and negated, the view" should {
    "hide constrained content when subject is not present" in new WithApplication(testApp(handler())) {
      override def running() = {
        val html = constraint(handler()).apply(allOfGroup("!foo"))(new AuthenticatedRequest(FakeRequest(), None))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "hide constrained content when subject has the negated role" in new WithApplication(testApp(handler(subject = admin))) {
      override def running() = {
        val html = constraint(handler(subject = admin)).apply(allOfGroup("!admin"))(new AuthenticatedRequest(FakeRequest(), admin))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "show constrained content when subject does not have the negated role" in new WithApplication(testApp(handler(subject = regularUser))) {
      override def running() = {
        val html = constraint(handler(subject = regularUser)).apply(allOfGroup("!admin"))(new AuthenticatedRequest(FakeRequest(), regularUser))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must contain("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }
  }

  "when ANDed roles contain a negated role, the view" should {
    "show constrained content when subject is not present" in new WithApplication(testApp(handler())) {
      override def running() = {
        val html = constraint(handler()).apply(allOfGroup("!admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), None))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "hide constrained content when subject has the negated role" in new WithApplication(testApp(handler(subject = adminWatchdog))) {
      override def running() = {
        val html = constraint(handler(subject = adminWatchdog)).apply(allOfGroup("!admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), adminWatchdog))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }

    "show constrained content when subject has the non-negated role but does not have the negated role" in new WithApplication(testApp(handler(subject = watchdog))) {
      override def running() = {
        val html = constraint(handler(subject = watchdog)).apply(allOfGroup("!admin", "watchdog"))(new AuthenticatedRequest(FakeRequest(), watchdog))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must contain("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }
  }

  def constraint(handler: DeadboltHandler) = new restrictContent(new restrict(viewSupport(), handlerCache(handler)))
}
