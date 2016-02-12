package be.objectify.deadbolt.scala.models

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
trait Subject {

  def identifier: String

  def roles: List[Role]

  def permissions: List[Permission]
}
