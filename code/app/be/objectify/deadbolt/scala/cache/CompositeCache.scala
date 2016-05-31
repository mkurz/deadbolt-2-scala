package be.objectify.deadbolt.scala.cache

import be.objectify.deadbolt.scala.composite._
import play.api.mvc.AnyContent

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
trait CompositeCache extends Function[String, Constraint[AnyContent]] {

  def register(name: String, constraint: Constraint[AnyContent])
}
