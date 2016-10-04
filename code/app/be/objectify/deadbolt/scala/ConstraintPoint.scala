package be.objectify.deadbolt.scala

/**
  * Indicates the point at which a constraint is applied.  See [[DeadboltHandler.onAuthSuccess()]].
  *
  * @author Steve Chaloner (steve@objectify.be)
  * @since 2.5.1
  */
object ConstraintPoint extends Enumeration {
  type ConstraintPoint = Value
  val CONTROLLER, FILTER, TEMPLATE = Value
}
