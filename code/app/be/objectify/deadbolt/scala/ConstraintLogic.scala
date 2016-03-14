package be.objectify.deadbolt.scala

import javax.inject.{Singleton, Inject}

import be.objectify.deadbolt.scala.models.{PatternType, Subject}

import scala.concurrent.Future

@Singleton
class ConstraintLogic @Inject()(analyzer: StaticConstraintAnalyzer,
                                ecProvider: ExecutionContextProvider) {

  val ec = ecProvider.get()

  def restrict[A, B](authRequest: AuthenticatedRequest[A],
                     handler: DeadboltHandler,
                     roleGroups: RoleGroups,
                     pass: AuthenticatedRequest[A] => Future[B],
                     fail: AuthenticatedRequest[A] => Future[B]): Future[B] = {
    def check(subject: Option[Subject], current: RoleGroup, remaining: RoleGroups): Boolean = {
      if (analyzer.hasAllRoles(subject, current)) true
      else if (remaining.isEmpty) false
      else check(subject, remaining.head, remaining.tail)
    }

    if (roleGroups.isEmpty) fail(authRequest)
    else {
      handler.getSubject(authRequest).flatMap((subjectOption: Option[Subject]) =>
        subjectOption match {
          case Some(subject) =>
            val withSubject = AuthenticatedRequest(authRequest, subjectOption)
            if (check(subjectOption, roleGroups.head, roleGroups.tail)) pass(withSubject)
            else fail(withSubject)
          case _ => fail(authRequest)
        })(ec)
    }
  }

  def dynamic[A, B](authRequest: AuthenticatedRequest[A],
                    handler: DeadboltHandler,
                    name: String,
                    meta: String,
                    pass: AuthenticatedRequest[A] => Future[B],
                    fail: AuthenticatedRequest[A] => Future[B]): Future[B] =
    handler.getDynamicResourceHandler(authRequest).flatMap((drhOption: Option[DynamicResourceHandler]) => {
      drhOption match {
        case Some(dynamicHandler) =>
          handler.getSubject(authRequest).flatMap(subjectOption => {
            val maybeWithSubject = AuthenticatedRequest(authRequest, subjectOption)
            dynamicHandler.isAllowed(name, meta, handler, maybeWithSubject).flatMap((allowed: Boolean) => allowed match {
              case true => pass(maybeWithSubject)
              case false => fail(maybeWithSubject)
            })(ec)
          })(ec)
        case None =>
          throw new RuntimeException("A dynamic resource is specified but no dynamic resource handler is provided")
      }
    })(ec)

  def pattern[A, B](authRequest: AuthenticatedRequest[A],
                    handler: DeadboltHandler,
                    value: String,
                    patternType: PatternType,
                    invert: Boolean,
                    pass: AuthenticatedRequest[A] => Future[B],
                    fail: AuthenticatedRequest[A] => Future[B]): Future[B] =
    handler.getSubject(authRequest).flatMap((subjectOption: Option[Subject]) => subjectOption match {
      case None => fail(authRequest)
      case Some(subject) =>
        val withSubject = AuthenticatedRequest(authRequest, subjectOption)
        patternType match {
          case PatternType.EQUALITY =>
            val equal: Boolean = analyzer.checkPatternEquality(subjectOption, Option(value))
            if (if (invert) !equal else equal) pass(withSubject)
            else fail(withSubject)
          case PatternType.REGEX =>
            val patternMatch: Boolean = analyzer.checkRegexPattern(subjectOption, Option(value))
            if (if (invert) !patternMatch else patternMatch) pass(withSubject)
            else fail(withSubject)
          case PatternType.CUSTOM =>
            handler.getDynamicResourceHandler(authRequest).flatMap((drhOption: Option[DynamicResourceHandler]) => {
              drhOption match {
                case Some(dynamicHandler) =>
                  dynamicHandler.checkPermission(value, handler, authRequest).flatMap((allowed: Boolean) => {
                    (if (invert) !allowed else allowed) match {
                      case true => pass(withSubject)
                      case false => fail(withSubject)
                    }
                  })(ec)
                case None =>
                  throw new RuntimeException("A custom pattern is specified but no dynamic resource handler is provided")
              }
            })(ec)
        }
    })(ec)

  def subjectPresent[A, B](authRequest: AuthenticatedRequest[A],
                           handler: DeadboltHandler,
                           present: AuthenticatedRequest[A] => Future[B],
                           notPresent: AuthenticatedRequest[A] => Future[B]): Future[B] = {
    val subject1: Future[Option[Subject]] = handler.getSubject(authRequest)
    subject1.flatMap((subjectOption: Option[Subject]) => subjectOption match {
      case Some(subject) => present(AuthenticatedRequest(authRequest, subjectOption))
      case None => notPresent(AuthenticatedRequest(authRequest, subjectOption))
    })(ec)
  }
}
