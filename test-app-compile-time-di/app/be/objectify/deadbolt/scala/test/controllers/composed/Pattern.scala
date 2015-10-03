package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.core.PatternType
import be.objectify.deadbolt.scala.DeadboltActions
import play.api.mvc.{Action, Controller}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Pattern(deadbolt: DeadboltActions) extends Controller {

  def custom =
    deadbolt.Pattern(value = "i-do-not-like-ice-cream",
                      patternType = PatternType.CUSTOM) {
      Action {
        Ok("Content accessible")
      }
    }

  def invertedCustom =
    deadbolt.Pattern(value = "i-do-not-like-ice-cream",
                      patternType = PatternType.CUSTOM,
                      invert = true) {
      Action {
        Ok("Content accessible")
      }
    }

  def equality =
    deadbolt.Pattern(value = "killer.undead.zombie",
                      patternType = PatternType.EQUALITY) {
      Action {
        Ok("Content accessible")
      }
    }

  def invertedEquality =
    deadbolt.Pattern(value = "killer.undead.zombie",
                      patternType = PatternType.EQUALITY,
                      invert = true) {
      Action {
        Ok("Content accessible")
      }
    }

  def regex_zombieKillersOnly =
    deadbolt.Pattern(value = "killer.undead.zombie",
                      patternType = PatternType.REGEX) {
      Action {
        Ok("Content accessible")
      }
    }

  def invertedRegex_zombieKillersOnly =
    deadbolt.Pattern(value = "killer.undead.zombie",
                      patternType = PatternType.REGEX,
                      invert = true) {
      Action {
        Ok("Content accessible")
      }
    }

  def regex_anyKillersOfTheUndeadWelcome =
    deadbolt.Pattern(value = "killer.undead.*",
                      patternType = PatternType.REGEX) {
      Action {
        Ok("Content accessible")
      }
    }

  def invertedRegex_anyKillersOfTheUndeadWelcome =
    deadbolt.Pattern(value = "killer.undead.*",
                      patternType = PatternType.REGEX,
                      invert = true) {
      Action {
        Ok("Content accessible")
      }
    }
}
