package be.objectify.deadbolt.scala.cache

import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.{HandlerKey, DynamicResourceHandler, DeadboltHandler}
import play.api.mvc.{Result, Request}

import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
trait HandlerCache extends Function[HandlerKey, DeadboltHandler] with Function0[DeadboltHandler] {
  val defaultHandlerName: String = "defaultHandler"

  /**
   * Wraps the default handler in another handler that caches the result of getSubject.  Use this on a per-request basis.
   *
   * @return a handler
   */
  def withCaching = new SubjectCachingHandler(apply())

  /**
   * Wraps the handler in another handler that caches the result of getSubject.  Use this on a per-request basis.
   *
   * @return a handler
   */
  def withCaching(handlerKey: HandlerKey) = new SubjectCachingHandler(apply(handlerKey))

  private class SubjectCachingHandler(delegate: DeadboltHandler) extends DeadboltHandler {
    private[this] var subject: Option[Future[Option[Subject]]] = None

    override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = delegate.beforeAuthCheck(request)

    // there must be a better way to do this
    override def getSubject[A](request: Request[A]): Future[Option[Subject]] =
      if (subject.isDefined) subject.get
      else {
        subject = Option(delegate.getSubject(request))
        subject.get
      }

    override def onAuthFailure[A](request: Request[A]): Future[Result] = delegate.onAuthFailure(request)

    override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = delegate.getDynamicResourceHandler(request)
  }
}
