/*
 * Copyright 2012-2016 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.scala.composite

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala._
import be.objectify.deadbolt.scala.composite.Operators.Operator
import be.objectify.deadbolt.scala.models.PatternType

import _root_.scala.concurrent.Future

@Singleton
class CompositeConstraints @Inject()(logic: ConstraintLogic, ecProvider: ExecutionContextProvider) {

  val ec = ecProvider.get()

  private def allow[A](request: AuthenticatedRequest[A]): Future[Boolean] = Future.successful(true)

  private def deny[A](request: AuthenticatedRequest[A]): Future[Boolean] = Future.successful(false)

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
                        meta: Option[Any] = None) extends Constraint[A] {
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
                        meta: Option[Any] = None,
                        invert: Boolean = false) extends Constraint[A] {
    override def apply(authRequest: AuthenticatedRequest[A],
                       handler: DeadboltHandler): Future[Boolean] =
      logic.pattern(authRequest,
                    handler,
                    value,
                    patternType,
                    meta,
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
                       handler: DeadboltHandler): Future[Boolean] = Future.successful(false)
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
