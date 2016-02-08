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
  def withCaching: DeadboltHandler = new SubjectCachingHandler(apply())

  /**
   * Wraps the handler in another handler that caches the result of getSubject.  Use this on a per-request basis.
   *
   * @return a handler
   */
  def withCaching(handlerKey: HandlerKey): DeadboltHandler = new SubjectCachingHandler(apply(handlerKey))

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
