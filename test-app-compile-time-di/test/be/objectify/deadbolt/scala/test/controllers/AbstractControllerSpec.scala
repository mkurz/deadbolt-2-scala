package be.objectify.deadbolt.scala.test.controllers

import java.io.File

import be.objectify.deadbolt.scala.test.CompileTimeDiApplicationLoader
import play.api.inject.{DefaultApplicationLifecycle, ApplicationLifecycle}
import play.api.libs.ws.ning.NingWSComponents
import play.api._
import play.api.test.PlaySpecification

abstract class AbstractControllerSpec extends PlaySpecification with PathSegmentProvider with NingWSComponents {
  sequential
  isolated

  val environment: Environment = new Environment(rootPath = new File("."),
                                                  classLoader = ApplicationLoader.getClass.getClassLoader,
                                                  mode = Mode.Test)

  def app: Application =  new CompileTimeDiApplicationLoader().load(ApplicationLoader.createContext(environment))

  override def configuration: Configuration = app.configuration

  override def applicationLifecycle: ApplicationLifecycle = new DefaultApplicationLifecycle()

}
