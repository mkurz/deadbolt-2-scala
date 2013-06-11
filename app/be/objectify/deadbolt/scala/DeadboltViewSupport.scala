package be.objectify.deadbolt.scala

import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.core.{PatternType, DeadboltAnalyzer}
import play.api.mvc.Request
import java.util.regex.Pattern
import play.cache.Cache
import java.util.concurrent.Callable

/**
 *
 * @author Steve Chaloner (steve@objectify.be)
 */

object  DeadboltViewSupport {
  /**
   * Used for restrict tags in the template.
   *
   * @param roles a list of String arrays.  Within an array, the roles are ANDed.  The arrays in the list are OR'd, so
   *              the first positive hit will allow access.
   * @param deadboltHandler application hook
   * @return true if the view can be accessed, otherwise false
   */
  def viewRestrict(roles: List[Array[String]],
                   deadboltHandler: DeadboltHandler,
                   request: Request[Any]): Boolean = {
    def check(subject: Subject, current: Array[String], remaining: List[Array[String]]): Boolean = {
      if (DeadboltAnalyzer.checkRole(subject, current)) true
      else if (remaining.isEmpty) false
      else check(subject, remaining.head, remaining.tail)
    }

    deadboltHandler.getSubject(request) match {
      case Some(subject) => {
        if (roles.headOption.isDefined) check(subject, roles.head, roles.tail)
        else false
      }
      case None => false
    }
  }

  /**
   * Used for dynamic tags in the template.
   *
   * @param name the name of the resource
   * @param meta meta information on the resource
   * @return true if the view can be accessed, otherwise false
   */
  def viewDynamic(name: String,
                  meta: String,
                  deadboltHandler: DeadboltHandler,
                  request: Request[Any]): Boolean = {
    val resourceHandler = deadboltHandler.getDynamicResourceHandler(request)
    if (resourceHandler.isDefined) resourceHandler.get.isAllowed(name, meta, deadboltHandler, request)
    else throw new RuntimeException("A dynamic resource is specified but no dynamic resource handler is provided")
  }

  /**
   *
   * @param value
   * @param patternType
   * @param deadboltHandler
   * @param request
   * @return
   */
  def viewPattern(value: String,
                  patternType: PatternType,
                  deadboltHandler: DeadboltHandler,
                  request: Request[Any]): Boolean = {
    def getPattern(patternValue: String): Pattern =
      Cache.getOrElse("Deadbolt." + patternValue,
                      new Callable[Pattern]{
                        def call() = Pattern.compile(patternValue)
                      },
                      0)

    val maySubject = deadboltHandler.getSubject(request)
    maySubject match {
      case None => false
      case Some(subject) => {
        patternType match {
          case PatternType.EQUALITY => DeadboltAnalyzer.checkPatternEquality(subject, value)
          case PatternType.REGEX => DeadboltAnalyzer.checkRegexPattern(subject, getPattern(value))
          case PatternType.CUSTOM => {
            deadboltHandler.getDynamicResourceHandler(request) match {
              case Some(dynamicHandler) => {
                if (dynamicHandler.checkPermission(value, deadboltHandler, request)) true
                else false
              }
              case None =>
                throw new RuntimeException("A custom pattern is specified but no dynamic resource handler is provided")
            }
          }
        }
      }
    }
  }
}