package be.objectify.deadbolt.scala

import play.api.Play

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
object ViewAccessPoint {

  val viewSupport: ViewSupport = Play.current.injector.instanceOf[ViewSupport]
}
