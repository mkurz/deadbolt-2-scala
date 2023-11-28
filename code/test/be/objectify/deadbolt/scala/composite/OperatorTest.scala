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
package be.objectify.deadbolt.scala.composite

import be.objectify.deadbolt.scala.composite.Operators._
import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.testhelpers.User
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import play.api.mvc.Request
import play.api.test.PlaySpecification

import scala.concurrent.Future

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object OperatorTest extends PlaySpecification {

  val ec = scala.concurrent.ExecutionContext.Implicits.global
  val subject = Some(User())
  val req = request(subject)
  val dh: DeadboltHandler = handler(subject)

  "&&" should {
    "return false when" >> {
      "the first constraint returns false" >> {
        val c3 = composite(&&(ec),
                            c1Result = false,
                            c2Result = true)
        await(c3(req, dh)) should beFalse
      }
      "the second constraint returns false" >> {
        val c3 = composite(&&(ec),
                            c1Result = true,
                            c2Result = false)
        await(c3(req, dh)) should beFalse
      }
      "both constraints returns false" >> {
        val c3 = composite(&&(ec),
                            c1Result = false,
                            c2Result = false)
        await(c3(req, dh)) should beFalse
      }
    }
    "return true when" >> {
      "both constraints return true" >> {
        val c3 = composite(&&(ec),
                            c1Result = true,
                            c2Result = true)
        val await1: Boolean = await(c3(req, dh))
        await1 should beTrue
      }
    }
  }

  "||" should {
    "return true when" >> {
      "the first constraint returns true" >> {
        val c3 = composite(||(ec),
                            c1Result = true,
                            c2Result = false)
        await(c3(req, dh)) should beTrue
      }
      "the second constraint returns true" >> {
        val c3 = composite(||(ec),
                            c1Result = false,
                            c2Result = true)
        await(c3(req, dh)) should beTrue
      }
      "both constraints returns true" >> {
        val c3 = composite(||(ec),
                            c1Result = true,
                            c2Result = true)
        await(c3(req, dh)) should beTrue
      }
    }
    "return false when" >> {
      "both constraints return false" >> {
        val c3 = composite(||(ec),
                            c1Result = false,
                            c2Result = false)
        await(c3(req, dh)) should beFalse
      }
    }
  }

  private def composite[A](op: Operator[A], c1Result: Boolean, c2Result: Boolean) = {
    val c1 = mock(classOf[Constraint[A]])
    when(c1(req, dh)).thenReturn(Future.successful(c1Result))
    val c2 = mock(classOf[Constraint[A]])
    when(c2(req, dh)).thenReturn(Future.successful(c2Result))
    op(c1, c2)
  }

  private def request[A](maybeSubject: Option[Subject]): AuthenticatedRequest[A] = new AuthenticatedRequest(mock(classOf[Request[A]]), maybeSubject)

  private def handler(maybeSubject: Option[Subject]): DeadboltHandler = {
    val handler = mock(classOf[DeadboltHandler])
    when(handler.getSubject(any[AuthenticatedRequest[_]])).thenReturn(Future {
      maybeSubject
    }(ec))
    handler
  }

}
