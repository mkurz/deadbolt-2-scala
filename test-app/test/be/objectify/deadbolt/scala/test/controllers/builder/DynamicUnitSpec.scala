package be.objectify.deadbolt.scala.test.controllers.builder

import be.objectify.deadbolt.scala.ActionBuilders
import be.objectify.deadbolt.scala.test.controllers.{AbstractDynamic, AbstractDynamicUnitSpec}
import play.api.inject.Injector
import play.api.mvc.ControllerComponents

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object DynamicUnitSpec extends AbstractDynamicUnitSpec {
  override def controller(injector: Injector): AbstractDynamic = new Dynamic(injector.instanceOf[ActionBuilders], injector.instanceOf[ControllerComponents])
}
