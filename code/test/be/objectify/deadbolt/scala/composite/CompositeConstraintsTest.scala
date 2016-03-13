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

import java.util.regex.Pattern

import be.objectify.deadbolt.scala.cache.PatternCache
import be.objectify.deadbolt.scala._
import be.objectify.deadbolt.scala.models.{Subject, PatternType}
import be.objectify.deadbolt.scala.testhelpers.{SecurityPermission, SecurityRole, User}
import org.mockito.Matchers
import org.specs2.mock.Mockito
import play.api.mvc.Request
import play.api.test.PlaySpecification

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object CompositeConstraintsTest extends PlaySpecification with Mockito {

  val analyzer: StaticConstraintAnalyzer = new StaticConstraintAnalyzer(new PatternCache {
    override def apply(value: String): Option[Pattern] = Some(Pattern.compile(value))
  })
  val ec = scala.concurrent.ExecutionContext.Implicits.global
  val ecProvider: ExecutionContextProvider = new ExecutionContextProvider {
    override def get(): ExecutionContext = ec
  }
  val logic: ConstraintLogic = new ConstraintLogic(analyzer,
                                                    ecProvider)
  val constraints: CompositeConstraints = new CompositeConstraints(logic,
                                                                    ecProvider)

  "Restrict" should {
    "return false when" >> {
      "there is no subject" >> {
        val result = constraints.Restrict(List(Array("foo")))(request(None),
                                                               handler(None))
        await(result) should beFalse
      }
      "the subject has no roles" >> {
        val subject = Some(User())
        val result = constraints.Restrict(List(Array("foo")))(request(subject),
                                                               handler(subject))
        await(result) should beFalse
      }
      "the subject has all required roles but one required role is negated" >> {
        val subject = Some(User(roles = List(SecurityRole("admin"), SecurityRole("editor"))))
        val result = constraints.Restrict(List(Array("admin", "!editor")))(request(subject),
                                                                            handler(subject))
        await(result) should beFalse
      }
      "the subject has all but one of the required roles" >> {
        val subject = Some(User(roles = List(SecurityRole("admin"), SecurityRole("editor"))))
        val result = constraints.Restrict(List(Array("admin", "editor", "foo")))(request(subject),
                                                                                  handler(subject))
        await(result) should beFalse
      }
      "there are no required roles" >> {
        val subject = Some(User(roles = List(SecurityRole("admin"), SecurityRole("editor"))))
        val result = constraints.Restrict(List(Array()))(request(subject),
                                                          handler(subject))
        await(result) should beFalse
      }
      "there are no role matches" >> {
        val subject = Some(User(roles = List(SecurityRole("admin"), SecurityRole("editor"))))
        val result = constraints.Restrict(List(Array("foo", "bar")))(request(subject),
                                                                      handler(subject))
        await(result) should beFalse
      }
    }
    "return true when" should {
      "the subject has all required roles" >> {
        val subject = Some(User(roles = List(SecurityRole("admin"), SecurityRole("editor"))))
        val result = constraints.Restrict(List(Array("admin", "editor")))(request(subject),
                                                                           handler(subject))
        await(result) should beTrue
      }
      "the subject has all required roles plus others" >> {
        val subject = Some(User(roles = List(SecurityRole("admin"), SecurityRole("editor"), SecurityRole("foo"))))
        val result = constraints.Restrict(List(Array("admin", "editor")))(request(subject),
                                                                           handler(subject))
        await(result) should beTrue
      }
    }
  }

  "Pattern of type" >> {
    "REGEX" >> {
      "return false when " >> {
        "maybeSubject is None" >> {
          val result = constraints.Pattern("[ABC]",
                                            PatternType.REGEX,
                                            invert = false)(request(None), handler(None))
          await(result) should beFalse
        }
        "none of the permissions match the regular expression" >> {
          val subject = Some(User(permissions = List(SecurityPermission("D"))))
          val result = constraints.Pattern("[ABC]",
                                            PatternType.REGEX,
                                            invert = false)(request(subject), handler(subject))
          await(result) should beFalse
        }
        "the subject has no permissions" >> {
          val subject = Some(User())
          val result = constraints.Pattern("[ABC]",
                                            PatternType.REGEX,
                                            invert = false)(request(subject), handler(subject))
          await(result) should beFalse
        }
      }
      "return true when" >> {
        "the subject has one permission that matches the regular expression" >> {
          val subject = Some(User(permissions = List(SecurityPermission("B"))))
          val result = constraints.Pattern("[ABC]",
                                            PatternType.REGEX,
                                            invert = false)(request(subject), handler(subject))
          await(result) should beTrue
        }
        "the subject has one permission that matches the regular expression, plus others" >> {
          val subject = Some(User(permissions = List(SecurityPermission("B"), SecurityPermission("D"))))
          val result = constraints.Pattern("[ABC]",
                                            PatternType.REGEX,
                                            invert = false)(request(subject), handler(subject))
          await(result) should beTrue
        }
      }
    }

    "EQUALITY" should {
      "return false when" should {
        "maybeSubject is None" >> {
          val result: Future[Boolean] = constraints.Pattern("foo",
                                                             PatternType.EQUALITY,
                                                             invert = false)(request(None), handler(None))
          await(result) should beFalse
        }
        "none of the permissions equal the pattern value" >> {
          val subject = Some(User(permissions = List(SecurityPermission("bar"))))
          val result: Future[Boolean] = constraints.Pattern("foo",
                                                             PatternType.EQUALITY,
                                                             invert = false)(request(subject), handler(subject))
          await(result) should beFalse
        }
        "the subject has no permissions" >> {
          val subject = Some(User())
          val result: Future[Boolean] = constraints.Pattern("foo",
                                                             PatternType.EQUALITY,
                                                             invert = false)(request(subject), handler(subject))
          await(result) should beFalse
        }
      }
      "return true when" >> {
        "the subject has one permission that equals the pattern value" >> {
          val subject = Some(User(permissions = List(SecurityPermission("foo"))))
          val result: Future[Boolean] = constraints.Pattern("foo",
                                                             PatternType.EQUALITY,
                                                             invert = false)(request(subject), handler(subject))
          await(result) should beTrue
        }
        "the subject has one permission that equals the pattern value, plus others" >> {
          val subject = Some(User(permissions = List(SecurityPermission("foo"), SecurityPermission("bar"))))
          val result: Future[Boolean] = constraints.Pattern("foo",
                                                             PatternType.EQUALITY,
                                                             invert = false)(request(subject), handler(subject))
          await(result) should beTrue
        }
      }
    }

    "CUSTOM" should {
      "return false when" >> {
        "dynamicResourceHandler is None" >> {
          val subject = Some(User())
          val result: Future[Boolean] = constraints.Pattern("foo",
                                                             PatternType.CUSTOM,
                                                             invert = false)(request(subject), handler(subject, None))
          await(result) must throwA(new RuntimeException("A custom pattern is specified but no dynamic resource handler is provided"))
        }
        "checkPermission returns false" >> {
          val subject = Some(User())
          val drh = mock[DynamicResourceHandler]
          val dh = handler(subject, Some(drh))
          drh.checkPermission(Matchers.eq("foo"), Matchers.eq(dh), any[AuthenticatedRequest[_]]) returns Future {false}(ec)
          val result: Future[Boolean] = constraints.Pattern("foo",
                                                             PatternType.CUSTOM,
                                                             invert = false)(request(subject), dh)
          await(result) should beFalse
        }
      }
      "return true when" >> {
        "checkPermission returns true" >> {
          val subject = Some(User())
          val drh = mock[DynamicResourceHandler]
          val dh = handler(subject, Some(drh))
          drh.checkPermission(Matchers.eq("foo"), Matchers.eq(dh), any[AuthenticatedRequest[_]]) returns Future {true}(ec)
          val result: Future[Boolean] = constraints.Pattern("foo",
                                                             PatternType.CUSTOM,
                                                             invert = false)(request(subject), dh)
          await(result) should beTrue
        }
      }
    }
  }

  "Dynamic" should {
    "return false when" >> {
      "dynamicResourceHandler is None" >> {
        val subject = Some(User())
        val result: Future[Boolean] = constraints.Dynamic("foo",
                                                           "bar")(request(subject), handler(subject, None))
        await(result) must throwA(new RuntimeException("A dynamic resource is specified but no dynamic resource handler is provided"))
      }
      "isAllowed returns false" >> {
        val subject = Some(User())
        val drh = mock[DynamicResourceHandler]
        val dh = handler(subject, Some(drh))
        drh.isAllowed(Matchers.eq("foo"), Matchers.eq("bar"), Matchers.eq(dh), any[AuthenticatedRequest[_]]) returns Future {false}(ec)
        val result: Future[Boolean] = constraints.Dynamic("foo",
                                                           "bar")(request(subject), dh)
        await(result) should beFalse
      }
    }
    "return true when" >> {
      "isAllowed returns true" >> {
        val subject = Some(User())
        val drh = mock[DynamicResourceHandler]
        val dh = handler(subject, Some(drh))
        drh.isAllowed(Matchers.eq("foo"), Matchers.eq("bar"), Matchers.eq(dh), any[AuthenticatedRequest[_]]) returns Future {true}(ec)
        val result: Future[Boolean] = constraints.Dynamic("foo",
                                                           "bar")(request(subject), dh)
        await(result) should beTrue

      }
    }
  }

  "SubjectPresent " should {
    "return true when a subject is present" >> {
      val subject = Some(User())
      val result: Future[Boolean] = constraints.SubjectPresent()(request(subject), handler(subject))
      await(result) should beTrue
    }
    "return false when a subject is not present" >> {
      val result: Future[Boolean] = constraints.SubjectPresent()(request(None), handler(None))
      await(result) should beFalse
    }
  }

  "SubjectNotPresent " should {
    "return false when a subject is present" >> {
      val subject = Some(User())
      val result: Future[Boolean] = constraints.SubjectNotPresent()(request(subject), handler(subject))
      await(result) should beFalse
    }
    "return true when a subject is not present" >> {
      val result: Future[Boolean] = constraints.SubjectNotPresent()(request(None), handler(None))
      await(result) should beTrue
    }
  }

  private def request[A](maybeSubject: Option[Subject]): AuthenticatedRequest[A] = AuthenticatedRequest(mock[Request[A]], maybeSubject)

  private def handler(maybeSubject: Option[Subject]): DeadboltHandler = handler(maybeSubject, None)

  private def handler(maybeSubject: Option[Subject],
                      maybeDrh: Option[DynamicResourceHandler]): DeadboltHandler = {
    val handler = mock[DeadboltHandler]
    handler.getSubject(any[AuthenticatedRequest[_]]) returns Future {maybeSubject}(ec)
    handler.getDynamicResourceHandler(any[AuthenticatedRequest[_]]) returns Future {maybeDrh}(ec)
  }


}
