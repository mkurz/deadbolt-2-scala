package be.objectify.deadbolt.scala

import scala.concurrent.Future

package object composite {
  type Constraint[A] = (AuthenticatedRequest[A], DeadboltHandler) => Future[Boolean]
}
