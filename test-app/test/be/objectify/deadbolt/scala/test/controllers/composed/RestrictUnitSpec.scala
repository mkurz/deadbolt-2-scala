package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.scala.DeadboltActions
import be.objectify.deadbolt.scala.test.controllers.{AbstractRestrict, AbstractRestrictUnitSpec}
import play.api.inject.Injector

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object RestrictUnitSpec extends AbstractRestrictUnitSpec {
  override def controller(injector: Injector): AbstractRestrict = new Restrict(injector.instanceOf[DeadboltActions])
}
