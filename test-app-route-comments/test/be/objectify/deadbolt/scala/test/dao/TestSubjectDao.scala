package be.objectify.deadbolt.scala.test.dao

import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.test.models.{SecurityPermission, SecurityRole, SecuritySubject}

class TestSubjectDao extends SubjectDao {

  val subjects: Map[String, Subject] = Map("greet" -> new SecuritySubject("greet",
                                                                           List(SecurityRole("foo"),
                                                                                 SecurityRole("bar")),
                                                                           List(SecurityPermission("killer.undead.zombie"))),
                                            "lotte" -> new SecuritySubject("lotte",
                                                                            List(SecurityRole("hurdy")),
                                                                            List(SecurityPermission("killer.undead.vampire"))),
                                            "steve" -> new SecuritySubject("steve",
                                                                            List(SecurityRole("bar")),
                                                                            List(SecurityPermission("curator.museum.insects"))),
                                            "mani" -> new SecuritySubject("mani",
                                                                           List(SecurityRole("bar"),
                                                                                 SecurityRole("hurdy")),
                                                                           List(SecurityPermission("zombie.movie.enthusiast"))),
                                            "trippel" -> new SecuritySubject("trippel",
                                                                           List(SecurityRole("foo"),
                                                                                 SecurityRole("hurdy")),
                                                                           List[SecurityPermission]()))

  override def user(userName: String): Option[Subject] = subjects.get(userName)
}
