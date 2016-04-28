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

import be.objectify.deadbolt.scala.DeadboltHandler

/**
  * Defines a route, as defined by its post-compilation path and its method.  If the method is None, the match is
  * purely on the path.
  *
  * The path pattern is the post-compilation path.  For a path with no dynamic parts, this will be the same as what is
  * defined in the routes file.  For a path with dynamic parts, this will be different.  For example, given the following route.
  *
  * <pre>GET     /view/:foo/:bar             controllers.Application.view(foo: String, bar: String)</pre>
  *
  * The post-compilation pattern will be the following:
  *
  * <pre>GET /view/$foo<[^/]+>/$bar<[^/]+> controllers.Application.view(foo:String, bar:String)</pre>
  *
  * If you bring up a list of the routes in the running application (just go to an invalid route, like http://localhost:9000/@foo), you
  * can copy and paste the route patterns from here.
  *
  * @param method the method of the route.  See the be.objectify.deadbolt.scala.filters package object for pre-defined methods
  * @param pathPattern the post-compilation path pattern.
  * @param constraint the constraint to apply to the route.
  * @param handler the handler to use for the request.  If None, the default handler (as defined by HandlerCache.apply() will be used.
  * @author Steve Chaloner (steve@objectify.be)
  * @since 2.5.1
  */
case class AuthorizedRoute(method: Option[String], pathPattern: String, constraint: FilterFunction, handler: Option[DeadboltHandler] = None)
