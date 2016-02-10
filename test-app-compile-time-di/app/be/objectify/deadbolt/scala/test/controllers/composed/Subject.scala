package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.scala.DeadboltActions
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class Subject(deadbolt: DeadboltActions) extends Controller {

  def subjectMustBePresent = deadbolt.SubjectPresent()() { authRequest =>
    Future {
             Ok("Content accessible")
           }
                                                         }

  def subjectMustNotBePresent =
    deadbolt.SubjectNotPresent()() { authRequest =>
      Future {
               Ok("Content accessible")
             }
                                   }
}
