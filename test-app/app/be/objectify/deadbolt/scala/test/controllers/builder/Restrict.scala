package be.objectify.deadbolt.scala.test.controllers.builder

import javax.inject.Inject

import be.objectify.deadbolt.scala.test.controllers.AbstractRestrict
import be.objectify.deadbolt.scala.{ActionBuilders, allOf, anyOf}
import play.api.mvc.{AbstractController, ControllerComponents, PlayBodyParsers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class Restrict @Inject()(actionBuilder: ActionBuilders, controllerComponents: ControllerComponents) extends AbstractController(controllerComponents) with AbstractRestrict {

  implicit val bodyParsers: PlayBodyParsers = controllerComponents.parsers

  def restrictedToFooAndBar =
    actionBuilder.RestrictAction("foo", "bar")
    .defaultHandler() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                      }

  def restrictedToFooOrBar =
    actionBuilder.RestrictAction(roles = anyOf(allOf("foo"), allOf("bar")))
    .defaultHandler() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                      }

  def restrictedToFooAndNotBar =
    actionBuilder.RestrictAction("foo", "!bar")
    .defaultHandler() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                      }

  def restrictedToFooOrNotBar =
    actionBuilder.RestrictAction(roles = anyOf(allOf("foo"), allOf("!bar")))
    .defaultHandler() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                      }
}
