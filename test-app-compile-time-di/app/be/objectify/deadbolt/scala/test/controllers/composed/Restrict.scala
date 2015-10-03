package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.scala.DeadboltActions
import play.api.mvc.{Action, Controller}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Restrict(deadbolt: DeadboltActions) extends Controller {

  def restrictedToFooAndBar =
    deadbolt.Restrict(roleGroups = List(Array("foo", "bar"))) {
      Action {
        Ok("Content accessible")
      }
    }

  def restrictedToFooOrBar =
    deadbolt.Restrict(roleGroups = List(Array("foo"), Array("bar"))) {
      Action {
        Ok("Content accessible")
      }
    }

  def restrictedToFooAndNotBar =
    deadbolt.Restrict(roleGroups = List(Array("foo", "!bar"))) {
      Action {
        Ok("Content accessible")
      }
    }

  def restrictedToFooOrNotBar =
    deadbolt.Restrict(roleGroups = List(Array("foo"), Array("!bar"))) {
      Action {
        Ok("Content accessible")
      }
    }
}
