package be.objectify.deadbolt.scala

import java.util.Optional
import java.util.concurrent.Callable
import java.util.regex.Pattern

import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.core.{DeadboltAnalyzer, PatternType}
import play.api.mvc.{Action, BodyParsers, Result, Results}
import play.cache.Cache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


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

      def check(analyzer: DeadboltAnalyzer, subject: Optional[Subject], current: Array[String], remaining: List[Array[String]]): Future[Result] = {
        if (analyzer.checkRole(subject, current)) action(request)
        else if (remaining.isEmpty) deadboltHandler.onAuthFailure(request)
        else check(analyzer, subject, remaining.head, remaining.tail)
      }

      deadboltHandler.beforeAuthCheck(request).flatMap((beforeAuthOption: Option[Result]) => {
        beforeAuthOption match {
          case Some(result) => Future(result)
          case None =>
            if (roleGroups.isEmpty) deadboltHandler.onAuthFailure(request)
            else {
              deadboltHandler.getSubject(request).flatMap((subjectOption: Option[Subject]) =>
                subjectOption match {
                  case Some(subject) => check(new DeadboltAnalyzer(), Optional.ofNullable(subject), roleGroups.head, roleGroups.tail)
                  case _ => deadboltHandler.onAuthFailure(request)
                })
            }
        }
      })
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

      deadboltHandler.beforeAuthCheck(request).flatMap((beforeAuthOption: Option[Result]) => {
        beforeAuthOption match {
          case Some(result) => Future(result)
          case None =>
            deadboltHandler.getDynamicResourceHandler(request).flatMap((drhOption: Option[DynamicResourceHandler]) => {
              drhOption match {
                case Some(dynamicHandler) =>
                  dynamicHandler.isAllowed(name, meta, deadboltHandler, request).flatMap((allowed: Boolean) => allowed match {
                    case true => action(request)
                    case false => deadboltHandler.onAuthFailure(request)
                  })
                case None =>
                  throw new RuntimeException("A dynamic resource is specified but no dynamic resource handler is provided")
              }
            })
        }
      })
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
        deadboltHandler.beforeAuthCheck(request).flatMap((beforeAuthOption: Option[Result]) => {
          beforeAuthOption match {
            case Some(result) => Future(result)
            case None =>
              deadboltHandler.getSubject(request).flatMap((subjectOption: Option[Subject]) => subjectOption match {
                case None => deadboltHandler.onAuthFailure(request)
                case Some(subject) => patternType match {
                  case PatternType.EQUALITY =>
                    if (new DeadboltAnalyzer().checkPatternEquality(Optional.ofNullable(subject), Optional.ofNullable(value))) action(request)
                    else deadboltHandler.onAuthFailure(request)
                  case PatternType.REGEX =>
                    if (new DeadboltAnalyzer().checkRegexPattern(Optional.ofNullable(subject), Optional.ofNullable(getPattern(value)))) action(request)
                    else deadboltHandler.onAuthFailure(request)
                  case PatternType.CUSTOM =>
                    deadboltHandler.getDynamicResourceHandler(request).flatMap((drhOption: Option[DynamicResourceHandler]) => {
                      drhOption match {
                        case Some(dynamicHandler) =>
                          dynamicHandler.checkPermission(value, deadboltHandler, request).flatMap((allowed: Boolean) => {
                            allowed match {
                              case true => action(request)
                              case false => deadboltHandler.onAuthFailure(request)
                            }
                          })
                        case None =>
                          throw new RuntimeException("A custom pattern is specified but no dynamic resource handler is provided")
                      }
                    })
                }
              })
          }
        })
    }
  }

  /**
   * Allows access to the action if there is a subject present.
   *
   * @param deadboltHandler
   * @param action
   * @tparam A
   * @return
   */
  def SubjectPresent[A](deadboltHandler: DeadboltHandler)(action: Action[A]): Action[A] = {
    Action.async(action.parser) { implicit request =>
      deadboltHandler.beforeAuthCheck(request).flatMap((beforeAuthOption: Option[Result]) =>
          beforeAuthOption match {
            case Some(result) => Future(result)
            case None =>
              deadboltHandler.getSubject(request).flatMap((subjectOption: Option[Subject]) => subjectOption match {
                case Some(subject) => action(request)
                case None => deadboltHandler.onAuthFailure(request)
              })
          }
      )
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
      deadboltHandler.beforeAuthCheck(request).flatMap((beforeAuthOption: Option[Result]) =>
          beforeAuthOption match {
            case Some(result) => Future(result)
            case None =>
              deadboltHandler.getSubject(request).flatMap((subjectOption: Option[Subject]) => subjectOption match {
                case Some(subject) => deadboltHandler.onAuthFailure(request)
                case None => action(request)
              })

          }
      )
    }
  }
}
