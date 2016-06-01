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
package be.objectify.deadbolt.scala.models

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
sealed trait PatternType

object PatternType {
  /**
    * Checks the pattern against the permissions of the user.  Exact, case-sensitive matches only!
    */
  case object EQUALITY extends PatternType

  /**
    * A standard regular expression that will be evaluated against the permissions of the Subject
    */
  case object REGEX extends PatternType

  /**
    * Perform some custom matching on the pattern.
    */
  case object CUSTOM extends PatternType

  def byName(name: String): PatternType = name match {
    case "EQUALITY" => EQUALITY
    case "REGEX" => REGEX
    case "CUSTOM" => CUSTOM
    case _ => throw new IllegalArgumentException(s"Unknown pattern type [$name]")
  }
}


