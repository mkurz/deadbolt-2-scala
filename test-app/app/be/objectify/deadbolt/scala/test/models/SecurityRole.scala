package be.objectify.deadbolt.scala.test.models

import be.objectify.deadbolt.core.models.Role

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
case class SecurityRole(name: String) extends Role {
  override def getName: String = name
}
