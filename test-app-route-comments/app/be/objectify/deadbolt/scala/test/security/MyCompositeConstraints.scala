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
package be.objectify.deadbolt.scala.test.security

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.cache.{CompositeCache, DefaultCompositeCache}
import be.objectify.deadbolt.scala.composite.CompositeConstraints
import be.objectify.deadbolt.scala.composite.Operators.||
import be.objectify.deadbolt.scala.models.PatternType
import be.objectify.deadbolt.scala.{ExecutionContextProvider, allOf, allOfGroup, anyOf}
import play.api.mvc.AnyContent

import scala.concurrent.ExecutionContext

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
@Singleton
class MyCompositeConstraints @Inject()(constraints: CompositeConstraints,
                                       compositeCache: DefaultCompositeCache,
                                       ecProvider: ExecutionContextProvider) {

  val ec: ExecutionContext = ecProvider.get()

  compositeCache.register("zombieKillerOrNoSubjectPresent",
    constraints.ConstraintTree[AnyContent](||(ec),
      List(constraints.SubjectNotPresent(),
        constraints.Pattern("killer.undead.zombie",
          PatternType.REGEX,
          invert = false))))

  compositeCache.register("fooAndBar",
    constraints.Restrict(allOfGroup("foo", "bar")))
  compositeCache.register("fooOrBar",
    constraints.Restrict(anyOf(allOf("foo"), allOf("bar"))))
  compositeCache.register("fooAndNotBar",
    constraints.Restrict(anyOf(allOf("foo", "!bar"))))
  compositeCache.register("fooOrNotBar",
    constraints.Restrict(anyOf(allOf("foo"), allOf("!bar"))))
  //  compositeCache.register("hasRoleFooAndPassesDynamic",
  //                          constraints.ConstraintTree[_](&&(ec),
  //                                                        List(constraints.Restrict(List(Array("foo"))),
  //                                                             constraints.Dynamic(name = "useMetaInfo",
  //                                                                                 meta = meta))))
}
