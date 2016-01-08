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

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.cache.HandlerCache
import play.api.mvc._
import be.objectify.deadbolt.core.PatternType

import scala.concurrent.Future

/**
 * Provides helpers for creating Play Actions wrapped by DeadboltActions.
 */
@Singleton
class ActionBuilders @Inject() (deadboltActions: DeadboltActions, handlers: HandlerCache) {

  object RestrictAction {

    def apply(roles: List[Array[String]]): RestrictAction.RestrictActionBuilder = RestrictActionBuilder(roles)
    def apply(roles: String*): RestrictAction.RestrictActionBuilder = RestrictActionBuilder(List(roles.toArray))

    case class RestrictActionBuilder(roles: List[Array[String]]) extends DeadboltActionBuilder {

      override def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit handler: DeadboltHandler) : Action[A] =
        deadboltActions.Restrict(roles, handler)(bodyParser)(block)
    }
  }

  object DynamicAction {

    def apply(name: String, meta: String = ""): DynamicAction.DynamicActionBuilder = DynamicActionBuilder(name, meta)

    case class DynamicActionBuilder(name: String, meta: String = "") extends DeadboltActionBuilder {

      override def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit handler: DeadboltHandler) : Action[A] =
        deadboltActions.Dynamic(name, meta, handler)(bodyParser)(block)
    }
  }

  object PatternAction {

    def apply(value: String, patternType: PatternType, invert: Boolean = false): PatternAction.PatternActionBuilder = PatternActionBuilder(value, patternType, invert)

    case class PatternActionBuilder(value: String, patternType: PatternType = PatternType.EQUALITY, invert: Boolean = false) extends DeadboltActionBuilder {

      override def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit handler: DeadboltHandler) : Action[A] =
        deadboltActions.Pattern(value, patternType, handler, invert)(bodyParser)(block)
    }
  }

  object SubjectPresentAction {

    def apply(): SubjectPresentAction.SubjectPresentActionBuilder = SubjectPresentActionBuilder()

    case class SubjectPresentActionBuilder() extends DeadboltActionBuilder {

      override def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit handler: DeadboltHandler) : Action[A] =
        deadboltActions.SubjectPresent(handler)(bodyParser)(block)
    }
  }

  object SubjectNotPresentAction {

    def apply(): SubjectNotPresentAction.SubjectNotPresentActionBuilder = SubjectNotPresentActionBuilder()

    case class SubjectNotPresentActionBuilder() extends DeadboltActionBuilder {

      override def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit handler: DeadboltHandler) : Action[A] =
        deadboltActions.SubjectNotPresent(handler)(bodyParser)(block)
    }
  }

  trait DeadboltActionBuilder {

    def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] =
      apply( _ => block)(deadboltHandler)
    def apply(block: AuthenticatedRequest[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] =
      apply(BodyParsers.parse.anyContent)(block)(deadboltHandler)
    def apply[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Result)(implicit deadboltHandler: DeadboltHandler): Action[A] =
      async(bodyParser) { req: AuthenticatedRequest[A] => Future.successful(block(req))
                                                                                                                                                                }

    def async(block: => Future[Result])(implicit deadbloltHandler: DeadboltHandler): Action[AnyContent] = async( _ => block)(deadbloltHandler)
    def async(block: AuthenticatedRequest[AnyContent] => Future[Result])(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] = async(BodyParsers.parse.anyContent)(block)(deadboltHandler)
    def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadboltHandler: DeadboltHandler): Action[A]

    def withHandler(deadboltHandler: DeadboltHandler) = new {
      def apply(block: => Result): Action[AnyContent] =
        DeadboltActionBuilder.this.apply(block)(deadboltHandler)

      def apply(block: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] =
        DeadboltActionBuilder.this.apply(block)(deadboltHandler)

      def apply[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Result)  : Action[A] =
        DeadboltActionBuilder.this.apply(bodyParser)(block)(deadboltHandler)
    }

    def key(handlerKey: HandlerKey) = withHandler(handlers(handlerKey))

    def defaultHandler() = withHandler(handlers())
  }
}
