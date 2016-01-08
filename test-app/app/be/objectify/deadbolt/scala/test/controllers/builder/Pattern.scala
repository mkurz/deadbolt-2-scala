package be.objectify.deadbolt.scala.test.controllers.builder

import be.objectify.deadbolt.core.PatternType
import be.objectify.deadbolt.scala.ActionBuilders
import com.google.inject.Inject
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class Pattern @Inject()(actionBuilder: ActionBuilders) extends Controller {

  def custom =
    actionBuilder.PatternAction(value = "i-do-not-like-ice-cream",
                                 patternType = PatternType.CUSTOM)
    .defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def invertedCustom =
    actionBuilder.PatternAction(value = "i-do-not-like-ice-cream",
                                 patternType = PatternType.CUSTOM,
                                 invert = true)
    .defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def equality =
    actionBuilder.PatternAction(value = "killer.undead.zombie",
                                 patternType = PatternType.EQUALITY)
    .defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def invertedEquality =
    actionBuilder.PatternAction(value = "killer.undead.zombie",
                                 patternType = PatternType.EQUALITY,
                                 invert = true)
    .defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def regex_zombieKillersOnly =
    actionBuilder.PatternAction(value = "killer.undead.zombie",
                                 patternType = PatternType.REGEX)
    .defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def invertedRegex_zombieKillersOnly =
    actionBuilder.PatternAction(value = "killer.undead.zombie",
                                 patternType = PatternType.REGEX,
                                 invert = true)
    .defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def regex_anyKillersOfTheUndeadWelcome =
    actionBuilder.PatternAction(value = "killer.undead.*",
                                 patternType = PatternType.REGEX)
    .defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

  def invertedRegex_anyKillersOfTheUndeadWelcome =
    actionBuilder.PatternAction(value = "killer.undead.*",
                                 patternType = PatternType.REGEX,
                                 invert = true)
    .defaultHandler() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }
}
