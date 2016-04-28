package be.objectify.deadbolt.scala.test.controllers.builder

import be.objectify.deadbolt.scala.ActionBuilders
import be.objectify.deadbolt.scala.test.controllers.{AbstractPattern, AbstractPatternUnitSpec}
import play.api.inject.Injector

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object PatternUnitSpec extends AbstractPatternUnitSpec {
  override def controller(injector: Injector): AbstractPattern = new Pattern(injector.instanceOf[ActionBuilders])
}
