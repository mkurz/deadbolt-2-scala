package be.objectify.deadbolt.scala.composite

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala._
import be.objectify.deadbolt.scala.composite.Operators.Operator
import be.objectify.deadbolt.scala.models.PatternType

import _root_.scala.concurrent.Future

@Singleton
class CompositeConstraints @Inject()(logic: ConstraintLogic, ecProvider: ExecutionContextProvider) {

  val ec = ecProvider.get()

  private def allow[A](request: AuthenticatedRequest[A]): Future[Boolean] = Future {
                                                                                     true
                                                                                   }(ec)

  private def deny[A](request: AuthenticatedRequest[A]): Future[Boolean] = Future {
                                                                                    false
                                                                                  }(ec)

  case class Restrict[A](roleGroups: RoleGroups) extends Constraint[A] {
    override def apply(authRequest: AuthenticatedRequest[A],
                       handler: DeadboltHandler): Future[Boolean] =
      logic.restrict(authRequest,
                     handler,
                     roleGroups,
                     (ar: AuthenticatedRequest[A]) => allow(ar),
                     (ar: AuthenticatedRequest[A]) => deny(ar))
  }

  case class Dynamic[A](name: String,
                        meta: String) extends Constraint[A] {
    override def apply(authRequest: AuthenticatedRequest[A],
                       handler: DeadboltHandler): Future[Boolean] = {
      logic.dynamic(authRequest,
                    handler,
                    name,
                    meta,
                    (ar: AuthenticatedRequest[A]) => allow(ar),
                    (ar: AuthenticatedRequest[A]) => deny(ar))
    }
  }

  case class Pattern[A](value: String,
                        patternType: PatternType,
                        invert: Boolean = false) extends Constraint[A] {
    override def apply(authRequest: AuthenticatedRequest[A],
                       handler: DeadboltHandler): Future[Boolean] =
      logic.pattern(authRequest,
                    handler,
                    value,
                    patternType,
                    invert,
                    (ar: AuthenticatedRequest[A]) => allow(ar),
                    (ar: AuthenticatedRequest[A]) => deny(ar))
  }

  case class SubjectPresent[A]() extends Constraint[A] {
    override def apply(authRequest: AuthenticatedRequest[A],
                       handler: DeadboltHandler): Future[Boolean] =
      logic.subjectPresent(authRequest,
                           handler,
                           (ar: AuthenticatedRequest[A]) => allow(ar),
                           (ar: AuthenticatedRequest[A]) => deny(ar))
  }

  case class SubjectNotPresent[A]() extends Constraint[A] {
    override def apply(authRequest: AuthenticatedRequest[A],
                       handler: DeadboltHandler): Future[Boolean] =
      logic.subjectPresent(authRequest,
                           handler,
                           (ar: AuthenticatedRequest[A]) => deny(ar),
                           (ar: AuthenticatedRequest[A]) => allow(ar))
  }

  private case class Deny[A]() extends Constraint[A] {
    override def apply(authRequest: AuthenticatedRequest[A],
                       handler: DeadboltHandler): Future[Boolean] = Future {
                                                                             false
                                                                           }(ec)
  }

  case class ConstraintTree[A](operator: Operator[A],
                               constraints: List[Constraint[A]]) extends Constraint[A] {
    override def apply(authRequest: AuthenticatedRequest[A],
                       handler: DeadboltHandler): Future[Boolean] = {
      def combine(op: Operator[A],
                  constraint: Constraint[A],
                  remaining: List[Constraint[A]]): Constraint[A] = {
        if (remaining.isEmpty) constraint
        else combine(op, op(constraint, remaining.head), remaining.tail)
      }

      (if (constraints.isEmpty) new Deny[A]()
      else if (constraints.size == 1) constraints.head
      else combine(operator, constraints.head, constraints.tail)) (authRequest, handler)
    }
  }

}
