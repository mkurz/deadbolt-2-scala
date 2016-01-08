package be.objectify.deadbolt.scala.test.controllers.builder

import be.objectify.deadbolt.scala.ActionBuilders
import com.google.inject.Inject
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Restrict @Inject()(actionBuilder: ActionBuilders) extends Controller {

  def restrictedToFooAndBar =
    actionBuilder.RestrictAction("foo", "bar")
    .defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def restrictedToFooOrBar =
    actionBuilder.RestrictAction(roles = List(Array("foo"), Array("bar")))
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
    actionBuilder.RestrictAction(roles = List(Array("foo"), Array("!bar")))
    .defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }
}
