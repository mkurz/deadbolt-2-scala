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

import javax.inject.{Inject, Provider, Singleton}

import play.api.{Application, Logger}

import scala.util.{Failure, Success, Try}

/**
 * Provides an instance of [[TemplateFailureListener]] for cases where template rendering fails.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
trait TemplateFailureListenerProvider extends Provider[TemplateFailureListener]

/**
 * A default implementation of [[TemplateFailureListenerProvider]] that checks the injector
 * for a [[TemplateFailureListener]] instance.
 */
@Singleton
class DefaultTemplateFailureListenerProvider @Inject() (appProvider: Provider[Application]) extends TemplateFailureListenerProvider {

  val logger: Logger = Logger("deadbolt.template")

  override def get(): TemplateFailureListener = Try(appProvider.get().injector.instanceOf[TemplateFailureListener]) match {
    case Success(listener) =>
      logger.info(s"Custom TemplateFailureListener found: $listener")
      listener
    case Failure(ex) =>
      logger.info("No custom TemplateFailureListener found, falling back to no-op implementation")
      new NoOpTemplateFailureListener()
  }
}
