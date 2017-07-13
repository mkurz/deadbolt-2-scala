package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.scala.test.controllers.{AbstractControllerSpec, CompositionBased}
import play.api.test.WithServer

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class CompositeSpec extends AbstractControllerSpec with CompositionBased {

  "The application" should {
    "deny access if" >> {
      "a subject is present but does not meet the pattern constraint" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/$pathSegment/composite/subjectDoesNotHavePermission")
              .withHttpHeaders(("x-deadbolt-test-user", "steve"))
              .get()).status must equalTo(UNAUTHORIZED)
      }

      "the required role is held but the dynamic check fails" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/$pathSegment/composite/roleButNotDynamic")
              .withHttpHeaders(("x-deadbolt-test-user", "trippel"))
              .get()).status must equalTo(UNAUTHORIZED)
      }

      "the dynamic check passes but the required role is not help" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/$pathSegment/composite/noRoleButPassesDynamic")
              .withHttpHeaders(("x-deadbolt-test-user", "lotte"))
              .get()).status must equalTo(UNAUTHORIZED)
      }
    }

    "allow access if" >> {
      "a subject is present and meets the pattern constraint" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/$pathSegment/composite/subjectDoesNotHavePermission")
              .withHttpHeaders(("x-deadbolt-test-user", "greet"))
              .get()).status must equalTo(OK)
      }

      "a subject is not present" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/$pathSegment/composite/subjectDoesNotHavePermission")
              .get()).status must equalTo(OK)
      }

      "the required role is held and the dynamic check passes" in new WithServer(app = testApp, port = 3333) {
        await(ws(implicitApp).url(s"http://localhost:3333/$pathSegment/composite/hasRoleAndPassesDynamic")
              .withHttpHeaders(("x-deadbolt-test-user", "trippel"))
              .get()).status must equalTo(OK)
      }
    }
  }
}
