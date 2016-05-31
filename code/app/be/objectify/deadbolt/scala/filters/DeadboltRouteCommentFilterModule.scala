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
package be.objectify.deadbolt.scala.filters

import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

/**
  * Bindings for run-time dependency injection.  Enable this module in your application.conf to get access to the route comment filter components.
  *
  * @author Steve Chaloner (steve@objectify.be)
  * @since 2.5.1
 */
class DeadboltRouteCommentFilterModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[DeadboltRouteCommentFilter].toSelf
  )
}
