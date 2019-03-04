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
package be.objectify.deadbolt.scala

import java.util.regex.Pattern

import be.objectify.deadbolt.scala.cache.PatternCache
import be.objectify.deadbolt.scala.models.{PatternType, Subject}
import be.objectify.deadbolt.scala.testhelpers.{SecurityPermission, SecurityRole, User}
import org.mockito.ArgumentMatchers
import org.specs2.mock.Mockito
import play.api.mvc.Request
import play.api.test.PlaySpecification

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object ConstraintLogicTest extends PlaySpecification with Mockito {
  val analyzer: StaticConstraintAnalyzer = new StaticConstraintAnalyzer(new PatternCache {
    override def apply(value: String): Option[Pattern] = Some(Pattern.compile(value))
  })
  val ec = scala.concurrent.ExecutionContext.Implicits.global
  val logic: ConstraintLogic = new ConstraintLogic(analyzer,
                                                   new ExecutionContextProvider {
                                                     override def get(): ExecutionContext = ec
                                                   })

  "restrict" should {
    "call the fail function when" >> {
      "there is no subject" >> {
        val result: Future[Boolean] = logic.restrict(request(None),
                                                      handler(None),
                                                      List(Array("foo")),
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beFalse
      }
      "the subject has no roles" >> {
        val subject = Some(User())
        val result: Future[Boolean] = logic.restrict(request(subject),
                                                      handler(subject),
                                                      List(Array("foo")),
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beFalse
      }
      "the subject has all required roles but one required role is negated" >> {
        val subject = Some(User(roles = List(SecurityRole("admin"), SecurityRole("editor"))))
        val result: Future[Boolean] = logic.restrict(request(subject),
                                                      handler(subject),
                                                      List(Array("admin", "!editor")),
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beFalse
      }
      "the subject has all but one of the required roles" >> {
        val subject = Some(User(roles = List(SecurityRole("admin"), SecurityRole("editor"))))
        val result: Future[Boolean] = logic.restrict(request(subject),
                                                      handler(subject),
                                                      List(Array("admin", "editor", "foo")),
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beFalse
      }
      "there are no required roles" >> {
        val subject = Some(User(roles = List(SecurityRole("admin"), SecurityRole("editor"))))
        val result: Future[Boolean] = logic.restrict(request(subject),
                                                      handler(subject),
                                                      List(Array()),
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beFalse
      }
      "there are no role matches" >> {
        val subject = Some(User(roles = List(SecurityRole("admin"), SecurityRole("editor"))))
        val result: Future[Boolean] = logic.restrict(request(subject),
                                                      handler(subject),
                                                      List(Array("foo", "bar")),
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beFalse
      }
    }
    "call the pass function when" should {
      "the subject has all required roles" >> {
        val subject = Some(User(roles = List(SecurityRole("admin"), SecurityRole("editor"))))
        val result: Future[Boolean] = logic.restrict(request(subject),
                                                      handler(subject),
                                                      List(Array("admin", "editor")),
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beTrue
      }
      "the subject has all required roles plus others" >> {
        val subject = Some(User(roles = List(SecurityRole("admin"), SecurityRole("editor"), SecurityRole("foo"))))
        val result: Future[Boolean] = logic.restrict(request(subject),
                                                      handler(subject),
                                                      List(Array("admin", "editor")),
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beTrue
      }
    }
  }

  "pattern of type" should {
    "REGEX" >> {
      "call the fail function when " >> {
        "maybeSubject is None" >> {
          val result: Future[Boolean] = logic.pattern(request(None),
                                                      handler(None),
                                                      "[ABC]",
                                                      PatternType.REGEX,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) should beFalse
        }
        "none of the permissions match the regular expression" >> {
          val subject = Some(User(permissions = List(SecurityPermission("D"))))
          val result: Future[Boolean] = logic.pattern(request(subject),
                                                      handler(subject),
                                                      "[ABC]",
                                                      PatternType.REGEX,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) should beFalse
        }
        "the subject has no permissions" >> {
          val subject = Some(User())
          val result: Future[Boolean] = logic.pattern(request(subject),
                                                      handler(subject),
                                                      "[ABC]",
                                                      PatternType.REGEX,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) should beFalse
        }
      }
      "call the pass function when" >> {
        "the subject has one permission that matches the regular expression" >> {
          val subject = Some(User(permissions = List(SecurityPermission("B"))))
          val result: Future[Boolean] = logic.pattern(request(subject),
                                                      handler(subject),
                                                      "[ABC]",
                                                      PatternType.REGEX,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) should beTrue
        }
        "the subject has one permission that matches the regular expression, plus others" >> {
          val subject = Some(User(permissions = List(SecurityPermission("B"), SecurityPermission("D"))))
          val result: Future[Boolean] = logic.pattern(request(subject),
                                                      handler(subject),
                                                      "[ABC]",
                                                      PatternType.REGEX,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) should beTrue
        }
      }
    }

    "EQUALITY" should {
      "call the fail function when" should {
        "maybeSubject is None" >> {
          val result: Future[Boolean] = logic.pattern(request(None),
                                                      handler(None),
                                                      "foo",
                                                      PatternType.EQUALITY,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) should beFalse
        }
        "none of the permissions equal the pattern value" >> {
          val subject = Some(User(permissions = List(SecurityPermission("bar"))))
          val result: Future[Boolean] = logic.pattern(request(subject),
                                                      handler(subject),
                                                      "foo",
                                                      PatternType.EQUALITY,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) should beFalse
        }
        "the subject has no permissions" >> {
          val subject = Some(User())
          val result: Future[Boolean] = logic.pattern(request(subject),
                                                      handler(subject),
                                                      "foo",
                                                      PatternType.EQUALITY,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) should beFalse
        }
      }
      "call the pass function when" >> {
        "the subject has one permission that equals the pattern value" >> {
          val subject = Some(User(permissions = List(SecurityPermission("foo"))))
          val result: Future[Boolean] = logic.pattern(request(subject),
                                                      handler(subject),
                                                      "foo",
                                                      PatternType.EQUALITY,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) should beTrue
        }
        "the subject has one permission that equals the pattern value, plus others" >> {
          val subject = Some(User(permissions = List(SecurityPermission("foo"), SecurityPermission("bar"))))
          val result: Future[Boolean] = logic.pattern(request(subject),
                                                      handler(subject),
                                                      "foo",
                                                      PatternType.EQUALITY,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) should beTrue
        }
      }
    }

    "CUSTOM" should {
      "call the fail function when" >> {
        "dynamicResourceHandler is None" >> {
          val subject = Some(User())

          val result: Future[Boolean] = logic.pattern(request(subject),
                                                      handler(subject, None),
                                                      "foo",
                                                      PatternType.CUSTOM,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) must throwA(new RuntimeException("A custom pattern is specified but no dynamic resource handler is provided"))
        }
        "checkPermission returns false" >> {
          val subject = Some(User())
          val drh = mock[DynamicResourceHandler]
          val dh = handler(subject, Some(drh))
          drh.checkPermission(ArgumentMatchers.eq("foo"), ArgumentMatchers.eq(None), ArgumentMatchers.eq(dh), any[AuthenticatedRequest[_]]) returns Future.successful(false)
          val result: Future[Boolean] = logic.pattern(request(subject),
                                                      dh,
                                                      "foo",
                                                      PatternType.CUSTOM,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) should beFalse
        }
      }
      "call the pass function when" >> {
        "checkPermission returns true" >> {
          val subject = Some(User())
          val drh = mock[DynamicResourceHandler]
          val dh = handler(subject, Some(drh))
          drh.checkPermission(ArgumentMatchers.eq("foo"), ArgumentMatchers.eq(None), ArgumentMatchers.eq(dh), any[AuthenticatedRequest[_]]) returns Future.successful(true)
          val result: Future[Boolean] = logic.pattern(request(subject),
                                                      dh,
                                                      "foo",
                                                      PatternType.CUSTOM,
                                                      None,
                                                      invert = false,
                                                      pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                      fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
          await(result) should beTrue

        }
      }
    }
  }

  "dynamic" should {
    "call the fail function when" >> {
      "dynamicResourceHandler is None" >> {
        val subject = Some(User())
        val result: Future[Boolean] = logic.dynamic(request(subject),
                                                     handler(subject, None),
                                                     "foo",
                                                     Some("bar"),
                                                     pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                     fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) must throwA(new RuntimeException("A dynamic resource is specified but no dynamic resource handler is provided"))
      }
      "isAllowed returns false" >> {
        val subject = Some(User())
        val drh = mock[DynamicResourceHandler]
        val dh = handler(subject, Some(drh))
        drh.isAllowed(ArgumentMatchers.eq("foo"), ArgumentMatchers.eq(Some("bar")), ArgumentMatchers.eq(dh), any[AuthenticatedRequest[_]]) returns Future.successful(false)
        val result: Future[Boolean] = logic.dynamic(request(subject),
                                                     dh,
                                                     "foo",
                                                     Some("bar"),
                                                     pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                     fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beFalse
      }
    }
    "call the pass function when" >> {
      "isAllowed returns true" >> {
        val subject = Some(User())
        val drh = mock[DynamicResourceHandler]
        val dh = handler(subject, Some(drh))
        drh.isAllowed(ArgumentMatchers.eq("foo"), ArgumentMatchers.eq(Some("bar")), ArgumentMatchers.eq(dh), any[AuthenticatedRequest[_]]) returns Future.successful(true)
        val result: Future[Boolean] = logic.dynamic(request(subject),
                                                     dh,
                                                     "foo",
                                                     Some("bar"),
                                                     pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                     fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beTrue

      }
    }
  }

  "subjectPresent " should {
    "invoke the present function when a subject is present" >> {
      val subject = Some(User())
      val result: Future[Boolean] = logic.subjectPresent(request(subject),
                                                          handler(subject),
                                                          present = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                          notPresent = (ar: AuthenticatedRequest[_]) => Future.successful(false))
      await(result) should beTrue
    }
    "invoke the notPresent function when a subject is not present" >> {
      val result: Future[Boolean] = logic.subjectPresent(request(None),
                                                          handler(None),
                                                          present = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                          notPresent = (ar: AuthenticatedRequest[_]) => Future.successful(false))
      await(result) should beFalse
    }
  }

  "roleBasedPermissions" should {
    "invoke the pass function when" >> {
      "the subject has at least one permission defined by DeadboltHandler#getPermissionsForRole" >> {
        val subject = Some(User(permissions = List(SecurityPermission("hurdy.gurdy"))))
        val result: Future[Boolean] = logic.roleBasedPermissions(request(subject),
                                                                 handler(subject),
                                                                 "foo",
                                                                 pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                                 fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beTrue
      }
    }
    "invoke the fail function when" >> {
      "the subject has no permissions" >> {
        val subject = Some(User())
        val result: Future[Boolean] = logic.roleBasedPermissions(request(subject),
                                                                 handler(subject),
                                                                 "foo",
                                                                 pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
                                                                 fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beFalse
      }
      "the subject has at no matching permissions" >> {
        val subject = Some(User(permissions = List(SecurityPermission("aze.eza"))))
        val result: Future[Boolean] = logic.roleBasedPermissions(request(subject),
          handler(subject),
          "foo",
          pass = (ar: AuthenticatedRequest[_]) => Future.successful(true),
          fail = (ar: AuthenticatedRequest[_]) => Future.successful(false))
        await(result) should beFalse
      }
    }
  }

  private def request[A](maybeSubject: Option[Subject]): AuthenticatedRequest[A] = new AuthenticatedRequest(mock[Request[A]], maybeSubject)

  private def handler(maybeSubject: Option[Subject]): DeadboltHandler = handler(maybeSubject, None)

  private def handler(maybeSubject: Option[Subject],
                     maybeDrh: Option[DynamicResourceHandler]): DeadboltHandler = {
    val handler = mock[DeadboltHandler]
    handler.getSubject(any[AuthenticatedRequest[_]]) returns Future {maybeSubject}(ec)
    handler.getDynamicResourceHandler(any[AuthenticatedRequest[_]]) returns Future {maybeDrh}(ec)
    handler.getPermissionsForRole("foo") returns Future{List("hurdy.*")}(ec)
  }
}
