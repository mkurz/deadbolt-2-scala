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
import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.cache.{CompositeCache, HandlerCache}
import be.objectify.deadbolt.scala.models.PatternType
import play.api.Logger
import play.api.mvc._
import play.api.routing.Router

import scala.concurrent.Future

/**
  * Filters all incoming HTTP requests and applies constraints based on the route's comment.  If a comment is present, the constraint
  * for that route will be applied.  If access is allowed, the next filter in the chain is invoked; if access is not allowed,
  * [[be.objectify.deadbolt.scala.DeadboltHandler.onAuthFailure()]] is invoked.
  *
  * The format of the comment is deadbolt:constraintType:config.  Individual configurations have the form :label[value] - to omit an optional config,
  * remove :label[value],
  *
  * <ul>
  * <li>deadbolt:subjectPresent:handler[handler name]
  * <ul>
  * <li>handler - optional.  The name of a handler in the HandlerCache</li>
  * </ul>
  * </li>
  * <li>deadbolt:subjectNotPresent:handler[handler name]
  * <ul>
  * <li>name - required.  This is the name passed to DeadboltHandler#isAllowed</li>
  * </ul>
  * </li>
  * <li>deadbolt:dynamic:name[constraint name]:handler[handler name]
  * <ul>
  * <li>name - required.  This is the name passed to DeadboltHandler#isAllowed</li>
  * <li>handler - optional.  The name of a handler in the HandlerCache</li>
  * </ul>
  * </li>
  * <li>deadbolt:pattern:value[constraint value]:type[EQUALITY|REGEX|CUSTOM]:invert[true|false]:handler[handler name]
  * <ul>
  * <li>value - required.  Used to test the permissions of a subject.</li>
  * <li>type - required.  The pattern type, case sensitive.</li>
  * <li>invert - optional.  Defines if the result should be flipped, i.e. a matching permission mean unauthorized.  Defaults to false.</li>
  * <li>handler - optional.  The name of a handler in the HandlerCache</li>
  * </ul>
  * </li>
  * <li>deadbolt:composite:name[constraint name]:handler[handler name]
  * <ul>
  * <li>name - required.  The name of a constraint in CompositeCache.</li>
  * <li>handler - optional.  The name of a handler in the HandlerCache</li>
  * </ul>
  * </li>
  * <li>deadbolt:restrict:name[constraint name]:handler[handler name]
  * <ul>
  * <li>name - required.  The name of a constraint in the CompositeCache</li>
  * <li>handler - optional.  The name of a handler in the HandlerCache</li>
  * </ul>
  * </li>
  * <li>deadbolt:rbp:name[role name]:handler[handler name]
  * <ul>
  * <li>role name - required.  The role name passed to DeadboltHandler#getPermissionsForRole</li>
  * <li>handler - optional.  The name of a handler in the HandlerCache</li>
  * </ul>
  * </li>
  * </ul>
  *
  * Restrict is a tricky one, because the possible combinations of roles leads to a nightmare to parse.  Instead, define your role constraints within the
  * composite cache and use the named constraint instead.  deadbolt:restrict is actually a synonym for deadbolt:composite.
  *
  * @param materializer the materializer
  * @param handlerCache the cache of handlers.  The default handler (defined by [[be.objectify.deadbolt.scala.cache.HandlerCache.apply()]]) will be passed into any constraint that doesn't have a handler associated with it.
  * @author Steve Chaloner (steve@objectify.be)
  * @since 2.5.1
  */
