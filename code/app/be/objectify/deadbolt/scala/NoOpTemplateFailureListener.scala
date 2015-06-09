package be.objectify.deadbolt.scala

import javax.inject.Singleton

import play.api.Logger

/**
 * No-op (or pretty close to it) implementation of TemplateFailureListener.  It trace-logs the received information, and nothing else.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
class NoOpTemplateFailureListener extends TemplateFailureListener {

  val logger: Logger = Logger("deadbolt.template")

  override def failure(message: String, timeout: Long): Unit = if (logger.isTraceEnabled) logger.trace(s"Template constraint failure notification:  Message [$message]  timeout [$timeout]")
}
