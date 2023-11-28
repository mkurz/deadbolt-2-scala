package be.objectify.deadbolt.scala.test.controllers

import play.api.inject.Injector
import play.api.mvc.Result
import play.api.test.{FakeRequest, WithApplication}

import scala.concurrent.Future

abstract class AbstractRestrictUnitSpec extends AbstractUnitSpec {

  "restrict" should {
    "when the foo AND bar roles are required" >> {
      "deny access if no subject is present" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooAndBar(), FakeRequest())
          val statusCode: Int = status(result)
          statusCode must be equalTo UNAUTHORIZED
        }
      }

      "allow access if the subject has the foo and bar roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooAndBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
          val statusCode: Int = status(result)
          statusCode must be equalTo OK
        }
      }

      "deny access if the subject has the foo but not the bar roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooAndBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "trippel")))
          val statusCode: Int = status(result)
          statusCode must be equalTo UNAUTHORIZED
        }
      }

      "deny access if the subject has the bar but not the foo roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooAndBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "steve")))
          val statusCode: Int = status(result)
          statusCode must be equalTo UNAUTHORIZED
        }
      }
    }

    "when the foo OR bar roles are required" >> {
      "deny access if no subject is present" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooOrBar(), FakeRequest())
          val statusCode: Int = status(result)
          statusCode must be equalTo UNAUTHORIZED
        }
      }

      "allow access if the subject has the foo and bar roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooOrBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
          val statusCode: Int = status(result)
          statusCode must be equalTo OK
        }
      }

      "allow access if the subject has the foo but not the bar roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooOrBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "trippel")))
          val statusCode: Int = status(result)
          statusCode must be equalTo OK
        }
      }

      "allow access if the subject has the bar but not the foo roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooOrBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "steve")))
          val statusCode: Int = status(result)
          statusCode must be equalTo OK
        }
      }

      "deny access if the subject has neither the bar or foo roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooOrBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "lotte")))
          val statusCode: Int = status(result)
          statusCode must be equalTo UNAUTHORIZED
        }
      }
    }

    "when the foo AND NOT the bar roles are required" >> {
      "deny access if no subject is present" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooAndNotBar(), FakeRequest())
          val statusCode: Int = status(result)
          statusCode must be equalTo UNAUTHORIZED
        }
      }

      "deny access if the subject has the foo and bar roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooAndNotBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
          val statusCode: Int = status(result)
          statusCode must be equalTo UNAUTHORIZED
        }
      }

      "allow access if the subject has the foo but not the bar roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooAndNotBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "trippel")))
          val statusCode: Int = status(result)
          statusCode must be equalTo OK
        }
      }

      "deny access if the subject has the bar but not the foo roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooAndNotBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "steve")))
          val statusCode: Int = status(result)
          statusCode must be equalTo UNAUTHORIZED
        }
      }

      "deny access if the subject has neither the bar or foo roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooAndNotBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "lotte")))
          val statusCode: Int = status(result)
          statusCode must be equalTo UNAUTHORIZED
        }
      }
    }

    "when the foo OR NOT the bar roles are required" >> {
      "deny access if no subject is present" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooOrNotBar(), FakeRequest())
          val statusCode: Int = status(result)
          statusCode must be equalTo UNAUTHORIZED
        }
      }

      "allow access if the subject has the foo and bar roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooOrNotBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
          val statusCode: Int = status(result)
          statusCode must be equalTo OK
        }
      }

      "allow access if the subject has the foo but not the bar roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooOrNotBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "trippel")))
          val statusCode: Int = status(result)
          statusCode must be equalTo OK
        }
      }

      "deny access if the subject only has the bar role" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooOrNotBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "steve")))
          val statusCode: Int = status(result)
          statusCode must be equalTo UNAUTHORIZED
        }
      }

      "allow access if the subject has neither the bar or foo roles" in new WithApplication(testApp) {
        override def running() = {
          val result: Future[Result] = call(controller(implicitApp.injector).restrictedToFooOrNotBar(), FakeRequest().withHeaders(("x-deadbolt-test-user", "lotte")))
          val statusCode: Int = status(result)
          statusCode must be equalTo OK
        }
      }
    }
  }

  def controller(injector: Injector): AbstractRestrict
}
