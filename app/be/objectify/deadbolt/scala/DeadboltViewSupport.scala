package be.objectify.deadbolt.scala

import java.util.concurrent.Callable
import java.util.regex.Pattern

import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.core.{DeadboltAnalyzer, PatternType}
import play.api.mvc.Request
import play.cache.Cache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{duration, Await, Future}
import scala.concurrent.duration.Duration

/**
 *
 * @author Steve Chaloner (steve@objectify.be)
 */

object  DeadboltViewSupport {

  /**
   * Used for subjectPresent and subjectNotPresent tags in the template.
   *
   * @param deadboltHandler application hook
   * @return true if the view can be accessed, otherwise false
   */
  def viewSubjectPresent(deadboltHandler: DeadboltHandler,
                         request: Request[Any]): Boolean = {
    Await.result(deadboltHandler.getSubject(request).flatMap(subjectOption => subjectOption match {
      case Some(subject) => Future(true)
      case None => Future(false)
    }), Duration(1000, duration.MILLISECONDS))
  }

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


    Await.result(deadboltHandler.getSubject(request).flatMap(subjectOption => subjectOption match {
      case Some(subject) => Future(check(subject, roles.head, roles.tail))
      case None => Future(false)
    }), Duration(1000, duration.MILLISECONDS))
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

    val subjectFuture: Future[Option[Subject]] = deadboltHandler.getSubject(request)

    Await.result(subjectFuture.flatMap((subjectOption: Option[Subject]) =>
      subjectOption match {
        case None => Future(false)
        case Some(subject) => patternType match {
          case PatternType.EQUALITY => Future(DeadboltAnalyzer.checkPatternEquality(subject, value))
          case PatternType.REGEX => Future(DeadboltAnalyzer.checkRegexPattern(subject, getPattern(value)))
          case PatternType.CUSTOM => deadboltHandler.getDynamicResourceHandler(request) match {
            case Some(dynamicHandler) =>
              if (dynamicHandler.checkPermission(value, deadboltHandler, request)) Future(true)
              else Future(false)
            case None =>
              throw new RuntimeException("A custom pattern is specified but no dynamic resource handler is provided")
          }
          case _ => Future(false)
        }
      }), Duration(1000, duration.MILLISECONDS))
  }
}