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

/**
  * Holds the authorized route definitions, and attempts to find one based on the method (if defined) and path, or just
  * the path if the route's method is None.
  *
  * Extend this class and implement the routes function with a Seq of authorization rules, e.g.
  *
  * <pre>
  *   val routes: Seq[AuthorizedRoute] = Seq(AuthorizedRoute(Get, "/view/$foo<[^/]+>/$bar<[^/]+>", filterConstraints.subjectPresent),
  *                                          AuthorizedRoute(Any, "/profile", filterConstraints.dynamic("ruleName")))
  * </pre>
  *
  * If you're using runtime dependency injection, you will also need to create a binding for your extended class, e.g.
  *
  * <pre>bind[AuthorizedRoutes].to[MyAuthorizedRoutes]</pre>
  *
  * @param filterConstraints used to define constraints.  This is available for injection from either [[DeadboltFilterModule]] or [[DeadboltFilterComponents]], depending on if you're using compile-time or run-time dependency injection.
  * @author Steve Chaloner (steve@objectify.be)
  * @since 2.5.1
  */
abstract class AuthorizedRoutes(filterConstraints: FilterConstraints) extends ((String, String) => Option[AuthorizedRoute]){

  override def apply(method: String, routePattern: String): Option[AuthorizedRoute] =
    routes.find(authRoute => authRoute.method.map(routeMethod => routeMethod.equals(method)
      && authRoute.pathPattern.equals(routePattern)).getOrElse(authRoute.pathPattern.equals(routePattern)))

  val routes: Seq[AuthorizedRoute]
}
