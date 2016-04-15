package be.objectify.deadbolt.scala.test.controllers

import play.api.inject._
import play.api.mvc.Result
import play.api.test.{FakeRequest, WithApplication}

import scala.concurrent.Future

abstract class AbstractPatternUnitSpec extends AbstractUnitSpec {

  "pattern" should {
    "when testing custom constraints" >> {
      "deny access if no subject is present" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).custom(), FakeRequest())
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }

      "deny access if the subject does not have a permission containing the string 'zombie'" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).custom(), FakeRequest().withHeaders(("x-deadbolt-test-user", "steve")))
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }

      "allow access if the subject has a permission containing the string 'zombie'" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).custom(), FakeRequest().withHeaders(("x-deadbolt-test-user", "mani")))
        val statusCode: Int = status(result)
        statusCode must be equalTo OK
      }

      "deny access if no subject is present even if invert is true" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).invertedCustom(), FakeRequest())
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }

      "allow access if the subject does not have a permission containing the string 'zombie' and if invert is true" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).invertedCustom(), FakeRequest().withHeaders(("x-deadbolt-test-user", "steve")))
        val statusCode: Int = status(result)
        statusCode must be equalTo OK
      }

      "deny access if the subject has a permission containing the string 'zombie' and if invert is true" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).invertedCustom(), FakeRequest().withHeaders(("x-deadbolt-test-user", "mani")))
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }
    }

    "when testing regex constraints" >> {
      "deny access if no subject is present" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).regex_zombieKillersOnly(), FakeRequest())
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }

      "deny access if the subject does not have a permission matching the regex 'killer.undead.zombie'" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).regex_zombieKillersOnly(), FakeRequest().withHeaders(("x-deadbolt-test-user", "lotte")))
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }

      "allow access if the subject does has a permission matching the regex 'killer.undead.zombie'" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).regex_zombieKillersOnly(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
        val statusCode: Int = status(result)
        statusCode must be equalTo OK
      }

      "allow access if the subject does has a permission matching the regex 'killer.undead.*'" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).regex_anyKillersOfTheUndeadWelcome(), FakeRequest().withHeaders(("x-deadbolt-test-user", "lotte")))
        val statusCode: Int = status(result)
        statusCode must be equalTo OK
      }

      "deny access if no subject is present even if invert is true" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).invertedRegex_zombieKillersOnly(), FakeRequest())
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }

      "allow access if the subject does not have a permission matching the regex 'killer.undead.zombie' and invert is true" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).invertedRegex_zombieKillersOnly(), FakeRequest().withHeaders(("x-deadbolt-test-user", "lotte")))
        val statusCode: Int = status(result)
        statusCode must be equalTo OK
      }

      "deny access if the subject does has a permission matching the regex 'killer.undead.zombie' and invert is true" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).invertedRegex_zombieKillersOnly(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }

      "deny access if the subject does has a permission matching the regex 'killer.undead.*' and invert is true" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).invertedRegex_anyKillersOfTheUndeadWelcome(), FakeRequest().withHeaders(("x-deadbolt-test-user", "lotte")))
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }
    }

    "when testing equality constraints" >> {
      "deny access if no subject is present" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).equality(), FakeRequest())
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }

      "deny access if the subject does not have a permission equal to the string 'killer.undead.zombie'" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).equality(), FakeRequest().withHeaders(("x-deadbolt-test-user", "steve")))
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }

      "allow access if the subject has a permission equal to the string 'killer.undead.zombie'" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).equality(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
        val statusCode: Int = status(result)
        statusCode must be equalTo OK
      }

      "deny access if no subject is present even if invert is true" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).invertedEquality(), FakeRequest())
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }

      "allow access if the subject does not have a permission equal to the string 'killer.undead.zombie' and if invert is true" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).invertedEquality(), FakeRequest().withHeaders(("x-deadbolt-test-user", "steve")))
        val statusCode: Int = status(result)
        statusCode must be equalTo OK
      }

      "deny access if the subject has a permission equal to the string 'killer.undead.zombie' and if invert is true" in new WithApplication(testApp) {
        val result: Future[Result] = call(controller(implicitApp.injector).invertedEquality(), FakeRequest().withHeaders(("x-deadbolt-test-user", "greet")))
        val statusCode: Int = status(result)
        statusCode must be equalTo UNAUTHORIZED
      }
    }
  }

  def controller(injector: Injector): AbstractPattern
}
