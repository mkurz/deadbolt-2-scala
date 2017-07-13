package be.objectify.deadbolt.scala.test.controllers

import play.api.test.WithServer

abstract class AbstractDynamicSpec extends AbstractControllerSpec {

  "The application" should {
    "deny access if no subject is present" in new WithServer(app = testApp, port = 3333) {
      await(ws(implicitApp).url(s"http://localhost:3333/$pathSegment/dynamic/niceName").get()).status must equalTo(UNAUTHORIZED)
    }

    "deny access if the subject does not have the same user name as my wife" in new WithServer(app = testApp, port = 3333) {
      await(ws(implicitApp).url(s"http://localhost:3333/$pathSegment/dynamic/niceName").withHttpHeaders(("x-deadbolt-test-user", "steve")).get()).status must equalTo(UNAUTHORIZED)
    }

    "allow access if the subject has the same user name as my wife" in new WithServer(app = testApp, port = 3333) {
      await(ws(implicitApp).url(s"http://localhost:3333/$pathSegment/dynamic/niceName").withHttpHeaders(("x-deadbolt-test-user", "greet")).get()).status must equalTo(OK)
    }
  }
}
