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
import be.objectify.deadbolt.scala.testhelpers.{User, SecurityRole, SecurityPermission}
import org.mockito.Mockito._
import org.specs2.mutable.Specification

import be.objectify.deadbolt.scala.models.Subject

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object StaticConstraintAnalyzerTest extends Specification {

  val analyzer: StaticConstraintAnalyzer = new StaticConstraintAnalyzer(new PatternCache {
    override def apply(value: String): Option[Pattern] = Some(Pattern.compile(value))
  })

  "getSubjectRoles should" >> {
    "return an empty list when" >> {
      "maybeSubject is None" >> {
        analyzer.getSubjectRoles(None).isEmpty
      }
      "the subject has no roles" >> {
        val subject = mock(classOf[Subject])
        when(subject.roles).thenReturn(List())
        analyzer.getSubjectRoles(Some(subject)).isEmpty
      }
    }
    "return a list of string representations of the roles in the same order when" >> {
      "roles are present" >> {
        val subject = User(roles = List(SecurityRole("admin"), SecurityRole("editor")))
        val maybeRoles: Option[List[String]] = analyzer.getSubjectRoles(Some(subject))
        maybeRoles.exists(roles => roles.equals(List("admin", "editor")))
      }
    }
  }

  "hasRole should" >> {
    "return false when" >> {
      "maybeSubject is None" >> {
        !analyzer.hasRole(None, "foo")
      }
      "the subject has no roles" >> {
        val subject = User()
        !analyzer.hasRole(Some(subject), "foo")
      }
      "the role is not present" >> {
        val subject = User(roles = List(SecurityRole("bar")))
        !analyzer.hasRole(Some(subject), "foo")
      }
      "the subject has the required role but it is negated" >> {
        val subject = User(roles = List(SecurityRole("admin")))
        !analyzer.hasAllRoles(Some(subject), Array("!admin"))
      }
    }
    "return true when" >> {
      "the subject only has the matching role" >> {
        val subject = User(roles = List(SecurityRole("foo")))
        analyzer.hasRole(Some(subject), "foo")
      }
      "the subject has the matching role plus others" >> {
        val subject = User(roles = List(SecurityRole("foo"), SecurityRole("bar")))
        analyzer.hasRole(Some(subject), "foo")
      }
    }
  }

  "hasAllRoles should" >> {
    "return false when" >> {
      "maybeSubject is None" >> {
        !analyzer.hasAllRoles(None, Array("admin", "editor"))
      }
      "the subject has no roles" >> {
        !analyzer.hasAllRoles(Some(User()), Array("admin", "editor"))
      }
      "the subject has all required roles but one required role is negated" >> {
        val subject = User(roles = List(SecurityRole("admin"), SecurityRole("editor")))
        !analyzer.hasAllRoles(Some(subject), Array("admin", "!editor"))
      }
      "the subject has all but one of the required roles" >> {
        val subject = User(roles = List(SecurityRole("admin"), SecurityRole("editor")))
        !analyzer.hasAllRoles(Some(subject), Array("admin", "editor", "foo"))
      }
      "there are no required roles" >> {
        val subject = User(roles = List(SecurityRole("admin"), SecurityRole("editor")))
        !analyzer.hasAllRoles(Some(subject), Array())
      }
      "there are no role matches" >> {
        val subject = User(roles = List(SecurityRole("admin"), SecurityRole("editor")))
        !analyzer.hasAllRoles(Some(subject), Array("foo", "bar"))
      }
    }
    "return true when" >> {
      "the subject has all required roles" >> {
        val subject = User(roles = List(SecurityRole("admin"), SecurityRole("editor")))
        analyzer.hasAllRoles(Some(subject), Array("admin", "editor"))
      }
      "the subject has all required roles plus others" >> {
        val subject = User(roles = List(SecurityRole("admin"), SecurityRole("editor"), SecurityRole("foo")))
        analyzer.hasAllRoles(Some(subject), Array("admin", "editor"))
      }
    }
  }

  "checkRegexPattern should" >> {
    "return false when" >> {
      "maybeSubject is None" >> {
        !analyzer.checkRegexPattern(None, Some("[ABC]"))
      }
      "maybePattern is None" >> {
        !analyzer.checkRegexPattern(Some(User()), None)
      }
      "none of the permissions match the regular expression" >> {
        val subject = User(permissions = List(SecurityPermission("D")))
        !analyzer.checkRegexPattern(Some(subject), Some("[ABC]"))
      }
      "the subject has no permissions" >> {
        !analyzer.checkRegexPattern(Some(User()), Some("[ABC]"))
      }
    }
    "return true when" >> {
      "the subject has one permission that matches the regular expression" >> {
        val subject = User(permissions = List(SecurityPermission("B")))
        analyzer.checkRegexPattern(Some(subject), Some("[ABC]"))
      }
      "the subject has one permission that matches the regular expression, plus others" >> {
        val subject = User(permissions = List(SecurityPermission("B"), SecurityPermission("D")))
        analyzer.checkRegexPattern(Some(subject), Some("[ABC]"))
      }
    }
  }

  "checkPatternEquality should" >> {
    "return false when" >> {
      "maybeSubject is None" >> {
        !analyzer.checkPatternEquality(None, Some("foo"))
      }
      "maybePatternValue is None" >> {
        !analyzer.checkPatternEquality(Some(User()), None)
      }
      "none of the permissions equal the pattern value" >> {
        val subject = User(permissions = List(SecurityPermission("bar")))
        !analyzer.checkPatternEquality(Some(subject), Some("foo"))
      }
      "the subject has no permissions" >> {
        !analyzer.checkPatternEquality(Some(User()), Some("foo"))
      }
    }
    "return true when" >> {
      "the subject has one permission that equals the pattern value" >> {
        val subject = User(permissions = List(SecurityPermission("foo")))
        analyzer.checkPatternEquality(Some(subject), Some("foo"))
      }
      "the subject has one permission that equals the pattern value, plus others" >> {
        val subject = User(permissions = List(SecurityPermission("foo"), SecurityPermission("bar")))
        analyzer.checkPatternEquality(Some(subject), Some("foo"))
      }
    }
  }
}