class DeadboltRouteCommentFilter @Inject()(materializer: Materializer,
  handlerCache: HandlerCache,
  constraints: FilterConstraints,
  compositeCache: CompositeCache) extends Filter {

  val logger: Logger = Logger("DeadboltRouteCommentFilter")

  val subjectPresentComment = """deadbolt\:(subjectPresent)(?:\:handler\[(.+?)\]){0,1}""".r
  val subjectNotPresentComment = """deadbolt\:(subjectNotPresent)(?:\:handler\[(.+?)\]){0,1}""".r
  val dynamicComment = """deadbolt\:(dynamic)\:name\[(.+?)\](?:\:handler\[(.+?)\]){0,1}""".r
  val patternComment = """deadbolt\:(pattern)\:value\[(.+?)\]\:type\[(EQUALITY|REGEX|CUSTOM)\](?:\:invert\[(true|false)\]){0,1}(?:\:handler\[(.+?)\]){0,1}""".r
  val compositeComment = """deadbolt\:(composite)\:name\[(.+?)\](?:\:handler\[(.+?)\]){0,1}""".r
  val restrictComment = """deadbolt\:(restrict)\:name\[(.+?)\](?:\:handler\[(.+?)\]){0,1}""".r
  val roleBasedPermissionsComment = """deadbolt\:(rbp)\:name\[(.+?)\](?:\:handler\[(.+?)\]){0,1}""".r

  private val handler = handlerCache()

  override implicit def mat: Materializer = materializer

  override def apply(next: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val comment: String = requestHeader.attrs.get(Router.Attrs.HandlerDef).map(handlerDef => handlerDef.comments).getOrElse("")
    if (comment startsWith "deadbolt:") {
      val authenticatedRequest = new AuthenticatedRequest[AnyContent](Request[AnyContent](requestHeader, AnyContentAsEmpty), None)
      comment match {
        case subjectPresentComment(constraintName, handlerName) => constraints.subjectPresent(requestHeader,
                                                                                              authenticatedRequest,
                                                                                              Option(handlerCache(SimpleHandlerKey(handlerName))).getOrElse(
                                                                                                handler),
                                                                                              next)
        case subjectNotPresentComment(constraintName, handlerName) => constraints.subjectNotPresent(requestHeader,
                                                                                                    authenticatedRequest,
                                                                                                    Option(handlerCache(SimpleHandlerKey(handlerName))).getOrElse(
                                                                                                      handler),
                                                                                                    next)
        case dynamicComment(constraintName, name, handlerName) => constraints.dynamic(name,
                                                                                      Option.empty)(requestHeader,
                                                                                                    authenticatedRequest,
                                                                                                    Option(handlerCache(SimpleHandlerKey(handlerName))).getOrElse(
                                                                                                      handler),
                                                                                                    next)
        case patternComment(constraintName, value, patternType, invert, handlerName) => constraints.pattern(value,
                                                                                                            PatternType.byName(patternType),
                                                                                                            meta = None,
                                                                                                            invert = if (invert == null) false
                                                                                                            else "true".equalsIgnoreCase(invert))(requestHeader,
                                                                                                                                                  authenticatedRequest,
                                                                                                                                                  Option(
                                                                                                                                                    handlerCache(
                                                                                                                                                      SimpleHandlerKey(
                                                                                                                                                        handlerName))).getOrElse(
                                                                                                                                                    handler),
                                                                                                                                                  next)
        case compositeComment(constraintName, name, handlerName) => constraints.composite(compositeCache(name))(requestHeader,
                                                                                                                authenticatedRequest,
                                                                                                                Option(handlerCache(SimpleHandlerKey(handlerName))).getOrElse(
                                                                                                                  handler),
                                                                                                                next)
        case restrictComment(constraintName, name, handlerName) => constraints.composite(compositeCache(name))(requestHeader,
                                                                                                               authenticatedRequest,
                                                                                                               Option(handlerCache(SimpleHandlerKey(handlerName))).getOrElse(
                                                                                                                 handler),
                                                                                                               next)
        case roleBasedPermissionsComment(constraintName, name, handlerName) => constraints.roleBasedPermissions(name)(requestHeader,
                                                                                                                      authenticatedRequest,
                                                                                                                      Option(handlerCache(SimpleHandlerKey(handlerName))).getOrElse(
                                                                                                                        handler),
                                                                                                                      next)
        case _ =>
          logger.error(s"Unknown Deadbolt route comment [$comment], denying access with default handler")
          handler.onAuthFailure(authenticatedRequest)
      }
    } else {
      next(requestHeader)
    }
  }
}
