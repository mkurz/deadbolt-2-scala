package be.objectify.deadbolt.scala.filters

import akka.stream.Materializer
import be.objectify.deadbolt.scala.cache.{CompositeCache, HandlerCache}
import org.mockito.Mockito._
import play.api.test.PlaySpecification

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class DeadboltRouteCommentFilterTest extends PlaySpecification {

  val materializer = mock(classOf[Materializer])
  val handlerCache = mock(classOf[HandlerCache])
  val constraints = mock(classOf[FilterConstraints])
  val compositeCache = mock(classOf[CompositeCache])
  val filter: DeadboltRouteCommentFilter = new DeadboltRouteCommentFilter(materializer, handlerCache, constraints, compositeCache)

  "subjectPresent should" >> {
    "match when" >> {
      "deadbolt:subjectPresent is used" >> {
        "deadbolt:subjectPresent" match {
          case filter.subjectPresentComment(constraintName, handler) => "subjectPresent".equals(constraintName) && handler === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:subjectPresent:handler[foo] is used" >> {
        "deadbolt:subjectPresent:handler[foo]" match {
          case filter.subjectPresentComment(constraintName, handler) => "subjectPresent".equals(constraintName) && "foo".equals(handler) should beTrue
          case _ => false should beTrue
        }
      }
    }
  }

  "subjectNotPresent should" >> {
    "match when" >> {
      "deadbolt:subjectNotPresent is used" >> {
        "deadbolt:subjectNotPresent" match {
          case filter.subjectNotPresentComment(constraintName, handler) => "subjectNotPresent".equals(constraintName) && handler === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:subjectNotPresent:handler[foo] is used" >> {
        "deadbolt:subjectNotPresent:handler[foo]" match {
          case filter.subjectNotPresentComment(constraintName, handler) => "subjectNotPresent".equals(constraintName) && "foo".equals(handler) should beTrue
          case _ => false should beTrue
        }
      }
    }
  }

  "dynamic should" >> {
    "match when" >> {
      "deadbolt:dynamic:name[foo] is used" >> {
        "deadbolt:dynamic:name[foo]" match {
          case filter.dynamicComment(constraintName, name, handler) => "dynamic".equals(constraintName) && "foo".equals(name) && handler === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:dynamic:name[foo]:handler[bar] is used" >> {
        "deadbolt:dynamic:name[foo]:handler[bar]" match {
          case filter.dynamicComment(constraintName, name, handler) => "dynamic".equals(constraintName) && "foo".equals(name) && "bar".equals(handler) should beTrue
          case _ => false should beTrue
        }
      }
    }
    "not match when" >> {
      "no name is specified" >> {
        "deadbolt:dynamic:handler[bar]" match {
          case filter.dynamicComment(constraintName, name, handler) => false
          case _ => true
        }
      }
    }
  }

  "pattern should" >> {
    "match when" >> {
      "deadbolt:pattern:value[foo]:type[EQUALITY] is used" >> {
        "deadbolt:pattern:value[foo]:type[EQUALITY]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "EQUALITY".equals(patternType) && invert === null && handlerName === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[REGEX] is used" >> {
        "deadbolt:pattern:value[foo]:type[REGEX]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "REGEX".equals(patternType) && invert === null && handlerName === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[CUSTOM] is used" >> {
        "deadbolt:pattern:value[foo]:type[CUSTOM]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "CUSTOM".equals(patternType) && invert === null && handlerName === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[EQUALITY]:invert[true] is used" >> {
        "deadbolt:pattern:value[foo]:type[EQUALITY]:invert[true]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "EQUALITY".equals(patternType) && "true".equals(invert) && handlerName === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[REGEX]:invert[true] is used" >> {
        "deadbolt:pattern:value[foo]:type[REGEX]:invert[true]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "REGEX".equals(patternType) && "true".equals(invert) && handlerName === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[CUSTOM]:invert[true] is used" >> {
        "deadbolt:pattern:value[foo]:type[CUSTOM]:invert[true]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "CUSTOM".equals(patternType) && "true".equals(invert) && handlerName === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[EQUALITY]:invert[false] is used" >> {
        "deadbolt:pattern:value[foo]:type[EQUALITY]:invert[false]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "EQUALITY".equals(patternType) && "false".equals(invert) && handlerName === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[REGEX]:invert[false] is used" >> {
        "deadbolt:pattern:value[foo]:type[REGEX]:invert[false]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "REGEX".equals(patternType) && "false".equals(invert) && handlerName === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[CUSTOM]:invert[false] is used" >> {
        "deadbolt:pattern:value[foo]:type[CUSTOM]:invert[false]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "CUSTOM".equals(patternType) && "false".equals(invert) && handlerName === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[EQUALITY]:handler[bar] is used" >> {
        "deadbolt:pattern:value[foo]:type[EQUALITY]:handler[bar]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "EQUALITY".equals(patternType) && "bar".equals(handlerName) && invert === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[REGEX]:handler[bar] is used" >> {
        "deadbolt:pattern:value[foo]:type[REGEX]:handler[bar]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "REGEX".equals(patternType) && "bar".equals(handlerName) && invert === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[CUSTOM]:handler[bar] is used" >> {
        "deadbolt:pattern:value[foo]:type[CUSTOM]:handler[bar]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "CUSTOM".equals(patternType) && "bar".equals(handlerName) && invert === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[EQUALITY]:invert[true]:handler[bar] is used" >> {
        "deadbolt:pattern:value[foo]:type[EQUALITY]:invert[true]:handler[bar]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "EQUALITY".equals(patternType) && "bar".equals(handlerName) && "true".equals(invert) should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[REGEX]:invert[true]:handler[bar] is used" >> {
        "deadbolt:pattern:value[foo]:type[REGEX]:invert[true]:handler[bar]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "REGEX".equals(patternType) && "bar".equals(handlerName) && "true".equals(invert) should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:pattern:value[foo]:type[CUSTOM]:invert[true]:handler[bar] is used" >> {
        "deadbolt:pattern:value[foo]:type[CUSTOM]:invert[true]:handler[bar]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => "pattern".equals(constraintName) && "foo".equals(value) && "CUSTOM".equals(patternType) && "bar".equals(handlerName) && "true".equals(invert) should beTrue
          case _ => false should beTrue
        }
      }
    }
    "not match when" >> {
      "no value is specified" >> {
        "deadbolt:pattern:type[CUSTOM]:invert[true]:handler[bar] is used" >> {
          "deadbolt:pattern:type[CUSTOM]:invert[true]:handler[bar]" match {
            case filter.patternComment(constraintName, value, patternType, invert, handlerName) => false
            case _ => true
          }
        }
      }
      "no pattern type is specified" >> {
        "deadbolt:pattern:value[foo]:invert[true]:handler[bar] is used" >> {
          "deadbolt:pattern:value[foo]:invert[true]:handler[bar]" match {
            case filter.patternComment(constraintName, value, patternType, invert, handlerName) => false
            case _ => true
          }
        }
      }
      "an unknown pattern type is used" >> {
        "deadbolt:pattern:value[foo]:type[XCUSTOMX]:invert[true]:handler[bar]" match {
          case filter.patternComment(constraintName, value, patternType, invert, handlerName) => false
          case _ => true
        }
      }
    }
  }

  "composite should" >> {
    "match when" >> {
      "deadbolt:composite:name[foo] is used" >> {
        "deadbolt:composite:name[foo]" match {
          case filter.compositeComment(constraintName, name, handler) => "composite".equals(constraintName) && "foo".equals(name) && handler === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:composite:name[foo]:handler[bar] is used" >> {
        "deadbolt:composite:name[foo]:handler[bar]" match {
          case filter.compositeComment(constraintName, name, handler) => "composite".equals(constraintName) && "foo".equals(name) && "bar".equals(handler) should beTrue
          case _ => false should beTrue
        }
      }
    }
  }

  "restrict should" >> {
    "match when" >> {
      "deadbolt:restrict:name[foo] is used" >> {
        "deadbolt:restrict:name[foo]" match {
          case filter.restrictComment(constraintName, name, handler) => "restrict".equals(constraintName) && "foo".equals(name) && handler === null should beTrue
          case _ => false should beTrue
        }
      }
      "deadbolt:restrict:name[foo]:handler[bar] is used" >> {
        "deadbolt:restrict:name[foo]:handler[bar]" match {
          case filter.restrictComment(constraintName, name, handler) => "restrict".equals(constraintName) && "foo".equals(name) && "bar".equals(handler) should beTrue
          case _ => false should beTrue
        }
      }
    }
  }
}
