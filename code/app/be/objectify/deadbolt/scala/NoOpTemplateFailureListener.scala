package be.objectify.deadbolt.scala

import javax.inject.Singleton

/**
 * No-op implementation of TemplateFailureListener..
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
class NoOpTemplateFailureListener extends TemplateFailureListener {

  override def failure(message: String, timeout: Long): Unit = {}
}
