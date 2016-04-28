package be.objectify.deadbolt.scala.test.controllers.builder

import be.objectify.deadbolt.scala.ActionBuilders
import be.objectify.deadbolt.scala.test.controllers.AbstractSubject
import com.google.inject.Inject
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Subject @Inject()(actionBuilder: ActionBuilders) extends Controller with AbstractSubject {

  def subjectMustBePresent: Action[AnyContent] =
    actionBuilder.SubjectPresentAction().defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def subjectMustNotBePresent: Action[AnyContent] =
    actionBuilder.SubjectNotPresentAction().defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }
}
