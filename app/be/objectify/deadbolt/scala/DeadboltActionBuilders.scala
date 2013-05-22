package be.objectify.deadbolt.scala

import play.api.mvc._
import be.objectify.deadbolt.core.PatternType

/**
 * Provides helpers for creating Play! Actions wrapped by DeadboltActions.
 */
object DeadboltActionBuilders {

  object RestrictAction extends DeadboltActions {

    def apply(roles: String*) = RestrictActionBuilder(roles:_*)

    case class RestrictActionBuilder(roles: String*) extends DeadboltActionBuilder {

      def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] =
        Restrict(roles.toArray, deadboltHandler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] =
        Restrict(roles.toArray, deadboltHandler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        Restrict(roles.toArray, deadboltHandler) { Action(bodyParser) { request:Request[A]  => block(request) } }

    }
  }

  object DynamicAction extends DeadboltActions {

    def apply(name: String, meta: String = "") = DynamicActionBuilder(name, meta)

    case class DynamicActionBuilder(name: String, meta: String = "") extends DeadboltActionBuilder {

      def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] =
        Dynamic(name, meta, deadboltHandler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] =
        Dynamic(name, meta, deadboltHandler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        Dynamic(name, meta, deadboltHandler) { Action(bodyParser) { request:Request[A]  => block(request) } }

    }
  }

  object PatternAction extends DeadboltActions {

    def apply(value: String, patternType: PatternType) = PatternActionBuilder(value, patternType)

    case class PatternActionBuilder(value: String, patternType: PatternType) extends DeadboltActionBuilder {

      def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] =
        Pattern(value, patternType, deadboltHandler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] =
        Pattern(value, patternType, deadboltHandler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        Pattern(value, patternType, deadboltHandler) { Action(bodyParser) { request:Request[A]  => block(request) } }

    }
  }

  object SubjectPresentAction extends DeadboltActions {

    def apply() = SubjectPresentActionBuilder()

    case class SubjectPresentActionBuilder() extends DeadboltActionBuilder {

      def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] =
        SubjectPresent(deadboltHandler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] =
        SubjectPresent(deadboltHandler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        SubjectPresent(deadboltHandler) { Action(bodyParser) { request:Request[A]  => block(request) } }

    }
  }

  object SubjectNotPresentAction extends DeadboltActions {

    def apply() = SubjectNotPresentActionBuilder()

    case class SubjectNotPresentActionBuilder() extends DeadboltActionBuilder {
      def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] =
        SubjectNotPresent(deadboltHandler) { Action { block } }

      def apply(block: Request[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] =
        SubjectNotPresent(deadboltHandler) { Action { request => block(request) } }

      def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        SubjectNotPresent(deadboltHandler) { Action(bodyParser) { request:Request[A]  => block(request) } }
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