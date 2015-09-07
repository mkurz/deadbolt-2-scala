package be.objectify.deadbolt.scala.test.controllers

import play.api.libs.ws.WS
import play.api.test.WithServer

abstract class AbstractPatternSpec extends AbstractControllerSpec {

  "The application" should {
    "when testing custom constraints" >> {
      "deny access if no subject is present" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/custom").get()).status must equalTo(UNAUTHORIZED)
      }

      "deny access if the subject does not have a permission containing the string 'zombie'" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/custom").withHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject has a permission containing the string 'zombie'" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/custom").withHeaders(("x-deadbolt-test-user", "mani")).get()).status must equalTo(OK)
      }

      "deny access if no subject is present even if invert is true" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/custom/invert").get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject does not have a permission containing the string 'zombie' and if invert is true" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/custom/invert").withHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(OK)
      }

      "deny access if the subject has a permission containing the string 'zombie' and if invert is true" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/custom/invert").withHeaders(("x-deadbolt-test-user", "mani")).get()).status must equalTo(UNAUTHORIZED)
      }
    }

    "when testing regex constraints" >> {
      "deny access if no subject is present" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/regex/zombieKillers").get()).status must equalTo(UNAUTHORIZED)
      }

      "deny access if the subject does not have a permission matching the regex 'killer.undead.zombie'" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/regex/zombieKillers").withHeaders(("x-deadbolt-test-user", "lotte")).get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject does has a permission matching the regex 'killer.undead.zombie'" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/regex/zombieKillers").withHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(OK)
      }

      "allow access if the subject does has a permission matching the regex 'killer.undead.*'" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/regex/undeadKillers").withHeaders(("x-deadbolt-test-user", "lotte")).get()).status must equalTo(OK)
      }

      "deny access if no subject is present even if invert is true" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/regex/zombieKillers/invert").get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject does not have a permission matching the regex 'killer.undead.zombie' and invert is true" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/regex/zombieKillers/invert").withHeaders(("x-deadbolt-test-user", "lotte")).get()).status must equalTo(OK)
      }

      "deny access if the subject does has a permission matching the regex 'killer.undead.zombie' and invert is true" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/regex/zombieKillers/invert").withHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(UNAUTHORIZED)
      }

      "deny access if the subject does has a permission matching the regex 'killer.undead.*' and invert is true" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/regex/undeadKillers/invert").withHeaders(("x-deadbolt-test-user", "lotte")).get()).status must equalTo(UNAUTHORIZED)
      }
    }

    "when testing equality constraints" >> {
      "deny access if no subject is present" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/equality").get()).status must equalTo(UNAUTHORIZED)
      }

      "deny access if the subject does not have a permission equal to the string 'killer.undead.zombie'" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/equality").withHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject has a permission equal to the string 'killer.undead.zombie'" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/equality").withHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(OK)
      }

      "deny access if no subject is present even if invert is true" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/equality/invert").get()).status must equalTo(UNAUTHORIZED)
      }

      "allow access if the subject does not have a permission equal to the string 'killer.undead.zombie' and if invert is true" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/equality/invert").withHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(OK)
      }

      "deny access if the subject has a permission equal to the string 'killer.undead.zombie' and if invert is true" in new WithServer(app = testApp, port = 3333) {
        await(WS.url(s"http://localhost:3333/$pathSegment/pattern/equality/invert").withHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(UNAUTHORIZED)
      }
    }
  }
}
