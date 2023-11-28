package be.objectify.deadbolt.scala.test.controllers

import play.api.inject._
import play.api.mvc.Result
import play.api.test.{FakeRequest, WithApplication}

import scala.concurrent.Future

abstract class AbstractDynamicUnitSpec extends AbstractUnitSpec {

  "dynamic " should {
    "should result in a 401 when no subject is present" in new WithApplication(testApp) {
      override def running() = {
        val result: Future[Result] = call(controller(implicitApp.injector).index(), FakeRequest())
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }
    }

    "should result in a 401 when the subject does not have the same user name as my wife" in new WithApplication(testApp) {
      override def running() = {
        val result: Future[Result] = call(controller(implicitApp.injector).index(), FakeRequest().withHeaders(("x-deadbolt-test-user", "steve")))
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }
    }

    "should result in a 200 the subject has the same user name as my wife" in new WithApplication(testApp) {
      override def running() = {
        val result: Future[Result] = call(controller(implicitApp.injector).index(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
        val statusCode: Int = status(result)
        statusCode must be equalTo OK
      }
    }
  }

  def controller(injector: Injector): AbstractDynamic
}
