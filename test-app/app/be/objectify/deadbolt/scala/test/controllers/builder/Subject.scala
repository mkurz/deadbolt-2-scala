package be.objectify.deadbolt.scala.test.controllers.builder

import be.objectify.deadbolt.scala.ActionBuilders
import com.google.inject.Inject
import play.api.mvc.Controller

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Subject @Inject()(actionBuilder: ActionBuilders) extends Controller {

  def subjectMustBePresent =
    actionBuilder.SubjectPresentAction().defaultHandler() { authRequest =>
      Ok("Content accessible")
    }

  def subjectMustNotBePresent =
    actionBuilder.SubjectNotPresentAction().defaultHandler() { authRequest =>
      Ok("Content accessible")
    }
}
