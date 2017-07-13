/*
 * Copyright 2012-2016 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.scala.cache

import be.objectify.deadbolt.scala.composite.Constraint
import play.api.mvc.AnyContent

import scala.collection.mutable

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class DefaultCompositeCache extends CompositeCache {

  private val cache: mutable.Map[String, Constraint[AnyContent]] = mutable.Map[String, Constraint[AnyContent]]()

  override def register(name: String, constraint: Constraint[AnyContent]): Unit = cache.put(name, constraint)

  override def apply(name: String): Constraint[AnyContent] = {
    cache.get(name) match {
      case Some(constraint) => constraint
      case None => throw new IllegalStateException(s"No composite constraint with name [$name] registered")
    }
  }
}
