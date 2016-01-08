/*
 * Copyright 2012-2015 Steve Chaloner
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
package be.objectify.deadbolt.scala.views.patternTest

import be.objectify.deadbolt.core.PatternType
import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.testhelpers.{SecurityPermission, User}
import be.objectify.deadbolt.scala.views.{drh, AbstractViewTest}
import be.objectify.deadbolt.scala.views.html.patternTest.patternOrContent
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}
import play.api.mvc.Request
import play.api.test.{FakeRequest, Helpers, WithApplication}
import play.libs.Scala

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class EqualityOrTest extends AbstractViewTest {

  val userZombie: Option[Subject] = Some(user(permissions = List(SecurityPermission("killer.undead.zombie"))))
  val userVampire: Option[Subject] = Some(user(permissions = List(SecurityPermission("killer.undead.vampire"))))
  val drHandler: Option[DynamicResourceHandler] = Some(drh(allowed = true, check = true))


  "when the subject has a permission that is equal to the pattern, the view" should {
     "show constrained content and hide fallback content" in new WithApplication(testApp(handler(subject = userZombie, drh = drHandler))) {
       val html = patternOrContent(value = "killer.undead.zombie", patternType = PatternType.EQUALITY)(AuthenticatedRequest(FakeRequest(), userZombie))

       private val content: String = Helpers.contentAsString(html)
       content must contain("This is before the constraint.")
       content must contain("This is protected by the constraint.")
       content must not contain("This is default content in case the constraint denies access to the protected content.")
       content must contain("This is after the constraint.")
     }
   }

  "when the subject has no permissions that are equal to the pattern, the view" should {
    "hide constrained content and show fallback content" in new WithApplication(testApp(handler(subject = userVampire,  drh = drHandler))) {
      val html = patternOrContent(value = "killer.undead.zombie", patternType = PatternType.EQUALITY)(AuthenticatedRequest(FakeRequest(), userVampire))

      private val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain("This is protected by the constraint.")
      content must contain("This is default content in case the constraint denies access to the protected content.")
      content must contain("This is after the constraint.")
    }
  }
 }
