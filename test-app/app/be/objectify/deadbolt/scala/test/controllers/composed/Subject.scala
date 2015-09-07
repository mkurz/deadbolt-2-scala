package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.scala.DeadboltActions
import com.google.inject.Inject
import play.api.mvc.{Action, Controller}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Subject @Inject()(deadbolt: DeadboltActions) extends Controller {

  def subjectMustBePresent = deadbolt.SubjectPresent() {
    Action {
      Ok("Content accessible")
    }
  }

  def subjectMustNotBePresent =
    deadbolt.SubjectNotPresent() {
      Action {
        Ok("Content accessible")
      }
    }
}
