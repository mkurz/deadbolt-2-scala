package be.objectify.deadbolt.scala.test.modules

import be.objectify.deadbolt.scala.TemplateFailureListener
import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.test.dao.SubjectDao
import be.objectify.deadbolt.scala.test.security.{MyCustomTemplateFailureListener, MyHandlerCache}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class CustomDeadboltHook extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =
    Seq(
         bind[HandlerCache].to[MyHandlerCache],
         bind[TemplateFailureListener].to[MyCustomTemplateFailureListener]
       )
}
