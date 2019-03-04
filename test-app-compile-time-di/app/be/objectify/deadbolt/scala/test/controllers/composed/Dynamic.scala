package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.scala.DeadboltActions
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Dynamic(deadbolt: DeadboltActions, components: ControllerComponents) extends AbstractController(components) {

  def index =
    deadbolt.Dynamic(name = "niceName")() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }
}
