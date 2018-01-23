package be.objectify.deadbolt.scala

import scala.concurrent.{ExecutionContext, Future}

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.models.PatternType
import javax.inject.{Inject, Singleton}
import play.api.mvc.{ActionFunction, ActionTransformer, PlayBodyParsers, Result}


@Singleton
class DeadboltActionBuilders @Inject() (handlers: HandlerCache, ecProvider: ExecutionContextProvider,
  logic: ConstraintLogic, bodyParsers: PlayBodyParsers) {

  private val ec = ecProvider.get()
  private val parser = bodyParsers.anyContent

  private type AuthRequest = AuthenticatedRequest[_]
  private type Block = AuthRequest => Future[Result]

  def RestrictAction(roleGroups: RoleGroups)(implicit handler: DeadboltHandler = handlers()) = createActionBuilder(handler,
    (block: Block) => (authRequest: AuthRequest) => logic.restrict(authRequest, handler, roleGroups,
      (ar: AuthRequest) => {
        handler.onAuthSuccess(authRequest, "restrict", ConstraintPoint.CONTROLLER)
        block(ar)
      },
      (ar: AuthRequest) => handler.onAuthFailure(ar)
    )
  )

  private def createActionBuilder(handler: DeadboltHandler, authorizeBlock: Block => Block) =
    SubjectActionBuilder(None, ec, parser)
      .andThen(new ActionFunction[AuthenticatedRequest, AuthenticatedRequest] {
        override def invokeBlock[A](authRequest: AuthenticatedRequest[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
          handler.beforeAuthCheck(authRequest).flatMap {
            case Some(result) => Future.successful(result)
            case None => authorizeBlock(block.asInstanceOf[Block])(authRequest)
          }(ec)
        }
        override protected def executionContext: ExecutionContext = ec
      })

  def RoleBasedPermissions(roleName: String)(implicit handler: DeadboltHandler = handlers()) = createActionBuilder(handler,
    (block: Block) => (authRequest: AuthRequest) => logic.roleBasedPermissions(authRequest, handler, roleName,
      (ar: AuthRequest) => {
        handler.onAuthSuccess(ar, "roleBasedPermissions", ConstraintPoint.CONTROLLER)
        block(ar)
      },
      (ar: AuthRequest) => handler.onAuthFailure(ar)
    )
  )

  def DynamicAction(name: String, meta: Option[Any] = None)(implicit handler: DeadboltHandler = handlers()) = createActionBuilder(handler,
    (block: Block) => (authRequest: AuthRequest) => logic.dynamic(authRequest, handler, name, meta,
      (ar: AuthRequest) => {
        handler.onAuthSuccess(ar, "dynamic", ConstraintPoint.CONTROLLER)
        block(ar)
      },
      (ar: AuthRequest) => handler.onAuthFailure(ar)
    )
  )

  def PatternAction(value: String, patternType: PatternType, meta: Option[Any] = None, invert: Boolean = false)(implicit handler: DeadboltHandler = handlers()) = createActionBuilder(handler,
    (block: Block) => (authRequest: AuthRequest) => logic.pattern(authRequest, handler, value, patternType, meta, invert,
      (ar: AuthRequest) => {
        handler.onAuthSuccess(ar, "pattern", ConstraintPoint.CONTROLLER)
        block(ar)
      },
      (ar: AuthRequest) => handler.onAuthFailure(ar)
    )
  )

  def SubjectPresentAction(implicit handler: DeadboltHandler = handlers()) = createActionBuilder(handler,
    (block: Block) => (authRequest: AuthRequest) => logic.subjectPresent(authRequest, handler,
      (ar: AuthRequest) => {
        handler.onAuthSuccess(ar, "subjectPresent", ConstraintPoint.CONTROLLER)
        block(ar)
      },
      (ar: AuthRequest) => handler.onAuthFailure(ar)
    )
  )

  def SubjectNotPresentAction(implicit handler: DeadboltHandler = handlers()) = createActionBuilder(handler,
    (block: Block) => (authRequest: AuthRequest) => logic.subjectPresent(authRequest, handler,
      (ar: AuthRequest) => handler.onAuthFailure(ar),
      (ar: AuthRequest) => {
        handler.onAuthSuccess(ar, "subjectNotPresent", ConstraintPoint.CONTROLLER)
        block(ar)
      }
    )
  )

  def WithAuthRequestAction(implicit handler: DeadboltHandler = handlers()) =
    SubjectActionBuilder(None, ec, parser)
      .andThen(new ActionTransformer[AuthenticatedRequest, AuthenticatedRequest] {
        override protected def transform[A](authRequest: AuthenticatedRequest[A]): Future[AuthenticatedRequest[A]] =
          handler.getSubject(authRequest).map { maybeSubject =>
            new AuthenticatedRequest(authRequest, maybeSubject)
          }(ec)
        override protected def executionContext: ExecutionContext = ec
      })
}
