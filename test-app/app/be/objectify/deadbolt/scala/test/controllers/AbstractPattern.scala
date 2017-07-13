package be.objectify.deadbolt.scala.test.controllers

import play.api.mvc.{Action, AnyContent}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
trait AbstractPattern {

  def custom: Action[AnyContent]

  def invertedCustom: Action[AnyContent]

  def equality: Action[AnyContent]

  def invertedEquality: Action[AnyContent]

  def regex_zombieKillersOnly: Action[AnyContent]

  def invertedRegex_zombieKillersOnly: Action[AnyContent]

  def regex_anyKillersOfTheUndeadWelcome: Action[AnyContent]

  def invertedRegex_anyKillersOfTheUndeadWelcome: Action[AnyContent]
}
