package be.objectify.deadbolt.scala.models

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
sealed trait PatternType

object PatternType {
  /**
    * Checks the pattern against the permissions of the user.  Exact, case-sensitive matches only!
    */
  case object EQUALITY extends PatternType

  /**
    * A standard regular expression that will be evaluated against the permissions of the Subject
    */
  case object REGEX extends PatternType

  /**
    * Perform some custom matching on the pattern.
    */
  case object CUSTOM extends PatternType

  def byName(name: String): PatternType = name match {
    case "EQUALITY" => EQUALITY
    case "REGEX" => REGEX
    case "CUSTOM" => CUSTOM
    case _ => throw new IllegalArgumentException(s"Unknown pattern type [$name]")
  }
}


