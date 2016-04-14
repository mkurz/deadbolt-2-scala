# Deadbolt 2 for Play 2.5

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/be.objectify/deadbolt-scala_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/be.objectify/deadbolt-scala_2.11) [![Build Status](https://travis-ci.org/schaloner/deadbolt-2-scala.svg?branch=master)](https://travis-ci.org/schaloner/deadbolt-2-scala) [![Join the chat at https://gitter.im/schaloner/deadbolt-2](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/schaloner/deadbolt-2?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)


Deadbolt 2 is an authorization library for Play 2, and features APIs for both Java- and Scala-based applications.  It allows you to apply constraints to controller actions, and to customize template rendering based on the current user.

This repository contains the Scala API for Deadbolt.

## Documentation
You can find documentation and examples for Deadbolt at [https://deadbolt-scala.readme.io/v2.5](https://deadbolt-scala.readme.io/v2.5).

## Get the book!
If you want to explore Deadbolt further, you might want to take a look at the book I'm currently writing on it.  You can find it at [https://leanpub.com/deadbolt-2](https://leanpub.com/deadbolt-2).

![Deadbolt 2 - Powerful authorization for your Play application](http://www.objectify.be/wordpress/wp-content/uploads/2015/09/large.jpg)

## Java API
The Java version of Deadbolt can by found at [https://github.com/schaloner/deadbolt-2-java](https://github.com/schaloner/deadbolt-2-java).

## 2.5.x Migration guide

* There is no longer a common module shared by the Scala and Java versions of Deadbolt, so there is no more usage of Java types in the API.
* Types previously found in `be.objectify.deadbolt.core.models` are now found in `be.objectify.deadbolt.scala.models`.
* `be.objectify.deadbolt.core.PatternType` is now `be.objectify.deadbolt.scala.models.PatternType`.
* `be.objectify.deadbolt.core.DeadboltAnalyzer` has been re-implemented in Scala as `be.objectify.deadbolt.scala.StaticConstraintAnalyzer`.
* Instead of an implicit `Request`, actions now receive an explicit `AuthenticatedRequest` that contains an `Option[Subject]`.
* The functions of `DeadboltActions` now return a `Future[Result]` in place of an `Action[A]`.
