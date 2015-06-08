package be.objectify.deadbolt.scala.cache

import java.util.regex.Pattern
import javax.inject.{Inject, Singleton}

import play.api.cache.CacheApi

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
class DefaultPatternCache @Inject() (cache: CacheApi) extends PatternCache {

  override def apply(value: String): Option[Pattern] = cache.getOrElse[Option[Pattern]](key = s"Deadbolt.pattern.$value") { Some(Pattern.compile(value)) }
}
