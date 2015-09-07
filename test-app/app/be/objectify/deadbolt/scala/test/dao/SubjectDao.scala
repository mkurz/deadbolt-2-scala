package be.objectify.deadbolt.scala.test.dao

import java.util.Collections

import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.test.models.SecuritySubject

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
trait SubjectDao {
  def user(userName: String): Option[Subject]
}
