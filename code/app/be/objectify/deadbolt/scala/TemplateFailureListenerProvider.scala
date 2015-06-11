package be.objectify.deadbolt.scala

import javax.inject.{Singleton, Provider}

import play.api.{Logger, Play}

import scala.util.{Failure, Success, Try}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
class TemplateFailureListenerProvider extends Provider[TemplateFailureListener] {

  val logger: Logger = Logger("deadbolt.template")

  override def get(): TemplateFailureListener = Try(Play.current.injector.instanceOf[TemplateFailureListener]) match {
    case Success(listener) =>
      logger.info(s"Custom TemplateFailureListener found: $listener")
      listener
    case Failure(ex) =>
      logger.info("No custom TemplateFailureListener found, falling back to no-op implementation")
      new NoOpTemplateFailureListener()
  }
}
