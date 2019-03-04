package be.objectify.deadbolt.scala.test.controllers.builder

import scala.concurrent.ExecutionContext.Implicits.global
import be.objectify.deadbolt.scala.ActionBuilders
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Dynamic(actionBuilder: ActionBuilders, components: ControllerComponents) extends AbstractController(components) {

  def index =
    actionBuilder.DynamicAction(name = "niceName")
    .defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }
}
