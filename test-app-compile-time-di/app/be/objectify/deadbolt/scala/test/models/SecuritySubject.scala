package be.objectify.deadbolt.scala.test.models

import java.util

import be.objectify.deadbolt.core.models.{Permission, Role, Subject}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
case class SecuritySubject(identifier: String,
                           roles: util.List[_ <: Role],
                           permissions: util.List[_ <: Permission]) extends Subject {
  override def getIdentifier: String = identifier

  override def getRoles: util.List[_ <: Role] = roles

  override def getPermissions: util.List[_ <: Permission] = permissions
}
