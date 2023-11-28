package be.objectify.deadbolt.scala.test.controllers

import java.io.File

import org.apache.pekko.stream.Materializer
import be.objectify.deadbolt.scala.test.CompileTimeDiApplicationLoader
import org.specs2.specification.AfterEach
import play.api._
import play.api.inject.{ApplicationLifecycle, DefaultApplicationLifecycle}
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.test.PlaySpecification

import scala.concurrent.ExecutionContext

abstract class AbstractControllerSpec extends PlaySpecification with PathSegmentProvider with AhcWSComponents with AfterEach {
  sequential
  isolated

  val environment: Environment = new Environment(rootPath = new File("."),
                                                  classLoader = ApplicationLoader.getClass.getClassLoader,
                                                  mode = Mode.Test)

  lazy val app: Application =  new CompileTimeDiApplicationLoader().load(ApplicationLoader.Context.create(environment))


  override def executionContext: ExecutionContext = app.actorSystem.dispatchers.lookup("deadbolt-dispatcher")

  override def configuration: Configuration = app.configuration

  override def applicationLifecycle: ApplicationLifecycle = new DefaultApplicationLifecycle()

  override def materializer: Materializer = app.materializer

  override def after: Any = {
    wsClient.close()
  }
}
