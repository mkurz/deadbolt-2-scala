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
package be.objectify.deadbolt.scala.views

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.models.{Subject, Permission, Role}
import be.objectify.deadbolt.scala.testhelpers.{FakeCache, User}
import be.objectify.deadbolt.scala._
import play.api.cache.CacheApi
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Request, Result, Results}
import play.api.test.PlaySpecification
import play.api.{Application, Mode}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class AbstractViewTest extends PlaySpecification {

  def testApp(handler: DeadboltHandler): Application = new GuiceApplicationBuilder()
                                                       .bindings(new DeadboltModule())
                                                       .bindings(bind[HandlerCache].toInstance(LightweightHandlerCache(handler)))
                                                       .overrides(bind[CacheApi].to[FakeCache])
                                                       .in(Mode.Test)
                                                       .build()

  def user(name: String = "foo",
           roles: List[_ <: Role] = List.empty,
           permissions: List[_ <: Permission] = List.empty): User =  User("foo", roles, permissions)

  def handler(beforeAuthCheck: Option[Result] = None,
              subject: Option[Subject] = None,
              onAuthFailure: Result = Results.Forbidden,
              drh: Option[DynamicResourceHandler] = None): DeadboltHandler = new LightweightHandler(Future.successful(beforeAuthCheck),
                                                                                                    Future.successful(subject),
                                                                                                    Future.successful(onAuthFailure),
                                                                                                    Future.successful(drh))
}

case class drh(allowed: Boolean, check: Boolean) extends DynamicResourceHandler {
  override def isAllowed[A](name: String, meta: Option[Any], deadboltHandler: DeadboltHandler, request: AuthenticatedRequest[A]): Future[Boolean] = Future.successful(allowed)
  override def checkPermission[A](permissionValue: String, meta: Option[Any], deadboltHandler: DeadboltHandler, request: AuthenticatedRequest[A]): Future[Boolean] = Future.successful(check)
}

case class BasicHandlerKey(name: String) extends HandlerKey

case class LightweightHandler(bac: Future[Option[Result]],
                              subj: Future[Option[Subject]],
                              authFail: Future[Result],
                              drh: Future[Option[DynamicResourceHandler]]) extends DeadboltHandler {

  override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = bac
  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = drh
  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] = subj
  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] = authFail
}

case class LightweightHandlerCache(handler: DeadboltHandler) extends HandlerCache {
  override def apply() = handler
  override def apply(key: HandlerKey) = handler
}
