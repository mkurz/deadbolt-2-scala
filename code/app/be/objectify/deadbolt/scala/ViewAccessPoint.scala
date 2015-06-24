package be.objectify.deadbolt.scala

import be.objectify.deadbolt.scala.cache.HandlerCache
import play.api.{Application, Play}

/**
 * We can't inject into views, or objects, so an injector look-up and some implicits to the rescue...
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
object ViewAccessPoint {
  private[deadbolt] val viewStuff = Application.instanceCache[ViewSupport]
  private[deadbolt] val handlerStuff = Application.instanceCache[HandlerCache]

  object Implicits {
    implicit def viewSupport(implicit application: Application): ViewSupport = viewStuff(application)
    implicit def handlerCache(implicit application: Application): HandlerCache = handlerStuff(application)
  }
}
