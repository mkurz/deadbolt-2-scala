/*
 * Copyright 2012-2015 Steve Chaloner
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
package be.objectify.deadbolt.scala

import be.objectify.deadbolt.scala.cache.HandlerCache
import play.api.{Application, Play}

/**
 * We can't inject into views, or objects, so an injector look-up and some implicits to the rescue...
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
object ViewAccessPoint {
  private[deadbolt] val viewStuff = Application.instanceCache[ViewSupport]
  private[deadbolt] val handlerStuff = Application.instanceCache[HandlerCache]

  object Implicits {
    implicit def viewSupport(implicit application: Application): ViewSupport = viewStuff(application)
    implicit def handlerCache(implicit application: Application): HandlerCache = handlerStuff(application)
  }
}
