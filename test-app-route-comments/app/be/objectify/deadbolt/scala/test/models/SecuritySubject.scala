package be.objectify.deadbolt.scala.test.models

import be.objectify.deadbolt.scala.models.{Permission, Role, Subject}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
case class SecuritySubject(identifier: String,
                           roles: List[_ <: Role],
                           permissions: List[_ <: Permission]) extends Subject
