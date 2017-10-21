package be.objectify.deadbolt.scala.test.controllers

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.test.dao.{SubjectDao, TestSubjectDao}
import be.objectify.deadbolt.scala.test.security.{MyCompositeConstraints, MyHandlerCache}
import play.api.inject.{bind, _}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.PlaySpecification
import play.api.{Application, Mode}

abstract class AbstractControllerSpec extends PlaySpecification {
  sequential
  isolated

  lazy val testApp: Application = new GuiceApplicationBuilder()
    .in(Mode.Test)
    .overrides()
    .bindings(
      bind[MyCompositeConstraints].toSelf.eagerly(),
      bind[SubjectDao].to[TestSubjectDao],
      bind[HandlerCache].to[MyHandlerCache]
    )
    .build()

  def ws(app: Application): WSClient = app.injector.instanceOf[WSClient]

  val x: MyCompositeConstraints = testApp.injector.instanceOf[MyCompositeConstraints]
}
