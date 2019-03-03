package be.objectify.deadbolt.scala.test.controllers

import play.api.test.WithServer

abstract class AbstractRestrictSpec extends AbstractControllerSpec {

  "The application" should {
    "when the foo AND bar roles are required" >> {
      "deny access if no subject is present" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooAndBar").get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject has the foo and bar roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooAndBar").addHttpHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(OK)
      }

      "deny access if the subject has the foo but not the bar roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooAndBar").addHttpHeaders(("x-deadbolt-test-user", "trippel")).get()).status must equalTo(UNAUTHORIZED)
      }

      "deny access if the subject has the bar but not the foo roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooAndBar").addHttpHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(UNAUTHORIZED)
      }
    }

    "when the foo OR bar roles are required" >> {
      "deny access if no subject is present" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooOrBar").get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject has the foo and bar roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooOrBar").addHttpHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(OK)
      }

      "allow access if the subject has the foo but not the bar roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooOrBar").addHttpHeaders(("x-deadbolt-test-user", "trippel")).get()).status must equalTo(OK)
      }

      "allow access if the subject has the bar but not the foo roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooOrBar").addHttpHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(OK)
      }

      "deny access if the subject has neither the bar or foo roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooOrBar").addHttpHeaders(("x-deadbolt-test-user", "lotte")).get()).status must equalTo(UNAUTHORIZED)
      }
    }

    "when the foo AND NOT the bar roles are required" >> {
      "deny access if no subject is present" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooAndNotBar").get()).status must equalTo(UNAUTHORIZED)
      }

      "deny access if the subject has the foo and bar roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooAndNotBar").addHttpHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject has the foo but not the bar roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooAndNotBar").addHttpHeaders(("x-deadbolt-test-user", "trippel")).get()).status must equalTo(OK)
      }

      "deny access if the subject has the bar but not the foo roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooAndNotBar").addHttpHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(UNAUTHORIZED)
      }

      "deny access if the subject has neither the bar or foo roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooAndNotBar").addHttpHeaders(("x-deadbolt-test-user", "lotte")).get()).status must equalTo(UNAUTHORIZED)
      }
    }

    "when the foo OR NOT the bar roles are required" >> {
      "deny access if no subject is present" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooOrNotBar").get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject has the foo and bar roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooOrNotBar").addHttpHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(OK)
      }

      "allow access if the subject has the foo but not the bar roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooOrNotBar").addHttpHeaders(("x-deadbolt-test-user", "trippel")).get()).status must equalTo(OK)
      }

      "deny access if the subject only has the bar role" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooOrNotBar").addHttpHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject has neither the bar or foo roles" in new WithServer(app = app, port = 3333) {
        await(wsClient.url(s"http://localhost:3333/$pathSegment/restrict/fooOrNotBar").addHttpHeaders(("x-deadbolt-test-user", "lotte")).get()).status must equalTo(OK)
      }
    }
  }
}
