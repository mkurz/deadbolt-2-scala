package be.objectify.deadbolt.scala

import javax.inject.{Inject, Singleton}

import play.api.{Configuration, Environment}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
class HandlerCache @Inject()(configuration: Configuration,
                             environment: Environment) extends Function[String, DeadboltHandler]
                                                               with Function0[DeadboltHandler] {

  override def apply(handlerKey: String): DeadboltHandler = ???

  override def apply(): DeadboltHandler = ???
}
