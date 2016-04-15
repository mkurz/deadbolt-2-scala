package be.objectify.deadbolt.scala.test.controllers

import be.objectify.deadbolt.scala.ActionBuilders
import be.objectify.deadbolt.scala.models.PatternType
import com.google.inject.Inject
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
