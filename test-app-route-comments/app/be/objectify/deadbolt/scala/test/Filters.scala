package be.objectify.deadbolt.scala.test

import javax.inject.Inject

import be.objectify.deadbolt.scala.filters.DeadboltRouteCommentFilter
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
class Filters @Inject() (deadbolt: DeadboltRouteCommentFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(deadbolt)
}
