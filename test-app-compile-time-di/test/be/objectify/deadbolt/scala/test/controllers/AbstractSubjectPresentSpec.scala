package be.objectify.deadbolt.scala.test.controllers

import play.api.test.WithServer

abstract class AbstractSubjectPresentSpec extends AbstractControllerSpec {

  "The application" should {
    "allow access if a subject is present" in new WithServer(app = app, port = 3333) {
      await(wsApi.url(s"http://localhost:3333/$pathSegment/subject/mustBePresent").withHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(OK)
    }

    "deny access if a subject is not present" in new WithServer(app = app, port = 3333) {
      await(wsApi.url(s"http://localhost:3333/$pathSegment/subject/mustBePresent").get()).status must equalTo(UNAUTHORIZED)
    }
  }
}


