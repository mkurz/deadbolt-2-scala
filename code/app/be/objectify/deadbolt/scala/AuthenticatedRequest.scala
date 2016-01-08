package be.objectify.deadbolt.scala

import be.objectify.deadbolt.core.models.Subject
import play.api.mvc.Request

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
trait AuthenticatedRequest[+A] extends Request[A] {
  val subject: Option[Subject]
}

object AuthenticatedRequest {
  def apply[A](r: Request[A], s: Option[Subject]) = new AuthenticatedRequest[A] {
    def id = r.id
    def tags = r.tags
    def uri = r.uri
    def path = r.path
    def method = r.method
    def version = r.version
    def queryString = r.queryString
    def headers = r.headers
    def secure = r.secure
    lazy val remoteAddress = r.remoteAddress
    val body = r.body
    val subject = s
  }
}
