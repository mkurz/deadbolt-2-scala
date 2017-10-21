package be.objectify.deadbolt.scala.test.controllers

import be.objectify.deadbolt.scala.ActionBuilders
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class App extends Controller {

  def index = Action.async {
    Future {
      Ok("Content accessible")
    }
  }
}
