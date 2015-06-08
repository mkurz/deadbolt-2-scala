package be.objectify.deadbolt.scala.cache

import java.util.regex.Pattern

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
trait PatternCache extends Function1[String, Option[Pattern]] {
}
