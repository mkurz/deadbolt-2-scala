package be.objectify.deadbolt.scala

import java.util.regex.Pattern
import javax.inject.Provider

import be.objectify.deadbolt.scala.cache.PatternCache
import be.objectify.deadbolt.scala.models.{PatternType, Subject}
import be.objectify.deadbolt.scala.testhelpers.{SecurityPermission, User, SecurityRole}
import org.specs2.mock.Mockito
import play.api.{Application, Configuration}
import play.api.test.PlaySpecification

import scala.concurrent.{Future, ExecutionContext}

/**
  *
  * @author Steve Chaloner (steve@objectify.be)
  */
object ViewSupportTest extends PlaySpecification with Mockito {
  val patternCache: PatternCache = new PatternCache {
    override def apply(value: String): Option[Pattern] = Some(Pattern.compile(value))
  }
  val analyzer: StaticConstraintAnalyzer = new StaticConstraintAnalyzer(patternCache)
  val ec = scala.concurrent.ExecutionContext.Implicits.global
  val ecProvider: ExecutionContextProvider = new ExecutionContextProvider {
    override def get(): ExecutionContext = ec
  }
  val logic: ConstraintLogic = new ConstraintLogic(analyzer,
                                                    ecProvider)

  val config: Configuration = mock[Configuration]
  config.getLong("deadbolt.scala.view-timeout") returns None
  val viewSupport: ViewSupport = new ViewSupport(config,
                                                  analyzer,
                                                  patternCache,
                                                  new DefaultTemplateFailureListenerProvider(mock[Provider[Application]]),
                                                  ecProvider)

  "subjectPresent should" >> {
    val handler = mock[DeadboltHandler]
    val request = mock[AuthenticatedRequest[_]]
    "evaluate to true when" >> {
      "a subject is present" >> {
        handler.getSubject(request) returns Future.successful(Some(mock[Subject]))
        val result: Boolean = viewSupport.subjectPresent(handler, 1000L, request)
        result should beTrue
      }
    }
    "evaluate to false when" >> {
      "a subject is not present" >> {
        handler.getSubject(request) returns Future.successful(None)
        val result: Boolean = viewSupport.subjectPresent(handler, 1000L, request)
        result should beFalse
      }
    }
  }

  "subjectNotPresent should" >> {
    val handler = mock[DeadboltHandler]
    val request = mock[AuthenticatedRequest[_]]
    "evaluate to true when" >> {
      "a subject is not present" >> {
        handler.getSubject(request) returns Future.successful(None)
        val result: Boolean = viewSupport.subjectNotPresent(handler, 1000L, request)
        result should beTrue
      }
    }
    "evaluate to false when" >> {
      "a subject is present" >> {
        handler.getSubject(request) returns Future.successful(Some(mock[Subject]))
        val result: Boolean = viewSupport.subjectNotPresent(handler, 1000L, request)
        result should beFalse
      }
    }
  }

  "restrict should" >> {
    val handler = mock[DeadboltHandler]
    val request = mock[AuthenticatedRequest[_]]
    "evaluate to true when" >> {
      "the necessary roles are present" >> {
        val subject: Subject = User(roles = List(SecurityRole("foo")))
        handler.getSubject(request) returns Future.successful(Some(subject))
        val result: Boolean = viewSupport.restrict(List(Array("foo")), handler, 1000L, request)
        result should beTrue
      }
    }
    "evaluate to false when" >> {
      "the necessary roles are not present" >> {
        val subject: Subject = User(roles = List(SecurityRole("foo")))
        handler.getSubject(request) returns Future.successful(Some(subject))
        val result: Boolean = viewSupport.restrict(List(Array("bar")), handler, 1000L, request)
        result should beFalse
      }
    }
  }

  "dynamic should" >> {
    val handler = mock[DeadboltHandler]
    val request = mock[AuthenticatedRequest[_]]
    "evaluate to true when" >> {
      "allowed" >> {
        val drh: DynamicResourceHandler = mock[DynamicResourceHandler]
        drh.isAllowed("foo", Some("bar"), handler, request) returns Future.successful(true)
        handler.getDynamicResourceHandler(request) returns Future.successful(Some(drh))
        val result: Boolean = viewSupport.dynamic("foo", Some("bar"), handler, 1000L, request)
        result should beTrue
      }
    }
    "evaluate to false when" >> {
      "denied" >> {
        val drh: DynamicResourceHandler = mock[DynamicResourceHandler]
        drh.isAllowed("foo", Some("bar"), handler, request) returns Future.successful(false)
        handler.getDynamicResourceHandler(request) returns Future.successful(Some(drh))
        val result: Boolean = viewSupport.dynamic("foo", Some("bar"), handler, 1000L, request)
        result should beFalse
      }
    }
  }

  "pattern should" >> {
    val handler = mock[DeadboltHandler]
    val request = mock[AuthenticatedRequest[_]]
    "evaluate to true when" >> {
      "the pattern type is EQUALITY and the subject has an equal permission" >> {
        val subject: Subject = User(permissions = List(SecurityPermission("foo")))
        handler.getSubject(request) returns Future.successful(Some(subject))
        val result: Boolean = viewSupport.pattern("foo", PatternType.EQUALITY, None, handler, 1000L, request)
        result should beTrue
      }
      "the pattern type is REGEX and the subject has a matching permission" >> {
        val subject: Subject = User(permissions = List(SecurityPermission("foo.bar")))
        handler.getSubject(request) returns Future.successful(Some(subject))
        val result: Boolean = viewSupport.pattern("foo.*", PatternType.REGEX, None, handler, 1000L, request)
        result should beTrue
      }
      "the pattern type is CUSTOM and checkPermission evaluates to true" >> {
        val drh: DynamicResourceHandler = mock[DynamicResourceHandler]
        drh.checkPermission("foo", Some("bar"), handler, request) returns Future.successful(true)
        handler.getDynamicResourceHandler(request) returns Future.successful(Some(drh))
        val result: Boolean = viewSupport.pattern("foo", PatternType.CUSTOM, Some("bar"), handler, 1000L, request)
        result should beTrue
      }
    }
    "evaluate to false when" >> {
      "the pattern type is EQUALITY and the subject has an equal permission" >> {
        val subject: Subject = User(permissions = List(SecurityPermission("bar")))
        handler.getSubject(request) returns Future.successful(Some(subject))
        val result: Boolean = viewSupport.pattern("foo", PatternType.EQUALITY, None, handler, 1000L, request)
        result should beFalse
      }
      "the pattern type is REGEX and the subject has a matching permission" >> {
        val subject: Subject = User(permissions = List(SecurityPermission("foo.bar")))
        handler.getSubject(request) returns Future.successful(Some(subject))
        val result: Boolean = viewSupport.pattern("bar.*", PatternType.REGEX, None, handler, 1000L, request)
        result should beFalse
      }
      "the pattern type is CUSTOM and checkPermission evaluates to true" >> {
        val drh: DynamicResourceHandler = mock[DynamicResourceHandler]
        drh.checkPermission("foo", Some("bar"), handler, request) returns Future.successful(false)
        handler.getDynamicResourceHandler(request) returns Future.successful(Some(drh))
        val result: Boolean = viewSupport.pattern("foo", PatternType.CUSTOM, Some("bar"), handler, 1000L, request)
        result should beFalse
      }
    }
  }
}
