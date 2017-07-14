package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.scala.DeadboltActions
import be.objectify.deadbolt.scala.test.controllers.AbstractSubject
import javax.inject.Inject

import play.api.mvc.{Action, AnyContent, AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Subject @Inject()(deadbolt: DeadboltActions, controllerComponents: ControllerComponents) extends AbstractController(controllerComponents) with AbstractSubject {

  def subjectMustBePresent: Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
    Future {
      Ok("Content accessible")
    }
  }

  def subjectMustNotBePresent: Action[AnyContent] =
    deadbolt.SubjectNotPresent()() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }
}
