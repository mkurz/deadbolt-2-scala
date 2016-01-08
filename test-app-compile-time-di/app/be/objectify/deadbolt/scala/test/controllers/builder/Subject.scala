package be.objectify.deadbolt.scala.test.controllers.builder

import scala.concurrent.ExecutionContext.Implicits.global
import be.objectify.deadbolt.scala.ActionBuilders
import play.api.mvc.Controller

import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Subject(actionBuilder: ActionBuilders) extends Controller {

  def subjectMustBePresent =
    actionBuilder.SubjectPresentAction().defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def subjectMustNotBePresent =
    actionBuilder.SubjectNotPresentAction().defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }
}
