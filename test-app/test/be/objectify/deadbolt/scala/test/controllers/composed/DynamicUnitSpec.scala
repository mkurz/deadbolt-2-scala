package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.scala.DeadboltActions
import be.objectify.deadbolt.scala.test.controllers.{AbstractDynamic, AbstractDynamicUnitSpec}
import play.api.inject.Injector

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object DynamicUnitSpec extends AbstractDynamicUnitSpec {
  override def controller(injector: Injector): AbstractDynamic = new Dynamic(injector.instanceOf[DeadboltActions])
}
