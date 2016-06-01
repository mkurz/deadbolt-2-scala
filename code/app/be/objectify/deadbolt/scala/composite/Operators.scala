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

import be.objectify.deadbolt.scala.{DeadboltHandler, AuthenticatedRequest}

import scala.concurrent.{ExecutionContext, Future}

object Operators {

  sealed trait Operator[A] extends ((Constraint[A], Constraint[A]) => Constraint[A])

  case class &&[A](ec: ExecutionContext) extends Operator[A] {
    def apply(c1: Constraint[A], c2: Constraint[A]): Constraint[A] =
      (request: AuthenticatedRequest[A],
       handler: DeadboltHandler) =>
        c1(request, handler).flatMap((passed: Boolean) =>
          if (passed) c2(request, handler) else Future.successful(false))(ec)
  }

  case class ||[A](ec: ExecutionContext) extends Operator[A] {
    def apply(c1: Constraint[A], c2: Constraint[A]): Constraint[A] = {
      implicit val context = ec
      (request: AuthenticatedRequest[A],
       handler: DeadboltHandler) =>
        (for {
          passed1 <- c1(request, handler)
          passed2 <- c2(request, handler)
        } yield passed1 || passed2)(ec)
    }
  }
}
