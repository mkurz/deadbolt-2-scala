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
          if (passed) c2(request, handler) else Future {false}(ec))(ec)
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
