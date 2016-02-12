package be.objectify.deadbolt.scala

import java.util.regex.Pattern

import be.objectify.deadbolt.scala.cache.PatternCache
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import be.objectify.deadbolt.scala.models.{Permission, Role, Subject}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object StaticConstraintAnalyzerTest extends Specification with Mockito {

  val analyzer: StaticConstraintAnalyzer = new StaticConstraintAnalyzer(new PatternCache {
    override def apply(value: String): Option[Pattern] = Some(Pattern.compile(value))
  })

  "getSubjectRoles should" >> {
    "return an empty list when" >> {
      "maybeSubject is None" >> {
        analyzer.getSubjectRoles(None).isEmpty
      }
      "the subject has no roles" >> {
        val subject = mock[Subject]
        subject.roles returns List()
        analyzer.getSubjectRoles(Some(subject)).isEmpty
      }
    }
    "return a list of string representations of the roles in the same order when" >> {
      "roles are present" >> {
        val subject = mock[Subject]
        subject.roles returns List(TestRole("admin"), TestRole("editor"))
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
        val subject = mock[Subject]
        subject.roles returns List()
        !analyzer.hasRole(Some(subject), "foo")
      }
      "the role is not present" >> {
        val subject = mock[Subject]
        subject.roles returns List(TestRole("bar"))
        !analyzer.hasRole(Some(subject), "foo")
      }
      "the subject has the required role but it is negated" >> {
        val subject = mock[Subject]
        subject.roles returns List(TestRole("admin"))
        !analyzer.hasAllRoles(Some(subject), Array("!admin"))
      }
    }
    "return true when" >> {
      "the subject only has the matching role" >> {
        val subject = mock[Subject]
        subject.roles returns List(TestRole("foo"))
        analyzer.hasRole(Some(subject), "foo")
      }
      "the subject has the matching role plus others" >> {
        val subject = mock[Subject]
        subject.roles returns List(TestRole("foo"), TestRole("bar"))
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
        val subject = mock[Subject]
        subject.roles returns List()
        !analyzer.hasAllRoles(Some(subject), Array("admin", "editor"))
      }
      "the subject has all required roles but one required role is negated" >> {
        val subject = mock[Subject]
        subject.roles returns List(TestRole("admin"), TestRole("editor"))
        !analyzer.hasAllRoles(Some(subject), Array("admin", "!editor"))
      }
      "the subject has all but one of the required roles" >> {
        val subject = mock[Subject]
        subject.roles returns List(TestRole("admin"), TestRole("editor"))
        !analyzer.hasAllRoles(Some(subject), Array("admin", "editor", "foo"))
      }
      "there are no required roles" >> {
        val subject = mock[Subject]
        subject.roles returns List(TestRole("admin"), TestRole("editor"))
        !analyzer.hasAllRoles(Some(subject), Array())
      }
      "there are no role matches" >> {
        val subject = mock[Subject]
        subject.roles returns List(TestRole("admin"), TestRole("editor"))
        !analyzer.hasAllRoles(Some(subject), Array("foo", "bar"))
      }
    }
    "return true when" >> {
      "the subject has all required roles" >> {
        val subject = mock[Subject]
        subject.roles returns List(TestRole("admin"), TestRole("editor"))
        analyzer.hasAllRoles(Some(subject), Array("admin", "editor"))
      }
      "the subject has all required roles plus others" >> {
        val subject = mock[Subject]
        subject.roles returns List(TestRole("admin"), TestRole("editor"), TestRole("foo"))
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
        val subject = mock[Subject]
        !analyzer.checkRegexPattern(Some(subject), None)
      }
      "none of the permissions match the regular expression" >> {
        val subject = mock[Subject]
        subject.permissions returns List(TestPermission("D"))
        !analyzer.checkRegexPattern(Some(subject), Some("[ABC]"))
      }
      "the subject has no permissions" >> {
        val subject = mock[Subject]
        subject.permissions returns List()
        !analyzer.checkRegexPattern(Some(subject), Some("[ABC]"))
      }
    }
    "return true when" >> {
      "the subject has one permission that matches the regular expression" >> {
        val subject = mock[Subject]
        subject.permissions returns List(TestPermission("B"))
        analyzer.checkRegexPattern(Some(subject), Some("[ABC]"))
      }
      "the subject has one permission that matches the regular expression, plus others" >> {
        val subject = mock[Subject]
        subject.permissions returns List(TestPermission("B"), TestPermission("D"))
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
        val subject = mock[Subject]
        !analyzer.checkPatternEquality(Some(subject), None)
      }
      "none of the permissions equal the pattern value" >> {
        val subject = mock[Subject]
        subject.permissions returns List(TestPermission("bar"))
        !analyzer.checkPatternEquality(Some(subject), Some("foo"))
      }
      "the subject has no permissions" >> {
        val subject = mock[Subject]
        subject.permissions returns List()
        !analyzer.checkPatternEquality(Some(subject), Some("foo"))
      }
    }
    "return true when" >> {
      "the subject has one permission that equals the pattern value" >> {
        val subject = mock[Subject]
        subject.permissions returns List(TestPermission("foo"))
        analyzer.checkPatternEquality(Some(subject), Some("foo"))
      }
      "the subject has one permission that equals the pattern value, plus others" >> {
        val subject = mock[Subject]
        subject.permissions returns List(TestPermission("foo"), TestPermission("bar"))
        analyzer.checkPatternEquality(Some(subject), Some("foo"))
      }
    }
  }

  case class TestRole(name: String) extends Role
  case class TestPermission(value: String) extends Permission
}

