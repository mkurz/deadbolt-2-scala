package be.objectify.deadbolt.scala.test.controllers

import play.api.mvc.{Action, AnyContent}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
trait AbstractSubject {
  def subjectMustBePresent: Action[AnyContent]

  def subjectMustNotBePresent: Action[AnyContent]
}
