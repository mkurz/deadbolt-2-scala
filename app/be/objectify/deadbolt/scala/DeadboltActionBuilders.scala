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

      override def apply[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        Restrict(roles.toArray, deadboltHandler)(bodyParser)(block)

    }
  }

  object DynamicAction extends DeadboltActions {

    def apply(name: String, meta: String = "") = DynamicActionBuilder(name, meta)

    case class DynamicActionBuilder(name: String, meta: String = "") extends DeadboltActionBuilder {

      override def apply[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        Dynamic(name, meta, deadboltHandler)(bodyParser)(block)

    }
  }

  object PatternAction extends DeadboltActions {

    def apply(value: String, patternType: PatternType) = PatternActionBuilder(value, patternType)

    case class PatternActionBuilder(value: String, patternType: PatternType) extends DeadboltActionBuilder {

      override def apply[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        Pattern(value, patternType, deadboltHandler)(bodyParser)(block)

    }
  }

  object SubjectPresentAction extends DeadboltActions {

    def apply() = SubjectPresentActionBuilder()

    case class SubjectPresentActionBuilder() extends DeadboltActionBuilder {

      override def apply[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        SubjectPresent(deadboltHandler)(bodyParser)(block)

    }
  }

  object SubjectNotPresentAction extends DeadboltActions {

    def apply() = SubjectNotPresentActionBuilder()

    case class SubjectNotPresentActionBuilder() extends DeadboltActionBuilder {

      override def apply[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        SubjectNotPresent(deadboltHandler)(bodyParser)(block)
    }

  }

  trait DeadboltActionBuilder {

    def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] = apply( _ => block)(deadboltHandler)
    def apply(block: AuthenticatedRequest[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] = apply(BodyParsers.parse.anyContent)(block)(deadboltHandler)
    def apply[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[A]

    def withHandler(deadboltHandler: DeadboltHandler) = new {
      def apply(block: => Result): Action[AnyContent] =
        DeadboltActionBuilder.this.apply(block)(deadboltHandler)

      def apply(block: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] =
        DeadboltActionBuilder.this.apply(block)(deadboltHandler)

      def apply[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Result)  : Action[A] =
        DeadboltActionBuilder.this.apply(bodyParser)(block)(deadboltHandler)
    }
  }

}