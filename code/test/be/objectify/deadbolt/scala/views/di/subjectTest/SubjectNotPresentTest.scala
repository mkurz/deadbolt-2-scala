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
package be.objectify.deadbolt.scala.views.di.subjectTest

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler}
import be.objectify.deadbolt.scala.testhelpers.User
import be.objectify.deadbolt.scala.views.di.AbstractViewTest
import be.objectify.deadbolt.scala.views.html.di.subjectNotPresent
import be.objectify.deadbolt.scala.views.html.di.subjectTest.subjectNotPresentContent
import play.api.test.{FakeRequest, Helpers, WithApplication}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class SubjectNotPresentTest extends AbstractViewTest {

  "show constrained content when subject is not present" in new WithApplication(testApp(handler())) {
    override def running() = {
      val html = constraint(handler()).render(new AuthenticatedRequest(FakeRequest(), None))

      val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must contain("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }
  }

  "hide constrained content when subject is present" in new WithApplication(testApp(handler(subject = Some(user())))) {
    override def running() = {
      val user = new User("foo", List.empty, List.empty)
      val html = constraint(handler(subject = Some(user))).render(new AuthenticatedRequest(FakeRequest(), Some(user)))

      val content: String = Helpers.contentAsString(html)
      content must contain("This is before the constraint.")
      content must not contain ("This is protected by the constraint.")
      content must contain("This is after the constraint.")
    }
  }

  def constraint(handler: DeadboltHandler) = new subjectNotPresentContent(new subjectNotPresent(viewSupport(), handlerCache(handler)))
}
