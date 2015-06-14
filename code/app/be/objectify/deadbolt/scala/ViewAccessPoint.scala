package be.objectify.deadbolt.scala

import be.objectify.deadbolt.scala.cache.HandlerCache
import play.api.Play

/**
 * We can't inject into views, or objects...so it's back to a static reference here
 * Todo - This is also the point that's causing the tests to fail, needs writng up as a bug
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
object ViewAccessPoint {
  val viewSupport: ViewSupport = Play.current.injector.instanceOf[ViewSupport]
  val handlers: HandlerCache = Play.current.injector.instanceOf[HandlerCache]
}
