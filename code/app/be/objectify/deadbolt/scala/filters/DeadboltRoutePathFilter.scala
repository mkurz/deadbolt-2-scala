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
package be.objectify.deadbolt.scala.filters

import javax.inject.Inject

import akka.stream.Materializer
import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.AuthenticatedRequest
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.Router.Tags

import scala.concurrent.Future

/**
  * Filters all incoming HTTP requests and matches the method and/or path (depending on the [[AuthorizedRoute]] definition).  If a match is found, the constraint
  * for that route will be applied.  If access is allowed, the next filter in the chain is invoked; if access is not allowed,
  * [[be.objectify.deadbolt.scala.DeadboltHandler.onAuthFailure()]] is invoked.
  *
  * @param materializer the materializer
  * @param handlerCache the cache of handlers.  The default handler (defined by [[HandlerCache.apply()]]) will be passed into any constraint that doesn't have a handler associated with it.
  * @param authorizedRoutes all authorized routes
  * @author Steve Chaloner (steve@objectify.be)
  * @since 2.5.1
  */
class DeadboltRoutePathFilter @Inject()(val materializer: Materializer, handlerCache: HandlerCache, authorizedRoutes: AuthorizedRoutes) extends Filter {

  val handler = handlerCache()

  override implicit def mat: Materializer = materializer

  override def apply(next: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] =

    authorizedRoutes(requestHeader.method, requestHeader.attrs(Router.Attrs.HandlerDef).path) match {
      case Some(authRoute) => authRoute.constraint(requestHeader,
                                                    new AuthenticatedRequest[AnyContent](Request[AnyContent](requestHeader, AnyContentAsEmpty), None),
                                                    authRoute.handler.getOrElse(handler),
                                                    next)
      case None => next(requestHeader)
    }
}
