package be.objectify.deadbolt.scala

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.cache.HandlerCache
import play.api.mvc._
import be.objectify.deadbolt.core.PatternType

/**
 * Provides helpers for creating Play Actions wrapped by DeadboltActions.
 */
@Singleton
class ActionBuilders @Inject() (deadboltActions: DeadboltActions, handlers: HandlerCache) {

  object RestrictAction {

    def apply(roles: List[Array[String]]): RestrictAction.RestrictActionBuilder = RestrictActionBuilder(roles)
    def apply(roles: String*): RestrictAction.RestrictActionBuilder = RestrictActionBuilder(List(roles.toArray))

    case class RestrictActionBuilder(roles: List[Array[String]]) extends DeadboltActionBuilder {

      def apply(block: => Result)(implicit handler: DeadboltHandler): Action[AnyContent] =
        deadboltActions.Restrict(roles, handler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit handler: DeadboltHandler) : Action[AnyContent] =
        deadboltActions.Restrict(roles, handler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit handler: DeadboltHandler) : Action[A] =
        deadboltActions.Restrict(roles, handler) { Action(bodyParser) { request:Request[A]  => block(request) } }
    }
  }

  object DynamicAction {

    def apply(name: String, meta: String = ""): DynamicAction.DynamicActionBuilder = DynamicActionBuilder(name, meta)

    case class DynamicActionBuilder(name: String, meta: String = "") extends DeadboltActionBuilder {

      def apply(block: => Result)(implicit handler: DeadboltHandler): Action[AnyContent] =
        deadboltActions.Dynamic(name, meta, handler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit handler: DeadboltHandler) : Action[AnyContent] =
        deadboltActions.Dynamic(name, meta, handler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit handler: DeadboltHandler) : Action[A] =
        deadboltActions.Dynamic(name, meta, handler) { Action(bodyParser) { request:Request[A]  => block(request) } }
    }
  }

  object PatternAction {

    def apply(value: String, patternType: PatternType): PatternAction.PatternActionBuilder = PatternActionBuilder(value, patternType)

    case class PatternActionBuilder(value: String, patternType: PatternType) extends DeadboltActionBuilder {

      def apply(block: => Result)(implicit handler: DeadboltHandler): Action[AnyContent] =
        deadboltActions.Pattern(value, patternType, handler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit handler: DeadboltHandler) : Action[AnyContent] =
        deadboltActions.Pattern(value, patternType, handler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit handler: DeadboltHandler) : Action[A] =
        deadboltActions.Pattern(value, patternType, handler) { Action(bodyParser) { request:Request[A]  => block(request) } }
    }
  }

  object SubjectPresentAction {

    def apply(): SubjectPresentAction.SubjectPresentActionBuilder = SubjectPresentActionBuilder()

    case class SubjectPresentActionBuilder() extends DeadboltActionBuilder {

      def apply(block: => Result)(implicit handler: DeadboltHandler): Action[AnyContent] =
        deadboltActions.SubjectPresent(handler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit handler: DeadboltHandler) : Action[AnyContent] =
        deadboltActions.SubjectPresent(handler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit handler: DeadboltHandler) : Action[A] =
        deadboltActions.SubjectPresent(handler) { Action(bodyParser) { request:Request[A]  => block(request) } }
    }
  }

  object SubjectNotPresentAction {

    def apply(): SubjectNotPresentAction.SubjectNotPresentActionBuilder = SubjectNotPresentActionBuilder()

    case class SubjectNotPresentActionBuilder() extends DeadboltActionBuilder {
      def apply(block: => Result)(implicit handler: DeadboltHandler): Action[AnyContent] =
        deadboltActions.SubjectNotPresent(handler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit handler: DeadboltHandler) : Action[AnyContent] =
        deadboltActions.SubjectNotPresent(handler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit handler: DeadboltHandler) : Action[A] =
        deadboltActions.SubjectNotPresent(handler) { Action(bodyParser) { request:Request[A]  => block(request) } }
    }
  }

  trait DeadboltActionBuilder {

    def apply(block: => Result)(implicit handler: DeadboltHandler): Action[AnyContent]
    def apply(block: Request[AnyContent] => Result)(implicit handler: DeadboltHandler) : Action[AnyContent]
    def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit handler: DeadboltHandler) : Action[A]

    def withHandler(handler: DeadboltHandler) = new {
      def apply(block: => Result): Action[AnyContent] =
        DeadboltActionBuilder.this.apply(block)(handler)

      def apply(block: Request[AnyContent] => Result): Action[AnyContent] =
        DeadboltActionBuilder.this.apply(block)(handler)

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)  : Action[A] =
        DeadboltActionBuilder.this.apply(bodyParser)(block)(handler)
    }

    def key(handlerKey: HandlerKey) = withHandler(handlers(handlerKey))

    def defaultHandler() = withHandler(handlers())
  }
}