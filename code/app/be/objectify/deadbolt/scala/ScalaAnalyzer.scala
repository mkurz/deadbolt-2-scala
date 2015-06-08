package be.objectify.deadbolt.scala

import java.util
import java.util.{List, Optional}
import java.util.regex.Pattern
import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.core.DeadboltAnalyzer
import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.cache.PatternCache

/**
 * Scala wrapper for {@link DeadboltAnalyzer}.
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
