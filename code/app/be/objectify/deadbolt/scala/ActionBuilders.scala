package be.objectify.deadbolt.scala

import javax.inject.{Inject, Singleton}

import play.api.mvc._
import be.objectify.deadbolt.core.PatternType

/**
 * Provides helpers for creating Play Actions wrapped by DeadboltActions.
 */
@Singleton
class ActionBuilders @Inject() (deadboltActions: DeadboltActions) {

  object RestrictAction {

    def apply(roles: String*) = RestrictActionBuilder(roles:_*)

    case class RestrictActionBuilder(roles: String*) extends DeadboltActionBuilder {

      def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] =
        deadboltActions.Restrict(roles.toArray, deadboltHandler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] =
        deadboltActions.Restrict(roles.toArray, deadboltHandler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        deadboltActions.Restrict(roles.toArray, deadboltHandler) { Action(bodyParser) { request:Request[A]  => block(request) } }
    }
  }

  object DynamicAction {

    def apply(name: String, meta: String = "") = DynamicActionBuilder(name, meta)

    case class DynamicActionBuilder(name: String, meta: String = "") extends DeadboltActionBuilder {

      def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] =
        deadboltActions.Dynamic(name, meta, deadboltHandler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] =
        deadboltActions.Dynamic(name, meta, deadboltHandler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        deadboltActions.Dynamic(name, meta, deadboltHandler) { Action(bodyParser) { request:Request[A]  => block(request) } }
    }
  }

  object PatternAction {

    def apply(value: String, patternType: PatternType) = PatternActionBuilder(value, patternType)

    case class PatternActionBuilder(value: String, patternType: PatternType) extends DeadboltActionBuilder {

      def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] =
        deadboltActions.Pattern(value, patternType, deadboltHandler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] =
        deadboltActions.Pattern(value, patternType, deadboltHandler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        deadboltActions.Pattern(value, patternType, deadboltHandler) { Action(bodyParser) { request:Request[A]  => block(request) } }
    }
  }

  object SubjectPresentAction {

    def apply() = SubjectPresentActionBuilder()

    case class SubjectPresentActionBuilder() extends DeadboltActionBuilder {

      def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] =
        deadboltActions.SubjectPresent(deadboltHandler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] =
        deadboltActions.SubjectPresent(deadboltHandler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        deadboltActions.SubjectPresent(deadboltHandler) { Action(bodyParser) { request:Request[A]  => block(request) } }
    }
  }

  object SubjectNotPresentAction {

    def apply() = SubjectNotPresentActionBuilder()

    case class SubjectNotPresentActionBuilder() extends DeadboltActionBuilder {
      def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] =
        deadboltActions.SubjectNotPresent(deadboltHandler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] =
        deadboltActions.SubjectNotPresent(deadboltHandler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        deadboltActions.SubjectNotPresent(deadboltHandler) { Action(bodyParser) { request:Request[A]  => block(request) } }
    }
  }

  trait DeadboltActionBuilder {

    def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent]
    def apply(block: Request[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent]
    def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A]

    def withHandler(deadboltHandler: DeadboltHandler) = new {
      def apply(block: => Result): Action[AnyContent] =
        DeadboltActionBuilder.this.apply(block)(deadboltHandler)

      def apply(block: Request[AnyContent] => Result): Action[AnyContent] =
        DeadboltActionBuilder.this.apply(block)(deadboltHandler)

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)  : Action[A] =
        DeadboltActionBuilder.this.apply(bodyParser)(block)(deadboltHandler)
    }
  }
}