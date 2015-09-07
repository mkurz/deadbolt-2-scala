package be.objectify.deadbolt.scala.test.controllers

import play.api.libs.ws.WS
import play.api.test.WithServer

abstract class AbstractSubjectNotPresentSpec extends AbstractControllerSpec {

  "The application" should {
    "allow access if a subject is present" in new WithServer(app = testApp, port = 3333) {
      await(WS.url(s"http://localhost:3333/$pathSegment/subject/mustNotBePresent").withHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(UNAUTHORIZED)
    }

    "deny access if a subject is not present" in new WithServer(app = testApp, port = 3333) {
      await(WS.url(s"http://localhost:3333/$pathSegment/subject/mustNotBePresent").get()).status must equalTo(OK)
    }
  }
}