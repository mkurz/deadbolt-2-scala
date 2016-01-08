package be.objectify.deadbolt.scala.test.controllers.builder

import be.objectify.deadbolt.scala.ActionBuilders
import play.api.mvc.Controller

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Dynamic(actionBuilder: ActionBuilders) extends Controller {

  def index =
    actionBuilder.DynamicAction(name = "niceName")
    .defaultHandler() { authRequest =>
      Ok("Content accessible")
    }
}
