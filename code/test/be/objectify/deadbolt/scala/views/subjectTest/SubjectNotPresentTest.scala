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
package be.objectify.deadbolt.scala.views.subjectTest

import be.objectify.deadbolt.scala.testhelpers.User
import be.objectify.deadbolt.scala.views.AbstractViewTest
import be.objectify.deadbolt.scala.views.html.subjectTest.subjectNotPresentContent
import play.api.test.{FakeRequest, Helpers, WithApplication}
import play.libs.Scala

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class SubjectNotPresentTest extends AbstractViewTest {

  "show constrained content when subject is not present" in new WithApplication(testApp(handler())) {
    val html = subjectNotPresentContent(FakeRequest())

    private val content: String = Helpers.contentAsString(html)
    content must contain("This is before the constraint.")
    content must contain("This is protected by the constraint.")
    content must contain("This is after the constraint.")
  }

  "hide constrained content when subject is present" in new WithApplication(testApp(handler(subject = Some(user())))) {
    val user = new User("foo", Scala.asJava(List.empty), Scala.asJava(List.empty))
    val html = subjectNotPresentContent(FakeRequest())

    private val content: String = Helpers.contentAsString(html)
    content must contain("This is before the constraint.")
    content must not contain("This is protected by the constraint.")
    content must contain("This is after the constraint.")
  }
}
