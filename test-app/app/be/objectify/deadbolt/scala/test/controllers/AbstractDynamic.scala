package be.objectify.deadbolt.scala.test.controllers

import play.api.mvc.{Action, AnyContent}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
trait AbstractDynamic {
  def index: Action[AnyContent]
}
