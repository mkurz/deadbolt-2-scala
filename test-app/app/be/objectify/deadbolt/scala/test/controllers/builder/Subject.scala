package be.objectify.deadbolt.scala.test.controllers.builder

import javax.inject.Inject

import be.objectify.deadbolt.scala.ActionBuilders
import be.objectify.deadbolt.scala.test.controllers.AbstractSubject
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Subject @Inject()(actionBuilder: ActionBuilders, controllerComponents: ControllerComponents) extends AbstractController(controllerComponents) with AbstractSubject {

  implicit val bodyParsers: PlayBodyParsers = controllerComponents.parsers

  def subjectMustBePresent: Action[AnyContent] =
    actionBuilder.SubjectPresentAction().defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def subjectMustNotBePresent: Action[AnyContent] =
    actionBuilder.SubjectNotPresentAction().defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }
}
