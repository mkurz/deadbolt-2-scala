package be.objectify.deadbolt.scala

import play.api.mvc.{Action, Results, BodyParsers}
import play.cache.Cache
import be.objectify.deadbolt.core.{DeadboltAnalyzer, PatternType}
import be.objectify.deadbolt.core.models.Subject
import java.util.concurrent.Callable
import java.util.regex.Pattern


/**
 * Controller-level authorisations for Scala controllers.
 *
 * @author Steve Chaloner
 */
trait DeadboltActions extends Results with BodyParsers {

  /**
   * Restrict access to an action to users that have all the specified roles.
   *
   * @param roleNames
   * @param deadboltHandler
   * @param action
   * @tparam A
   * @return
   */
  def Restrict[A](roleNames: Array[String],
                  deadboltHandler: DeadboltHandler)(action: Action[A]): Action[A] = {
    Restrict[A](List(roleNames),
                deadboltHandler)(action)
  }

  /**
   * Restrict access to an action to users that have all the specified roles within a given group.  Each group, which is
   * an array of strings, is checked in turn.
   *
   * @param roleGroups
   * @param deadboltHandler
   * @param action
   * @tparam A
   * @return
   */
  def Restrict[A](roleGroups: List[Array[String]],
                  deadboltHandler: DeadboltHandler)
                  (action: Action[A]): Action[A] = {
    Action.async(action.parser) { implicit request =>

      def check(subject: Subject, current: Array[String], remaining: List[Array[String]]): Boolean = {
        if (DeadboltAnalyzer.checkRole(subject, current)) true
        else if (remaining.isEmpty) false
        else check(subject, remaining.head, remaining.tail)
      }

      deadboltHandler.beforeAuthCheck(request) match {
          case Some(result) => result
          case _ => {
            if (roleGroups.isEmpty) deadboltHandler.onAuthFailure(request)
            else {
              deadboltHandler.getSubject(request) match {
                case Some(subject) => {
                  if (check(subject, roleGroups.head, roleGroups.tail)) action(request)
                  else deadboltHandler.onAuthFailure(request)
                }
                case _ => deadboltHandler.onAuthFailure(request)
              }
            }
          }
        }
    }
  }

  /**
   *
   * @param name
   * @param meta
   * @param deadboltHandler
   * @param action
   * @tparam A
   * @return
   */
  def Dynamic[A](name: String,
                 meta: String = "",
                 deadboltHandler: DeadboltHandler)
                (action: Action[A]): Action[A] = {
    Action.async(action.parser) { implicit request =>
      deadboltHandler.beforeAuthCheck(request) match {
          case Some(result) => result
          case _ => {
            deadboltHandler.getDynamicResourceHandler(request) match {
              case Some(dynamicHandler) => {
                if (dynamicHandler.isAllowed(name, meta, deadboltHandler, request)) action(request)
                else deadboltHandler.onAuthFailure(request)
              }
              case None =>
                throw new RuntimeException("A dynamic resource is specified but no dynamic resource handler is provided")
            }
          }
        }
    }
  }

  /**
   *
   * @param value
   * @param patternType
   * @param deadboltHandler
   * @param action
   * @tparam A
   * @return
   */
  def Pattern[A](value: String,
                 patternType: PatternType,
                 deadboltHandler: DeadboltHandler)
                (action: Action[A]): Action[A] = {

    def getPattern(patternValue: String): Pattern =
      Cache.getOrElse("Deadbolt." + patternValue,
        new Callable[Pattern] {
          def call() = java.util.regex.Pattern.compile(patternValue)
        },
        0)

    Action.async(action.parser) {
      implicit request =>
        deadboltHandler.beforeAuthCheck(request) match {
          case Some(result) => result
          case _ => {
            val maySubject = deadboltHandler.getSubject(request)
            maySubject match {
              case None => deadboltHandler.onAuthFailure(request)
              case Some(subject) => {
                patternType match {
                  case PatternType.EQUALITY => {
                    if (DeadboltAnalyzer.checkPatternEquality(subject, value)) action(request)
                    else deadboltHandler.onAuthFailure(request)
                  }
                  case PatternType.REGEX => {
                    if (DeadboltAnalyzer.checkRegexPattern(subject, getPattern(value))) action(request)
                    else deadboltHandler.onAuthFailure(request)
                  }
                  case PatternType.CUSTOM => {
                    deadboltHandler.getDynamicResourceHandler(request) match {
                      case Some(dynamicHandler) => {
                        if (dynamicHandler.checkPermission(value, deadboltHandler, request)) action(request)
                        else deadboltHandler.onAuthFailure(request)
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
    }
  }

  /**
   * Denies access to the action if there is no subject present.
   *
   * @param deadboltHandler
   * @param action
   * @tparam A
   * @return
   */
  def SubjectPresent[A](deadboltHandler: DeadboltHandler)(action: Action[A]): Action[A] = {
    Action.async(action.parser) { implicit request =>
      deadboltHandler.beforeAuthCheck(request) match {
            case Some(result) => result
            case _ => {
              deadboltHandler.getSubject(request) match {
                case Some(handler) => action(request)
                case None => deadboltHandler.onAuthFailure(request)
              }
            }
          }
    }
  }

  /**
   * Denies access to the action if there is a subject present.
   *
   * @param deadboltHandler
   * @param action
   * @tparam A
   * @return
   */
  def SubjectNotPresent[A](deadboltHandler: DeadboltHandler)(action: Action[A]): Action[A] = {
    Action.async(action.parser) { implicit request =>
      deadboltHandler.beforeAuthCheck(request) match {
            case Some(result) => result
            case _ => {
              deadboltHandler.getSubject(request) match {
                case Some(subject) => deadboltHandler.onAuthFailure(request)
                case None => action(request)
              }
            }
          }
    }
  }
}
