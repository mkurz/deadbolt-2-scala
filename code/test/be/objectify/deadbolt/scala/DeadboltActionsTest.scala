package be.objectify.deadbolt.scala

import java.util.regex.Pattern

import akka.stream.Materializer
import be.objectify.deadbolt.scala.cache.{HandlerCache, PatternCache}
import be.objectify.deadbolt.scala.composite.CompositeConstraints
import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.testhelpers.User
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import play.api.mvc._
import play.api.test.PlaySpecification

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
object DeadboltActionsTest extends PlaySpecification {

  private val materializer: Materializer = mock(classOf[Materializer])

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

  private def deadbolt(handler: DeadboltHandler): DeadboltActions = new DeadboltActions(analyzer,
    new HandlerCache {
      override def apply(): DeadboltHandler = handler
      override def apply(v1: HandlerKey): DeadboltHandler = handler
    },
    ecProvider,
    logic,
    parsers)

  private def request[A](maybeSubject: Option[Subject]): AuthenticatedRequest[A] = new AuthenticatedRequest(mock(classOf[Request[A]]), maybeSubject)

  private def handler(maybeSubject: Option[Subject]): DeadboltHandler = handler(maybeSubject, None)

  private def handler(maybeSubject: Option[Subject],
    maybeDrh: Option[DynamicResourceHandler]): DeadboltHandler = {
    val handler = mock(classOf[DeadboltHandler])
    when(handler.getSubject(any[AuthenticatedRequest[_]])).thenReturn(Future {maybeSubject}(ec))
    when(handler.getDynamicResourceHandler(any[AuthenticatedRequest[_]])).thenReturn(Future {maybeDrh}(ec))
    when(handler.beforeAuthCheck(any[AuthenticatedRequest[_]])).thenReturn(Future {None}(ec))
    when(handler.getPermissionsForRole("foo")).thenReturn(Future{List("hurdy.*")}(ec))
    handler
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
