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

import be.objectify.deadbolt.scala.cache.{HandlerCache, PatternCache}
import play.api.Configuration

import scala.concurrent.ExecutionContext

/**
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
trait DeadboltComponents {

  def patternCache: PatternCache
  def handlers: HandlerCache
  def configuration: Configuration

  lazy val defaultEcContextProvider: ExecutionContextProvider = new ExecutionContextProvider {
    override val get: ExecutionContext = scala.concurrent.ExecutionContext.global
  }
  def ecContextProvider: ExecutionContextProvider = defaultEcContextProvider

  lazy val defaultTemplateFailureListenerProvider: TemplateFailureListenerProvider = new TemplateFailureListenerProvider {
    override def get(): TemplateFailureListener = new NoOpTemplateFailureListener
  }
  def templateFailureListenerProvider: TemplateFailureListenerProvider = defaultTemplateFailureListenerProvider

  lazy val scalaAnalyzer: ScalaAnalyzer = new ScalaAnalyzer(patternCache)
  lazy val deadboltActions: DeadboltActions = new DeadboltActions(scalaAnalyzer,
                                                                  handlers,
                                                                  ecContextProvider)
  lazy val actionBuilders: ActionBuilders = new ActionBuilders(deadboltActions,
                                                               handlers)
  lazy val viewSupport: ViewSupport = new ViewSupport(configuration,
                                                      scalaAnalyzer,
                                                      patternCache,
                                                      templateFailureListenerProvider,
                                                      ecContextProvider)
}
