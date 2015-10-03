package be.objectify.deadbolt.scala.test.security

import be.objectify.deadbolt.scala.TemplateFailureListener
import play.api.Logger

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class MyCustomTemplateFailureListener extends TemplateFailureListener {

  val logger: Logger = Logger(this.getClass)

  override def failure(message: String, timeout: Long): Unit =
    logger.error(s"Template constraint failure: message [$message]  timeout [$timeout]ms")
}
