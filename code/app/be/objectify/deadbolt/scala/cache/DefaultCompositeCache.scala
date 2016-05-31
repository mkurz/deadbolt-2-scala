package be.objectify.deadbolt.scala.cache

import javax.inject.Inject

import be.objectify.deadbolt.scala.composite.Constraint
import play.api.cache.CacheApi
import play.api.mvc.AnyContent

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class DefaultCompositeCache @Inject() (cache: CacheApi) extends CompositeCache {

  override def register(name: String, constraint: Constraint[AnyContent]): Unit = cache.set(s"Deadbolt.composite.$name", constraint)

  override def apply(name: String): Constraint[AnyContent] = {
    val maybeConstraint = cache.get[Constraint[AnyContent]](s"Deadbolt.composite.$name")
    maybeConstraint match {
      case Some(constraint) => constraint
      case None => throw new IllegalStateException(s"No composite constraint with name [$name] registered")
    }
  }
}
