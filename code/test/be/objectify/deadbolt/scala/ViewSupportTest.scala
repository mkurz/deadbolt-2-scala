package be.objectify.deadbolt.scala

import java.util.regex.Pattern
import javax.inject.Provider

import be.objectify.deadbolt.scala.cache.PatternCache
import be.objectify.deadbolt.scala.models.{PatternType, Subject}
import be.objectify.deadbolt.scala.testhelpers.{SecurityPermission, User, SecurityRole}
import org.specs2.mock.Mockito
import play.api.{Application, Configuration}
import play.api.test.PlaySpecification
import org.mockito.ArgumentMatchers.{eq => eqTo}

import scala.concurrent.{Future, ExecutionContext}

/**
  *
  * @author Steve Chaloner (steve@objectify.be)
  */
object ViewSupportTest extends PlaySpecification with Mockito {

  val ec = scala.concurrent.ExecutionContext.Implicits.global
  val logic: ConstraintLogic = new ConstraintLogic(new StaticConstraintAnalyzer(new PatternCache {
    override def apply(value: String): Option[Pattern] = Some(Pattern.compile(value))
  }),
                                                    new ExecutionContextProvider {
                                                      override def get(): ExecutionContext = ec
                                                    })

  val viewSupport: ViewSupport = new ViewSupport(Configuration.empty,
                                                  new DefaultTemplateFailureListenerProvider(mock[Provider[Application]]),
                                                  logic)

  "subjectPresent should" >> {
    "evaluate to true when" >> {
      "a subject is present" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(Some(mock[Subject]))
        val result: Boolean = viewSupport.subjectPresent(handler, 1000L, request)
        result should beTrue
      }
    }
    "evaluate to false when" >> {
      "a subject is not present" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(None)
        val result: Boolean = viewSupport.subjectPresent(handler, 1000L, request)
        result should beFalse
      }
    }
  }

  "subjectNotPresent should" >> {
    "evaluate to true when" >> {
      "a subject is not present" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(None)
        val result: Boolean = viewSupport.subjectNotPresent(handler, 1000L, request)
        result should beTrue
      }
    }
    "evaluate to false when" >> {
      "a subject is present" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(Some(mock[Subject]))
        val result: Boolean = viewSupport.subjectNotPresent(handler, 1000L, request)
        result should beFalse
      }
    }
  }

  "restrict should" >> {
    "evaluate to true when" >> {
      "the necessary roles are present" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        val subject: Subject = User(roles = List(SecurityRole("foo")))
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(Some(subject))
        val result: Boolean = viewSupport.restrict(List(Array("foo")), handler, 1000L, request)
        result should beTrue
      }
    }
    "evaluate to false when" >> {
      "the necessary roles are not present" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        val subject: Subject = User(roles = List(SecurityRole("foo")))
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(Some(subject))
        val result: Boolean = viewSupport.restrict(List(Array("bar")), handler, 1000L, request)
        result should beFalse
      }
    }
  }

  "dynamic should" >> {
    "evaluate to true when" >> {
      "allowed" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        val drh: DynamicResourceHandler = mock[DynamicResourceHandler]
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(None)
        drh.isAllowed(eqTo("foo"), eqTo(Some("bar")), eqTo(handler), any[AuthenticatedRequest[_]]) returns Future.successful(true)
        handler.getDynamicResourceHandler(any[AuthenticatedRequest[_]]) returns Future.successful(Some(drh))
        val result: Boolean = viewSupport.dynamic("foo", Some("bar"), handler, 1000L, request)
        result should beTrue
      }
    }
    "evaluate to false when" >> {
      "denied" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        val drh: DynamicResourceHandler = mock[DynamicResourceHandler]
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(None)
        drh.isAllowed(eqTo("foo"), eqTo(Some("bar")), eqTo(handler), any[AuthenticatedRequest[_]]) returns Future.successful(false)
        handler.getDynamicResourceHandler(any[AuthenticatedRequest[_]]) returns Future.successful(Some(drh))
        val result: Boolean = viewSupport.dynamic("foo", Some("bar"), handler, 1000L, request)
        result should beFalse
      }
    }
  }

  "pattern should" >> {
    "evaluate to true when" >> {
      "the pattern type is EQUALITY and the subject has an equal permission" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        val subject: Subject = User(permissions = List(SecurityPermission("foo")))
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(Some(subject))
        val result: Boolean = viewSupport.pattern("foo", PatternType.EQUALITY, None, false, handler, 1000L, request)
        result should beTrue
      }
      "the pattern type is REGEX and the subject has a matching permission" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        val subject: Subject = User(permissions = List(SecurityPermission("foo.bar")))
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(Some(subject))
        val result: Boolean = viewSupport.pattern("foo.*", PatternType.REGEX, None, false, handler, 1000L, request)
        result should beTrue
      }
      "the pattern type is CUSTOM and checkPermission evaluates to true" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(Some(User()))
        val drh: DynamicResourceHandler = mock[DynamicResourceHandler]
        drh.checkPermission(eqTo("foo"), eqTo(Some("bar")), eqTo(handler), any[AuthenticatedRequest[_]]) returns Future.successful(true)
        handler.getDynamicResourceHandler(any[AuthenticatedRequest[_]]) returns Future.successful(Some(drh))
        val result: Boolean = viewSupport.pattern("foo", PatternType.CUSTOM, Some("bar"), false, handler, 1000L, request)
        result should beTrue
      }
    }
    "evaluate to false when" >> {
      "the pattern type is EQUALITY and the subject has an equal permission" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        val subject: Subject = User(permissions = List(SecurityPermission("bar")))
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(Some(subject))
        val result: Boolean = viewSupport.pattern("foo", PatternType.EQUALITY, None, false, handler, 1000L, request)
        result should beFalse
      }
      "the pattern type is REGEX and the subject has a matching permission" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        val subject: Subject = User(permissions = List(SecurityPermission("foo.bar")))
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(Some(subject))
        val result: Boolean = viewSupport.pattern("bar.*", PatternType.REGEX, None, false, handler, 1000L, request)
        result should beFalse
      }
      "the pattern type is CUSTOM and checkPermission evaluates to true" >> {
        val handler = mock[DeadboltHandler]
        val request = mock[AuthenticatedRequest[_]]
        handler.getSubject(any[AuthenticatedRequest[_]]) returns Future.successful(Some(User()))
        val drh: DynamicResourceHandler = mock[DynamicResourceHandler]
        drh.checkPermission(eqTo("foo"), eqTo(Some("bar")), eqTo(handler), any[AuthenticatedRequest[_]]) returns Future.successful(false)
        handler.getDynamicResourceHandler(any[AuthenticatedRequest[_]]) returns Future.successful(Some(drh))
        val result: Boolean = viewSupport.pattern("foo", PatternType.CUSTOM, Some("bar"), false, handler, 1000L, request)
        result should beFalse
      }
    }
  }
}
