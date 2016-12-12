package be.objectify.deadbolt.scala.test.controllers

import play.api.test.WithServer

class RestrictSpec extends AbstractControllerSpec {

  "The application" should {
    "when the foo AND bar roles are required" >> {
      "deny access if no subject is present" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooAndBar").get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject has the foo and bar roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooAndBar").withHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(OK)
      }

      "deny access if the subject has the foo but not the bar roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooAndBar").withHeaders(("x-deadbolt-test-user", "trippel")).get()).status must equalTo(UNAUTHORIZED)
      }

      "deny access if the subject has the bar but not the foo roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooAndBar").withHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(UNAUTHORIZED)
      }
    }

    "when the foo OR bar roles are required" >> {
      "deny access if no subject is present" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooOrBar").get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject has the foo and bar roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooOrBar").withHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(OK)
      }

      "allow access if the subject has the foo but not the bar roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooOrBar").withHeaders(("x-deadbolt-test-user", "trippel")).get()).status must equalTo(OK)
      }

      "allow access if the subject has the bar but not the foo roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooOrBar").withHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(OK)
      }

      "deny access if the subject has neither the bar or foo roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooOrBar").withHeaders(("x-deadbolt-test-user", "lotte")).get()).status must equalTo(UNAUTHORIZED)
      }
    }

    "when the foo AND NOT the bar roles are required" >> {
      "deny access if no subject is present" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooAndNotBar").get()).status must equalTo(UNAUTHORIZED)
      }

      "deny access if the subject has the foo and bar roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooAndNotBar").withHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject has the foo but not the bar roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooAndNotBar").withHeaders(("x-deadbolt-test-user", "trippel")).get()).status must equalTo(OK)
      }

      "deny access if the subject has the bar but not the foo roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooAndNotBar").withHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(UNAUTHORIZED)
      }

      "deny access if the subject has neither the bar or foo roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooAndNotBar").withHeaders(("x-deadbolt-test-user", "lotte")).get()).status must equalTo(UNAUTHORIZED)
      }
    }

    "when the foo OR NOT the bar roles are required" >> {
      "deny access if no subject is present" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooOrNotBar").get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject has the foo and bar roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooOrNotBar").withHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(OK)
      }

      "allow access if the subject has the foo but not the bar roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooOrNotBar").withHeaders(("x-deadbolt-test-user", "trippel")).get()).status must equalTo(OK)
      }

      "deny access if the subject only has the bar role" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooOrNotBar").withHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject has neither the bar or foo roles" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/restrict/fooOrNotBar").withHeaders(("x-deadbolt-test-user", "lotte")).get()).status must equalTo(OK)
      }
    }
  }
}
