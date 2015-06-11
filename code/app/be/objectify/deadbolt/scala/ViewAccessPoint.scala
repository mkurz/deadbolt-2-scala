package be.objectify.deadbolt.scala

import be.objectify.deadbolt.scala.cache.HandlerCache
import play.api.Play

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
object ViewAccessPoint {

  val viewSupport: ViewSupport = Play.current.injector.instanceOf[ViewSupport]
  val handlers: HandlerCache = Play.current.injector.instanceOf[HandlerCache]
}
