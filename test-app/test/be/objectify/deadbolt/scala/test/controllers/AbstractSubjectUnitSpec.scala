package be.objectify.deadbolt.scala.test.controllers

import play.api.inject._
import play.api.mvc.Result
import play.api.test.{FakeRequest, WithApplication}

import scala.concurrent.Future

abstract class AbstractSubjectUnitSpec extends AbstractUnitSpec {
  "subjectMustBePresent " should {
    "should result in a 401 when no subject is present" in new WithApplication(testApp) {
      val result: Future[Result] = call(controller(implicitApp.injector).subjectMustBePresent(), FakeRequest())
      val statusCode: Int = status(result)
      statusCode must be equalTo UNAUTHORIZED
    }

    "should result in a 200 when a subject is present" in new WithApplication(testApp) {
      val result: Future[Result] = call(controller(implicitApp.injector).subjectMustBePresent(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
      val statusCode: Int = status(result)
      statusCode must be equalTo OK
    }
  }

  "subjectMustNotBePresent" should {
    "should result in a 200 when no subject is present" in new WithApplication(testApp) {
      val result: Future[Result] = call(controller(implicitApp.injector).subjectMustNotBePresent(), FakeRequest())
      val statusCode: Int = status(result)
      statusCode must be equalTo OK
    }

    "should result in a 401 when a subject is present" in new WithApplication(testApp) {
      val result: Future[Result] = call(controller(implicitApp.injector).subjectMustNotBePresent(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
      val statusCode: Int = status(result)
      statusCode must be equalTo UNAUTHORIZED
    }
  }

  def controller(injector: Injector): AbstractSubject
}
