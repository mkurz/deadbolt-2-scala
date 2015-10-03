package be.objectify.deadbolt.scala.test.security

import javax.inject.{Singleton, Inject}

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.test.dao.SubjectDao
import be.objectify.deadbolt.scala.{DeadboltHandler, HandlerKey}

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class MyHandlerCache(subjectDao: SubjectDao) extends HandlerCache {
  val defaultHandler: DeadboltHandler = new MyDeadboltHandler(subjectDao)

  override def apply(): DeadboltHandler = defaultHandler

  override def apply(handlerKey: HandlerKey): DeadboltHandler = defaultHandler
}
