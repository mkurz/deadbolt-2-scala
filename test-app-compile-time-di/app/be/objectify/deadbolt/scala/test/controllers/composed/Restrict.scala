package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.scala.DeadboltActions
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class Restrict(deadbolt: DeadboltActions) extends Controller {

  def restrictedToFooAndBar =
    deadbolt.Restrict(roleGroups = List(Array("foo", "bar")))() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                                                                }

  def restrictedToFooOrBar =
    deadbolt.Restrict(roleGroups = List(Array("foo"), Array("bar")))() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                                                                       }

  def restrictedToFooAndNotBar =
    deadbolt.Restrict(roleGroups = List(Array("foo", "!bar")))() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                                                                 }

  def restrictedToFooOrNotBar =
    deadbolt.Restrict(roleGroups = List(Array("foo"), Array("!bar")))() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                                                                        }
}
