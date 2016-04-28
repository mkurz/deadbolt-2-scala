package be.objectify.deadbolt.scala.test.controllers.builder

import be.objectify.deadbolt.scala.test.controllers.AbstractRestrict
import be.objectify.deadbolt.scala.{ActionBuilders, allOf, allOfGroup, anyOf}
import com.google.inject.Inject
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class Restrict @Inject()(actionBuilder: ActionBuilders) extends Controller with AbstractRestrict {

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
