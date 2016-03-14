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
package be.objectify.deadbolt.scala

import be.objectify.deadbolt.scala.cache.{DefaultPatternCache, PatternCache}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class DeadboltModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[PatternCache].to[DefaultPatternCache],
    bind[StaticConstraintAnalyzer].toSelf,
    bind[DeadboltActions].toSelf,
    bind[ViewSupport].toSelf,
    bind[ActionBuilders].toSelf,
    bind[TemplateFailureListenerProvider].to[DefaultTemplateFailureListenerProvider],
    bind[ExecutionContextProvider].to[DefaultExecutionContextProvider],
    bind[ConstraintLogic].toSelf
  )
}
