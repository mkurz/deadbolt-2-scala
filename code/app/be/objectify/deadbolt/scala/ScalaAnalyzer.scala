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
package be.objectify.deadbolt.scala

import java.util
import java.util.Optional
import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.core.DeadboltAnalyzer
import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.cache.PatternCache

/**
 * Scala wrapper for [[be.objectify.deadbolt.core.DeadboltAnalyzer]].
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
class ScalaAnalyzer @Inject() (patternCache: PatternCache) {

  val analyzer: DeadboltAnalyzer = new DeadboltAnalyzer()

  def checkRole(subjectOption: Option[Subject], roleNames: Array[String]): Boolean = analyzer.checkRole(Optional.ofNullable(subjectOption.orNull),
                                                                                                        roleNames)

  def getRoleNames (subjectOption: Option[Subject]): util.List[String] = analyzer.getRoleNames(Optional.ofNullable(subjectOption.orNull))

  def hasRole(subjectOption: Option[Subject], roleName: String): Boolean = analyzer.hasRole(Optional.ofNullable(subjectOption.orNull),
                                                                                            roleName)

  def hasAllRoles(subjectOption: Option[Subject], roleNames: Array[String]): Boolean = analyzer.hasAllRoles(Optional.ofNullable(subjectOption.orNull),
                                                                                                            roleNames)

  def checkRegexPattern(subjectOption: Option[Subject], patternValue: String): Boolean = analyzer.checkRegexPattern(Optional.ofNullable(subjectOption.orNull),
                                                                                                                              Optional.ofNullable(patternCache(patternValue).orNull))


  def checkPatternEquality(subjectOption: Option[Subject], patternValueOption: Option[String]): Boolean = analyzer.checkPatternEquality(Optional.ofNullable(subjectOption.orNull),
                                                                                                                                        Optional.ofNullable(patternValueOption.orNull))
}
