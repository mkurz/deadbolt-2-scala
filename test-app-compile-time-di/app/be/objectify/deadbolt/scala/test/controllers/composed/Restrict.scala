package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.scala.{DeadboltActions, anyOf, allOf, allOfGroup}
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class Restrict(deadbolt: DeadboltActions) extends Controller {

  def restrictedToFooAndBar =
    deadbolt.Restrict(roleGroups = allOfGroup("foo", "bar"))() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                                                                }

  def restrictedToFooOrBar =
    deadbolt.Restrict(roleGroups = anyOf(allOf("foo"), allOf("bar")))() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                                                                       }

  def restrictedToFooAndNotBar =
    deadbolt.Restrict(roleGroups = allOfGroup("foo", "!bar"))() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                                                                 }

  def restrictedToFooOrNotBar =
    deadbolt.Restrict(roleGroups = anyOf(allOf("foo"), allOf("!bar")))() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                                                                        }
}
