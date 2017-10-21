package be.objectify.deadbolt.scala.test.controllers

import java.io.File

import akka.stream.Materializer
import be.objectify.deadbolt.scala.test.CompileTimeDiApplicationLoader
import play.api._
import play.api.inject.{ApplicationLifecycle, DefaultApplicationLifecycle}
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.test.PlaySpecification

import scala.concurrent.ExecutionContext

abstract class AbstractControllerSpec extends PlaySpecification with PathSegmentProvider with AhcWSComponents {
  sequential
  isolated

  val environment: Environment = new Environment(rootPath = new File("."),
                                                  classLoader = ApplicationLoader.getClass.getClassLoader,
                                                  mode = Mode.Test)

  lazy val app: Application =  new CompileTimeDiApplicationLoader().load(ApplicationLoader.createContext(environment))


  override def executionContext: ExecutionContext = app.actorSystem.dispatchers.lookup("deadbolt-dispatcher")

  override def configuration: Configuration = app.configuration

  override def applicationLifecycle: ApplicationLifecycle = new DefaultApplicationLifecycle()

  override def materializer: Materializer = app.materializer
}
