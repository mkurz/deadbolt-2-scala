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

import be.objectify.deadbolt.scala.models.Subject
import play.api.mvc.{AnyContent, AnyContentAsEmpty, Request, RequestHeader}

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
    def clientCertificateChain = r.clientCertificateChain
    lazy val remoteAddress = r.remoteAddress
    val body = r.body
    val subject = s
  }

  def apply[_](rh: RequestHeader, s: Option[Subject]) = new AuthenticatedRequest[AnyContent] {
    def id = rh.id
    def tags = rh.tags
    def uri = rh.uri
    def path = rh.path
    def method = rh.method
    def version = rh.version
    def queryString = rh.queryString
    def headers = rh.headers
    def secure = rh.secure
    def clientCertificateChain = rh.clientCertificateChain
    lazy val remoteAddress = rh.remoteAddress
    val body = AnyContentAsEmpty
    val subject = s
  }
}
