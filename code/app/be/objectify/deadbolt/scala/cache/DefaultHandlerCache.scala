package be.objectify.deadbolt.scala.cache

import be.objectify.deadbolt.scala.{HandlerKey, DeadboltHandler}

/**
 * Basic implementation of [[HandlerCache]]
 * @author Steve Chaloner (steve@objectify.be)
 */
class DefaultHandlerCache(handlers: Map[HandlerKey, DeadboltHandler], defaultHandlerKey: HandlerKey) extends HandlerCache {

  override def apply(): DeadboltHandler = handlers(defaultHandlerKey)

  override def apply(handlerKey: HandlerKey): DeadboltHandler = handlers(handlerKey)
}
