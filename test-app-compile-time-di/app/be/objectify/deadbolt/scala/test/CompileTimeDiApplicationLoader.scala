package be.objectify.deadbolt.scala.test

import java.util.regex.Pattern

import _root_.controllers.Assets
import be.objectify.deadbolt.scala.test.controllers.composed.Composite
import be.objectify.deadbolt.scala.{DeadboltComponents, ExecutionContextProvider}
import be.objectify.deadbolt.scala.cache.{CompositeCache, HandlerCache, PatternCache}
import be.objectify.deadbolt.scala.composite.Constraint
import be.objectify.deadbolt.scala.test.dao.{SubjectDao, TestSubjectDao}
import be.objectify.deadbolt.scala.test.security.{MyCompositeConstraints, MyHandlerCache}
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}
import play.api.ApplicationLoader.Context
import play.api.routing.Router

import scala.concurrent.ExecutionContext
import be.objectify.deadbolt.scala.test.controllers._
import play.api.mvc.AnyContent
import router.Routes

import scala.collection.mutable

/**
 * Application loader for enabling compile-time DI.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
class CompileTimeDiApplicationLoader extends ApplicationLoader  {
  override def load(context: Context): Application = new ApplicationComponents(context).application
}

class ApplicationComponents(context: Context) extends BuiltInComponentsFromContext(context) with DeadboltComponents {

  override lazy val ecContextProvider: ExecutionContextProvider = new ExecutionContextProvider {
    override val get: ExecutionContext = scala.concurrent.ExecutionContext.global
  }

  lazy val subjectDao: SubjectDao = new TestSubjectDao

  lazy val myCompositeConstraints = new MyCompositeConstraints(compositeConstraints,
                                                               ecContextProvider)

  override lazy val patternCache: PatternCache = new PatternCache {
    override def apply(v1: String): Option[Pattern] = Some(Pattern.compile(v1))
  }

  override lazy val handlers: HandlerCache = new MyHandlerCache(subjectDao) 

  lazy val builderDynamic: builder.Dynamic = new builder.Dynamic(actionBuilders, controllerComponents)
  lazy val builderPattern: builder.Pattern = new builder.Pattern(actionBuilders, controllerComponents)
  lazy val builderRestrict: builder.Restrict = new builder.Restrict(actionBuilders, controllerComponents)
  lazy val builderSubject: builder.Subject = new builder.Subject(actionBuilders, controllerComponents)

  lazy val composedDynamic: composed.Dynamic = new composed.Dynamic(deadboltActions, controllerComponents)
  lazy val composedPattern: composed.Pattern = new composed.Pattern(deadboltActions, controllerComponents)
  lazy val composedRestrict: composed.Restrict = new composed.Restrict(deadboltActions, controllerComponents)
  lazy val composedSubject: composed.Subject = new composed.Subject(deadboltActions, controllerComponents)
  lazy val composedComposite: composed.Composite = new Composite(deadboltActions,
                                                                 myCompositeConstraints, controllerComponents)

  override lazy val router: Router = new Routes(httpErrorHandler,
                                                builderDynamic,
                                                builderPattern,
                                                builderRestrict,
                                                builderSubject,
                                                composedDynamic,
                                                composedPattern,
                                                composedRestrict,
                                                composedSubject,
                                                composedComposite,
                                                "")

//  lazy val assets = new Assets(httpErrorHandler)

  override def httpFilters = Seq.empty

  override def compositeCache: CompositeCache = new CompositeCache {

    val composites: scala.collection.mutable.Map[String, Constraint[AnyContent]] = mutable.Map()

    override def register(name: String, constraint: Constraint[AnyContent]): Unit = composites.put(name, constraint)

    override def apply(name: String): Constraint[AnyContent] = composites(name)
  }
}