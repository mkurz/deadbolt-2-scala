package be.objectify.deadbolt.scala.testhelpers

import be.objectify.deadbolt.core.models.Permission

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
case class SecurityPermission(value: String) extends Permission {
  override def getValue: String = value
}
