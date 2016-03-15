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

import be.objectify.deadbolt.scala.ExecutionContextProvider
import be.objectify.deadbolt.scala.composite.Operators.{&&, ||}
import be.objectify.deadbolt.scala.composite.{CompositeConstraints, Constraint}
import be.objectify.deadbolt.scala.models.PatternType

import scala.concurrent.ExecutionContext

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
@Singleton
class MyCompositeConstraints @Inject()(constraints: CompositeConstraints,
                                       ecProvider: ExecutionContextProvider){

  val ec: ExecutionContext = ecProvider.get()

  def zombieKillerOrNoSubjectPresent[A](): Constraint[A] = constraints.ConstraintTree[A](||(ec),
                                                                                         List(constraints.SubjectNotPresent(),
                                                                                              constraints.Pattern("killer.undead.zombie",
                                                                                                                  PatternType.REGEX,
                                                                                                                  invert = false)))
  def hasRoleFooAndPassesDynamic[A](meta: Option[Any] = None): Constraint[A] = constraints.ConstraintTree[A](&&(ec),
                                                                                                             List(constraints.Restrict(List(Array("foo"))),
                                                                                                                  constraints.Dynamic(name = "useMetaInfo",
                                                                                                                                      meta = meta)))
}
