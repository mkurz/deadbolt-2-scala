package be.objectify.deadbolt.scala.test.dao

import be.objectify.deadbolt.core.models.Subject
import be.objectify.deadbolt.scala.test.models.{SecurityPermission, SecurityRole, SecuritySubject}

import scala.collection.JavaConversions._

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
trait SubjectDao {
  def user(userName: String): Option[Subject]
}

class TestSubjectDao extends SubjectDao {

  val subjects: Map[String, Subject] = Map("greet" -> new SecuritySubject("greet",
                                                                           List(SecurityRole("foo"),
                                                                                 SecurityRole("bar")).toList,
                                                                           List(SecurityPermission("killer.undead.zombie")).toList),
                                            "lotte" -> new SecuritySubject("lotte",
                                                                            List(SecurityRole("hurdy")).toList,
                                                                            List(SecurityPermission("killer.undead.vampire")).toList),
                                            "steve" -> new SecuritySubject("steve",
                                                                            List(SecurityRole("bar")).toList,
                                                                            List(SecurityPermission("curator.museum.insects")).toList),
                                            "mani" -> new SecuritySubject("mani",
                                                                           List(SecurityRole("bar"),
                                                                                 SecurityRole("hurdy")).toList,
                                                                           List(SecurityPermission("zombie.movie.enthusiast")).toList),
                                            "trippel" -> new SecuritySubject("trippel",
                                                                              List(SecurityRole("foo"),
                                                                                    SecurityRole("hurdy")).toList,
                                                                              List[SecurityPermission]().toList))

  override def user(userName: String): Option[Subject] = subjects.get(userName)
}