package be.objectify.deadbolt.scala

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.{Action, Results, BodyParsers, BodyParser, Result}
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
                  deadboltHandler: DeadboltHandler)
                 (bodyParser: BodyParser[A])
                 (block: AuthenticatedRequest[A] => Result): Action[A] = {
    Restrict[A](List(roleNames),
                deadboltHandler)(bodyParser)(block)
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
                 (bodyParser: BodyParser[A])
                 (block: AuthenticatedRequest[A] => Result): Action[A] = {
    SubjectActionBuilder(None).async(bodyParser){ authRequest =>

      def check(subject: Subject, current: Array[String], remaining: List[Array[String]]): Boolean = {
        if (DeadboltAnalyzer.checkRole(subject, current)) true
        else if (remaining.isEmpty) false
        else check(subject, remaining.head, remaining.tail)
      }

      deadboltHandler.beforeAuthCheck(authRequest) match {
          case Some(result) => result
          case _ => {
            if (roleGroups.isEmpty) deadboltHandler.onAuthFailure(authRequest)
            else {
              deadboltHandler.getSubject(authRequest) match {
                case Some(subject) => {
                  if (check(subject, roleGroups.head, roleGroups.tail))
                    Future.successful( block( AuthenticatedRequest(authRequest, Some(subject)) ) )
                  else deadboltHandler.onAuthFailure(authRequest)
                }
                case _ => deadboltHandler.onAuthFailure(authRequest)
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
                (bodyParser: BodyParser[A])
                (block: AuthenticatedRequest[A] => Result): Action[A] = {
    SubjectActionBuilder(None).async(bodyParser) { authRequest =>
      deadboltHandler.beforeAuthCheck(authRequest) match {
          case Some(result) => result
          case _ => {
            deadboltHandler.getDynamicResourceHandler(authRequest) match {
              case Some(dynamicHandler) => {
                if (dynamicHandler.isAllowed(name, meta, deadboltHandler, authRequest))
                  Future.successful( block(authRequest) )
                else deadboltHandler.onAuthFailure(authRequest)
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
                (bodyParser: BodyParser[A])
                (block: AuthenticatedRequest[A] => Result): Action[A] = {

    def getPattern(patternValue: String): Pattern =
      Cache.getOrElse("Deadbolt." + patternValue,
        new Callable[Pattern] {
          def call() = java.util.regex.Pattern.compile(patternValue)
        },
        0)

    SubjectActionBuilder(None).async(bodyParser) {
      implicit authRequest =>
        deadboltHandler.beforeAuthCheck(authRequest) match {
          case Some(result) => result
          case _ => {
            deadboltHandler.getSubject(authRequest) match {
              case None => deadboltHandler.onAuthFailure(authRequest)
              case Some(subject) => {
                patternType match {
                  case PatternType.EQUALITY => {
                    if (DeadboltAnalyzer.checkPatternEquality(subject, value)) Future.successful(block(authRequest))
                    else deadboltHandler.onAuthFailure(authRequest)
                  }
                  case PatternType.REGEX => {
                    if (DeadboltAnalyzer.checkRegexPattern(subject, getPattern(value))) Future.successful(block(authRequest))
                    else deadboltHandler.onAuthFailure(authRequest)
                  }
                  case PatternType.CUSTOM => {
                    deadboltHandler.getDynamicResourceHandler(authRequest) match {
                      case Some(dynamicHandler) => {
                        if (dynamicHandler.checkPermission(value, deadboltHandler, authRequest)) Future.successful(block(authRequest))
                        else deadboltHandler.onAuthFailure(authRequest)
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
  def SubjectPresent[A](deadboltHandler: DeadboltHandler)
                       (bodyParser: BodyParser[A])
                       (block: AuthenticatedRequest[A] => Result): Action[A] = {
    SubjectActionBuilder(None).async(bodyParser) { authRequest =>
      deadboltHandler.beforeAuthCheck(authRequest) match {
            case Some(result) => result
            case _ => {
              deadboltHandler.getSubject(authRequest) match {
                case Some(subject) => Future.successful(block( AuthenticatedRequest(authRequest, Some(subject)) ))
                case None => deadboltHandler.onAuthFailure(authRequest)
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
  def SubjectNotPresent[A](deadboltHandler: DeadboltHandler)
                          (bodyParser: BodyParser[A])
                          (block: AuthenticatedRequest[A] => Result): Action[A] = {
    SubjectActionBuilder(None).async(bodyParser) { authRequest =>
      deadboltHandler.beforeAuthCheck(authRequest) match {
            case Some(result) => result
            case _ => {
              deadboltHandler.getSubject(authRequest) match {
                case Some(subject) => deadboltHandler.onAuthFailure(authRequest)
                case None => Future.successful( block(authRequest) )
              }
            }
          }
    }
  }
}
