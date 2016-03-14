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

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.cache.PatternCache
import be.objectify.deadbolt.scala.models.Subject

/**
 * Static constraint analysis.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
class StaticConstraintAnalyzer @Inject()(patternCache: PatternCache) {

  /**
    * Convert the subject's roles to a list of role names.
    *
    * @param maybeSubject an option for the subject
    * @return None if the subject is None or the subject has no roles, otherwise Some list of strings in the same order as the roles
    */
  def getSubjectRoles(maybeSubject: Option[Subject]): Option[List[String]] =
    maybeSubject.map(subject => subject.roles)
    .filter(roles => roles.nonEmpty)
    .map(roles => for (role <- roles) yield role.name)

  /**
    * Check if the subject has a single role.
    *
    * @param maybeSubject an option for the subject
    * @param requiredRoleName the role name the subject is required to have
    * @return true iff the subject has that role
    */
  def hasRole(maybeSubject: Option[Subject],
              requiredRoleName: String): Boolean =
    hasAllRoles(maybeSubject, Array(requiredRoleName))

  /**
    * Check if the subject has several roles using AND.
    *
    * @param maybeSubject an option for the subject
    * @param requiredRoleNames all the roles the subject must hold
    * @return true iff the subject has all roles
    */
  def hasAllRoles(maybeSubject: Option[Subject],
                  requiredRoleNames: RoleGroup): Boolean = {
    def roleMatch(requiredRoleName: String,
                  subjectRoles: List[String]) = {
      val invert = requiredRoleName.startsWith("!")
      val role = if (invert) requiredRoleName.substring(1) else requiredRoleName
      if (invert) !subjectRoles.contains(role) else subjectRoles.contains(role)
    }
    getSubjectRoles(maybeSubject).filter(subjectRoles => subjectRoles.nonEmpty)
    .map(subjectRoles => for (requiredRole <- requiredRoleNames) yield roleMatch(requiredRole, subjectRoles))
    .exists(wasMatched => wasMatched.foldLeft(if (requiredRoleNames.isEmpty) false else true)(_ && _))
  }

  /**
    * Check if any of the subject's permissions match the supplied regular expression.
    *
    * @param maybeSubject an option for the subject
    * @param maybePattern an option for the regular expression
    * @return true iff the subject has permissions and at least one matches the regular expression
    */
  def checkRegexPattern(maybeSubject: Option[Subject],
                        maybePattern: Option[String]): Boolean = {
    val maybeResult: Option[Boolean] = for {
      permissions <- maybeSubject.map(subject => subject.permissions)
      pattern <- maybePattern.flatMap(p => patternCache(p))
    } yield permissions.map(permission => pattern.matcher(permission.value).matches()).foldLeft(false)(_ || _)
    maybeResult.getOrElse(false)
  }

  /**
    * Check if any of the subject's permissions equals the supplied pattern value.
    *
    * @param maybeSubject an option for the subject
    * @param maybePatternValue an option for the equality pattern
    * @return true iff the subject has permissions and at least one equals the pattern value
    */
  def checkPatternEquality(maybeSubject: Option[Subject],
                           maybePatternValue: Option[String]) = {
    val maybeResult: Option[Boolean] = for {
      permissions <- maybeSubject.map(subject => subject.permissions)
      patternValue <- maybePatternValue
    } yield permissions.map(permission => patternValue.equals(permission.value)).foldLeft(false)(_ || _)
    maybeResult.getOrElse(false)
  }
}
