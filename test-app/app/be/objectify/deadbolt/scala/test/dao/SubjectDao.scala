package be.objectify.deadbolt.scala.test.dao

import be.objectify.deadbolt.scala.models.Subject

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
trait SubjectDao {
  def user(userName: String): Option[Subject]
}
