package be.objectify.deadbolt.scala.test.controllers

import be.objectify.deadbolt.scala.DeadboltActions
import be.objectify.deadbolt.scala.test.controllers.composed.Subject
import be.objectify.deadbolt.scala.test.dao.{SubjectDao, TestSubjectDao}
import play.api.Mode
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Result, Results}
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

import scala.concurrent.Future

object SubjectPresentUnitSpec extends PlaySpecification with Results {
  "subjectMustBePresent " should {
    "should result in a 401 when no subject is present" in new WithApplication(new GuiceApplicationBuilder().in(Mode.Test).bindings(bind[SubjectDao].to[TestSubjectDao]).build()) {
      val deadbolt: DeadboltActions = implicitApp.injector.instanceOf[DeadboltActions]
      val controller = new Subject(deadbolt)
      val result: Future[Result] = call(controller.subjectMustBePresent(), FakeRequest())
      val statusCode: Int = status(result)
      statusCode must be equalTo 401
    }

    "should result in a 200 when a subject is present" in new WithApplication(new GuiceApplicationBuilder().in(Mode.Test).bindings(bind[SubjectDao].to[TestSubjectDao]).build()) {
      val deadbolt: DeadboltActions = implicitApp.injector.instanceOf[DeadboltActions]
      val controller = new Subject(deadbolt)
      val result: Future[Result] = call(controller.subjectMustBePresent(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
      val statusCode: Int = status(result)
      statusCode must be equalTo 200
    }
  }

  "subjectMustNotBePresent" should {
    "should result in a 200 when no subject is present" in new WithApplication(new GuiceApplicationBuilder().in(Mode.Test).bindings(bind[SubjectDao].to[TestSubjectDao]).build()) {
      val deadbolt: DeadboltActions = implicitApp.injector.instanceOf[DeadboltActions]
      val controller = new Subject(deadbolt)
      val result: Future[Result] = call(controller.subjectMustNotBePresent(), FakeRequest())
      val statusCode: Int = status(result)
      statusCode must be equalTo 200
    }

    "should result in a 401 when a subject is present" in new WithApplication(new GuiceApplicationBuilder().in(Mode.Test).bindings(bind[SubjectDao].to[TestSubjectDao]).build()) {
      val deadbolt: DeadboltActions = implicitApp.injector.instanceOf[DeadboltActions]
      val controller = new Subject(deadbolt)
      val result: Future[Result] = call(controller.subjectMustNotBePresent(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
      val statusCode: Int = status(result)
      statusCode must be equalTo 401
    }
  }
}
