/*
 * Copyright 2012-2017 Steve Chaloner
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

import be.objectify.deadbolt.scala.cache.{CompositeCache, HandlerCache, PatternCache}
import be.objectify.deadbolt.scala.composite.CompositeConstraints
import play.api.BuiltInComponents
import play.api.mvc.PlayBodyParsers

import scala.concurrent.ExecutionContext

/**
  * Individual components of Deadbolt.  Use this trait if your
  * application uses compile-time dependency injection.
  *
  * @author Steve Chaloner (steve@objectify.be)
  */
trait DeadboltComponents extends BuiltInComponents {

  def patternCache: PatternCache

  def compositeCache: CompositeCache

  def handlers: HandlerCache

  lazy val defaultEcContextProvider: ExecutionContextProvider = new ExecutionContextProvider {
    override val get: ExecutionContext = scala.concurrent.ExecutionContext.global
  }

  def ecContextProvider: ExecutionContextProvider = defaultEcContextProvider

  lazy val defaultTemplateFailureListenerProvider: TemplateFailureListenerProvider = new TemplateFailureListenerProvider {
    override def get(): TemplateFailureListener = new NoOpTemplateFailureListener
  }

  def templateFailureListenerProvider: TemplateFailureListenerProvider = defaultTemplateFailureListenerProvider

  lazy val scalaAnalyzer: StaticConstraintAnalyzer = new StaticConstraintAnalyzer(patternCache)
  lazy val constraintLogic: ConstraintLogic = new ConstraintLogic(scalaAnalyzer,
    defaultEcContextProvider)

  val deadboltActions: DeadboltActions = new DeadboltActions(scalaAnalyzer,
    handlers,
    ecContextProvider,
    constraintLogic,
    playBodyParsers)

  val actionBuilders: ActionBuilders =
    new ActionBuilders(deadboltActions, handlers, playBodyParsers)

  lazy val viewSupport: ViewSupport = new ViewSupport(configuration,
    templateFailureListenerProvider,
    constraintLogic)
  lazy val compositeConstraints: CompositeConstraints = new CompositeConstraints(constraintLogic,
    ecContextProvider)
}
