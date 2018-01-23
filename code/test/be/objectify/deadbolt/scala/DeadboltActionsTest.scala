package be.objectify.deadbolt.scala

import java.util.regex.Pattern

import akka.stream.Materializer
import be.objectify.deadbolt.scala.cache.{HandlerCache, PatternCache}
import be.objectify.deadbolt.scala.composite.CompositeConstraints
import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.testhelpers.User
import org.specs2.mock.Mockito
import play.api.mvc._
import play.api.test.PlaySpecification

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object DeadboltActionsTest extends PlaySpecification with Mockito {

  private val materializer: Materializer = mock[Materializer]

  private val ec = scala.concurrent.ExecutionContext.Implicits.global
  private val analyzer = new StaticConstraintAnalyzer(new PatternCache {
    override def apply(value: String): Option[Pattern] = Some(Pattern.compile(value))
  })
  private val ecProvider = new ExecutionContextProvider {
    override def get(): ExecutionContext = ec
  }
  val logic: ConstraintLogic = new ConstraintLogic(analyzer,
    ecProvider)

  val composite: CompositeConstraints = new CompositeConstraints(logic,
    ecProvider)

  val parsers: PlayBodyParsers = PlayBodyParsers()(materializer)

  private def deadbolt(handler: DeadboltHandler): DeadboltActions = new DeadboltActions(
    new DeadboltActionBuilders(new HandlerCache {
      override def apply(): DeadboltHandler = handler
      override def apply(v1: HandlerKey): DeadboltHandler = handler
    } , ecProvider, logic, parsers),
    analyzer,
    new HandlerCache {
      override def apply(): DeadboltHandler = handler
      override def apply(v1: HandlerKey): DeadboltHandler = handler
    },
    ecProvider,
    logic,
    parsers)

  private def request[A](maybeSubject: Option[Subject]): AuthenticatedRequest[A] = new AuthenticatedRequest(mock[Request[A]], maybeSubject)

  private def handler(maybeSubject: Option[Subject]): DeadboltHandler = handler(maybeSubject, None)

  private def handler(maybeSubject: Option[Subject],
    maybeDrh: Option[DynamicResourceHandler]): DeadboltHandler = {
    val handler = mock[DeadboltHandler]
    handler.getSubject(any[AuthenticatedRequest[_]]) returns Future {maybeSubject}(ec)
    handler.getDynamicResourceHandler(any[AuthenticatedRequest[_]]) returns Future {maybeDrh}(ec)
    handler.beforeAuthCheck(any[AuthenticatedRequest[_]]) returns Future {None}(ec)
    handler.getPermissionsForRole("foo") returns Future{List("hurdy.*")}(ec)
  }

  "composite" should {
    "propagate the authorized request when" >> {
      "a subject is present" >> {
        val result: Future[Result] = deadbolt(handler(Option(User()))).Composite(constraint = composite.SubjectPresent())()(ar => Future.successful(if (ar.subject.isDefined) Results.Ok("ok") else Results.Unauthorized("no user"))).apply(request(None))
        await(result).header.status should beEqualTo(200)
      }
      "no subject is present" >> {
        val result: Future[Result] = deadbolt(handler(None)).Composite(constraint = composite.SubjectNotPresent())()(ar => Future.successful(if (ar.subject.isDefined) Results.Ok("ok") else Results.Unauthorized("no user"))).apply(request(None))
        await(result).header.status should beEqualTo(401)
      }
    }
  }
}
