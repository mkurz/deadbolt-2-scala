package be.objectify.deadbolt.scala.test.controllers

import play.api.test.WithServer

class SubjectNotPresentSpec extends AbstractControllerSpec {

  "The application" should {
    "allow access if a subject is present" in new WithServer(app = testApp, port = 3333) {
      await(ws(implicitApp).url(s"http://localhost:3333/subject/mustNotBePresent").withHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(UNAUTHORIZED)
    }

    "deny access if a subject is not present" in new WithServer(app = testApp, port = 3333) {
      await(ws(implicitApp).url(s"http://localhost:3333/subject/mustNotBePresent").get()).status must equalTo(OK)
    }
  }
}