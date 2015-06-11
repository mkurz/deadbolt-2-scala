package be.objectify.deadbolt.scala.testhelpers

import play.api.cache.CacheApi

import scala.concurrent.duration.Duration

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class FakeCache extends CacheApi {
  override def set(key: String, value: Any, expiration: Duration): Unit = {}

  override def get[T](key: String)(implicit evidence$2: ClassManifest[T]): Option[T] = None

  override def getOrElse[A](key: String, expiration: Duration)(orElse: => A)(implicit evidence$1: ClassManifest[A]): A = orElse

  override def remove(key: String): Unit = {}
}
