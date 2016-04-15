package be.objectify.deadbolt.scala.test.controllers

import be.objectify.deadbolt.scala.test.dao.{SubjectDao, TestSubjectDao}
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results
import play.api.test.PlaySpecification
import play.api.{Application, Mode}

abstract class AbstractUnitSpec extends PlaySpecification with Results {

  def testApp: Application =  new GuiceApplicationBuilder().in(Mode.Test).bindings(bind[SubjectDao].to[TestSubjectDao]).build()
}
