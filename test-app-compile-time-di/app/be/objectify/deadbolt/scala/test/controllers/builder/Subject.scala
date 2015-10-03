package be.objectify.deadbolt.scala.test.controllers.builder

import be.objectify.deadbolt.scala.ActionBuilders
import play.api.mvc.Controller

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Subject(actionBuilder: ActionBuilders) extends Controller {

  def subjectMustBePresent =
    actionBuilder.SubjectPresentAction().defaultHandler() {
      Ok("Content accessible")
    }

  def subjectMustNotBePresent =
    actionBuilder.SubjectNotPresentAction().defaultHandler() {
      Ok("Content accessible")
    }
}
