package be.objectify.deadbolt.scala

import scala.concurrent.Future
import play.api.mvc._
import be.objectify.deadbolt.core.PatternType

/**
 * Provides helpers for creating Play! Actions wrapped by DeadboltActions.
 */
object DeadboltActionBuilders {

  object RestrictAction extends DeadboltActions {

    def apply(roles: String*) = RestrictActionBuilder(roles:_*)

    def async(roles: String*)(block: => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = RestrictActionBuilder(roles: _*).async(block)(deadbloltHandler)
    def async(roles: String*)(block: AuthenticatedRequest[AnyContent] => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = RestrictActionBuilder(roles: _*).async(block)(deadbloltHandler)
    def async[A](roles: String*)(bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = RestrictActionBuilder(roles: _*).async(bodyParser)(block)(deadbloltHandler)

    case class RestrictActionBuilder(roles: String*) extends DeadboltActionBuilder {

      override def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        Restrict(roles.toArray, deadboltHandler)(bodyParser)(block)

    }
  }

  object DynamicAction extends DeadboltActions {

    def apply(name: String, meta: String = "") = DynamicActionBuilder(name, meta)

    def async(name: String)(block: => Future[Result])(implicit deadbloltHandler: DeadboltHandler):Action[AnyContent] = DynamicActionBuilder(name, "").async(block)(deadbloltHandler)
    def async(name: String)(block: AuthenticatedRequest[AnyContent] => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = DynamicActionBuilder(name, "").async(block)(deadbloltHandler)
    def async[A](name: String)(bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = DynamicActionBuilder(name, "").async(bodyParser)(block)(deadbloltHandler)

    def async(name: String, meta: String)(block: => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = DynamicActionBuilder(name, meta).async(block)(deadbloltHandler)
    def async(name: String, meta: String)(block: AuthenticatedRequest[AnyContent] => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = DynamicActionBuilder(name, meta).async(block)(deadbloltHandler)
    def async[A](name: String, meta: String)(bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = DynamicActionBuilder(name, meta).async(bodyParser)(block)(deadbloltHandler)

    case class DynamicActionBuilder(name: String, meta: String = "") extends DeadboltActionBuilder {

      override def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        Dynamic(name, meta, deadboltHandler)(bodyParser)(block)

    }
  }

  object PatternAction extends DeadboltActions {

    def apply(value: String, patternType: PatternType) = PatternActionBuilder(value, patternType)

    def async(value: String, patternType: PatternType)(block: => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = PatternActionBuilder(value, patternType).async(block)(deadbloltHandler)
    def async(value: String, patternType: PatternType)(block: AuthenticatedRequest[AnyContent] => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = PatternActionBuilder(value, patternType).async(block)(deadbloltHandler)
    def async[A](value: String, patternType: PatternType)(bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = PatternActionBuilder(value, patternType).async(bodyParser)(block)(deadbloltHandler)

    case class PatternActionBuilder(value: String, patternType: PatternType) extends DeadboltActionBuilder {

      override def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        Pattern(value, patternType, deadboltHandler)(bodyParser)(block)

    }
  }

  object SubjectPresentAction extends DeadboltActions {

    def apply = SubjectPresentActionBuilder()

    def async(block: => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = SubjectPresentActionBuilder().async(block)(deadbloltHandler)
    def async(block: AuthenticatedRequest[AnyContent] => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = SubjectPresentActionBuilder().async(block)(deadbloltHandler)
    def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = SubjectPresentActionBuilder().async(bodyParser)(block)(deadbloltHandler)

    case class SubjectPresentActionBuilder() extends DeadboltActionBuilder {

      override def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        SubjectPresent(deadboltHandler)(bodyParser)(block)

    }
  }

  object SubjectNotPresentAction extends DeadboltActions {

    def apply = SubjectNotPresentActionBuilder()

    def async(block: => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = SubjectNotPresentActionBuilder().async(block)(deadbloltHandler)
    def async(block: AuthenticatedRequest[AnyContent] => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = SubjectNotPresentActionBuilder().async(block)(deadbloltHandler)
    def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadbloltHandler: DeadboltHandler) = SubjectNotPresentActionBuilder().async(bodyParser)(block)(deadbloltHandler)

    case class SubjectNotPresentActionBuilder() extends DeadboltActionBuilder {

      override def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadboltHandler: DeadboltHandler) : Action[A] =
        SubjectNotPresent(deadboltHandler)(bodyParser)(block)
    }

  }

  trait DeadboltActionBuilder {

    def apply(block: => Result)(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] = apply( _ => block)(deadboltHandler)
    def apply(block: AuthenticatedRequest[AnyContent] => Result)(implicit deadboltHandler: DeadboltHandler) : Action[AnyContent] = apply(BodyParsers.parse.anyContent)(block)(deadboltHandler)
    def apply[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Result)(implicit deadboltHandler: DeadboltHandler): Action[A] = async(bodyParser) { req: AuthenticatedRequest[A] =>
      Future.successful(block(req))
    }

    private[DeadboltActionBuilders] def async(block: => Future[Result])(implicit deadbloltHandler: DeadboltHandler): Action[AnyContent] = async( _ => block)(deadbloltHandler)
    private[DeadboltActionBuilders] def async(block: AuthenticatedRequest[AnyContent] => Future[Result])(implicit deadboltHandler: DeadboltHandler): Action[AnyContent] = async(BodyParsers.parse.anyContent)(block)(deadboltHandler)
    private[DeadboltActionBuilders] def async[A](bodyParser: BodyParser[A])(block: AuthenticatedRequest[A] => Future[Result])(implicit deadboltHandler: DeadboltHandler): Action[A]

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