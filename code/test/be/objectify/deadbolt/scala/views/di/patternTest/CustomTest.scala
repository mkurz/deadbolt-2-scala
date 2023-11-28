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
package be.objectify.deadbolt.scala.views.di.patternTest

import be.objectify.deadbolt.scala.models.{PatternType, Subject}
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}
import be.objectify.deadbolt.scala.testhelpers.SecurityPermission
import be.objectify.deadbolt.scala.views.di.{AbstractViewTest, drh}
import be.objectify.deadbolt.scala.views.html.di.pattern
import be.objectify.deadbolt.scala.views.html.di.patternTest.patternContent
import play.api.test.{FakeRequest, Helpers, WithApplication}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class CustomTest extends AbstractViewTest {

  val user: Option[Subject] = Some(user(permissions = List(SecurityPermission("killer.undead.zombie"))))
  val drhAllow: Option[DynamicResourceHandler] = Some(drh(allowed = true, check = true))
  val drhDeny: Option[DynamicResourceHandler] = Some(drh(allowed = true, check = false))

  "when a custom permission allows it, the view" should {
     "show constrained content" in new WithApplication(testApp(handler(subject = user, drh = drhAllow))) {
       override def running() = {
         val html = constraint(handler(subject = user, drh = drhAllow)).apply(value = "something arbitrary", patternType = PatternType.CUSTOM)(new AuthenticatedRequest(FakeRequest(), user))

         val content: String = Helpers.contentAsString(html)
         content must contain("This is before the constraint.")
         content must contain("This is protected by the constraint.")
         content must contain("This is after the constraint.")
       }
     }
   }

  "when a custom permission denies it, the view" should {
    "hide constrained content" in new WithApplication(testApp(handler(subject = user,  drh = drhDeny))) {
      override def running() = {
        val html = constraint(handler(subject = user, drh = drhDeny)).apply(value = "something arbitrary", patternType = PatternType.CUSTOM)(new AuthenticatedRequest(FakeRequest(), user))

        val content: String = Helpers.contentAsString(html)
        content must contain("This is before the constraint.")
        content must not contain ("This is protected by the constraint.")
        content must contain("This is after the constraint.")
      }
    }
  }

  def constraint(handler: DeadboltHandler) = new patternContent(new pattern(viewSupport(), handlerCache(handler)))
 }
