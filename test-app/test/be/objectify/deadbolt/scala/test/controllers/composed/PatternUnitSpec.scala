package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.scala.DeadboltActions
import be.objectify.deadbolt.scala.test.controllers.{AbstractPattern, AbstractPatternUnitSpec}
import play.api.inject.Injector

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object PatternUnitSpec extends AbstractPatternUnitSpec {
  override def controller(injector: Injector): AbstractPattern = new Pattern(injector.instanceOf[DeadboltActions])
}
