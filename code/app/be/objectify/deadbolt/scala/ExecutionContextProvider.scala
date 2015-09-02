package be.objectify.deadbolt.scala

import javax.inject.{Provider, Singleton}

import play.api.{Logger, Play}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/**
 * Specifies the execution context used by Deadbolt.  Falls back to [[scala.concurrent.ExecutionContext.global]] if
 * nothing is specified.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
class ExecutionContextProvider extends Provider[ExecutionContext] {

  val logger: Logger = Logger("deadbolt.execution-context")

  override def get(): ExecutionContext = Try(Play.current.injector.instanceOf[DeadboltExecutionContextProvider]) match {
    case Success(provider) =>
      logger.info("Custom execution context found")
      provider.get()
    case Failure(ex) =>
      logger.info("No custom execution context found, falling back to scala.concurrent.ExecutionContext.global")
      scala.concurrent.ExecutionContext.global
  }
}

trait DeadboltExecutionContextProvider extends Provider[ExecutionContext]