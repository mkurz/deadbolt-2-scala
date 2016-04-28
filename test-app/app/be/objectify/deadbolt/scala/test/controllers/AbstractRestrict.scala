package be.objectify.deadbolt.scala.test.controllers

import be.objectify.deadbolt.scala.{DeadboltActions, allOf, allOfGroup, anyOf}
import com.google.inject.Inject
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
trait AbstractRestrict {

  def restrictedToFooAndBar: Action[AnyContent]

  def restrictedToFooOrBar: Action[AnyContent]

  def restrictedToFooAndNotBar: Action[AnyContent]

  def restrictedToFooOrNotBar: Action[AnyContent]
}
