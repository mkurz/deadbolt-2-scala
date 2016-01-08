package be.objectify.deadbolt.scala.test.controllers.composed

import be.objectify.deadbolt.core.PatternType
import be.objectify.deadbolt.scala.DeadboltActions
import com.google.inject.Inject
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Pattern @Inject()(deadbolt: DeadboltActions) extends Controller {

  def custom =
    deadbolt.Pattern(value = "i-do-not-like-ice-cream",
                      patternType = PatternType.CUSTOM)() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def invertedCustom =
    deadbolt.Pattern(value = "i-do-not-like-ice-cream",
                      patternType = PatternType.CUSTOM,
                      invert = true)() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def equality =
    deadbolt.Pattern(value = "killer.undead.zombie",
                      patternType = PatternType.EQUALITY)() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def invertedEquality =
    deadbolt.Pattern(value = "killer.undead.zombie",
                      patternType = PatternType.EQUALITY,
                      invert = true)() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def regex_zombieKillersOnly =
    deadbolt.Pattern(value = "killer.undead.zombie",
                      patternType = PatternType.REGEX)() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def invertedRegex_zombieKillersOnly =
    deadbolt.Pattern(value = "killer.undead.zombie",
                      patternType = PatternType.REGEX,
                      invert = true)() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def regex_anyKillersOfTheUndeadWelcome =
    deadbolt.Pattern(value = "killer.undead.*",
                      patternType = PatternType.REGEX)() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def invertedRegex_anyKillersOfTheUndeadWelcome =
    deadbolt.Pattern(value = "killer.undead.*",
                      patternType = PatternType.REGEX,
                      invert = true)() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }
}
