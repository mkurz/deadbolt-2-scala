package be.objectify.deadbolt.scala

/**
 * Listens for failures when applying constraints at the template level.   Useful for extra logging or creating
 * a load-based template constraint timeout function.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
trait TemplateFailureListener {

  def failure(message: String, timeout: Long)
}
