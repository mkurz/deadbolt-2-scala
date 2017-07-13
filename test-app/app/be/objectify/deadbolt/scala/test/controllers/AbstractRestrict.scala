package be.objectify.deadbolt.scala.test.controllers

import play.api.mvc.{Action, AnyContent}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
trait AbstractRestrict {

  def restrictedToFooAndBar: Action[AnyContent]

  def restrictedToFooOrBar: Action[AnyContent]

  def restrictedToFooAndNotBar: Action[AnyContent]

  def restrictedToFooOrNotBar: Action[AnyContent]
}
