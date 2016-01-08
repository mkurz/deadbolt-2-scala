package be.objectify.deadbolt.scala.test.controllers.builder

import javax.inject.Inject

import be.objectify.deadbolt.scala.ActionBuilders
import play.api.mvc.Controller

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Dynamic @Inject()(actionBuilder: ActionBuilders) extends Controller {

  def index =
    actionBuilder.DynamicAction(name = "niceName")
    .defaultHandler() { authRequest =>
      Ok("Content accessible")
    }
}
