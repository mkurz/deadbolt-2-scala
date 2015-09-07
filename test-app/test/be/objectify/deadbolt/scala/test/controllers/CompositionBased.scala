package be.objectify.deadbolt.scala.test.controllers

trait CompositionBased extends PathSegmentProvider{
  override def pathSegment: String = "composed"
}
