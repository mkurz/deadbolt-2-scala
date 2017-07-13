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

import be.objectify.deadbolt.scala.models.Subject
import play.api.mvc.{ActionBuilder, ActionTransformer, BodyParser, Request}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object SubjectActionBuilder {

  def apply[B](subject: Option[Subject], executionContext: ExecutionContext, parser: BodyParser[B]) =
    AuthenticatedActionBuilder(subject, executionContext, parser)

  case class AuthenticatedActionBuilder[B](subject: Option[Subject],
                                           override val executionContext: ExecutionContext,
                                           override val parser: BodyParser[B]
                                          ) extends SubjectActionBuilder[B]
}

trait SubjectActionBuilder[B] extends ActionBuilder[AuthenticatedRequest, B] with ActionTransformer[Request, AuthenticatedRequest] {

  def subject: Option[Subject]

  def transform[A](request: Request[A]) = Future.successful{
    new AuthenticatedRequest(request, subject)
  }
}
