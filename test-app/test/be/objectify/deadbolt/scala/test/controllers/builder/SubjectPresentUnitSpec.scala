package be.objectify.deadbolt.scala.test.controllers.builder

import be.objectify.deadbolt.scala.ActionBuilders
import be.objectify.deadbolt.scala.test.controllers.{AbstractSubject, AbstractSubjectUnitSpec}
import play.api.inject._
import play.api.mvc.ControllerComponents

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object SubjectPresentUnitSpec extends AbstractSubjectUnitSpec {

  override def controller(injector: Injector): AbstractSubject = new Subject(injector.instanceOf[ActionBuilders], injector.instanceOf[ControllerComponents])
}
