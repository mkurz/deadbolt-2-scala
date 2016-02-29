# Deadbolt 2 for Scala 2.4

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/be.objectify/deadbolt-scala_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/be.objectify/deadbolt-scala_2.11) [![Build Status](https://travis-ci.org/schaloner/deadbolt-2-scala.svg?branch=2.4.x)](https://travis-ci.org/schaloner/deadbolt-2-scala) [![Join the chat at https://gitter.im/schaloner/deadbolt-2](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/schaloner/deadbolt-2?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)


Idiomatic Scala API for Deadbolt 2, an authorisation module for Play 2.

Deadbolt 2 comprises of several modules - a common core, and language-specific implementations for Java and Scala.  Example applications and a user guide are also available.  

All modules related to Deadbolt 2, including the user guide, are grouped together in the [Deadbolt 2](https://github.com/schaloner/deadbolt-2) Github super-module.  Installation information, including Deadbolt/Play compatibility, can also be found here.

## An important note about Deadbolt 2.4.4

One persistent question that keeps coming up is "how do I get the subject in an authorized action?".  As of Deadbolt 2.5, the request passed into an action has been replaced with an `AuthenticatedRequest` which contains an `Option[Subject]`.  As users of 2.4 also want this feature, it has been included in 2.4.4 as a breaking change.

The following examples use `SubjectPresent` as an example, but the same change applies to all authorization constraints.

#### Action builders
In place of 

    def index = actionBuilder.SubjectPresentAction().defaultHandler() { implicit request
      Ok(accessOk()) 
    }

we now have

    def index = actionBuilder.SubjectPresentAction().defaultHandler() { authRequest =>
      Future {
        Ok(accessOk()) 
      }
    }

#### Action composition
In place of 

    def index = deadbolt.SubjectPresent() { implicit request
        Action {
            Ok(accessOk())
        }
    }

we now have

    def someFunctionA = deadbolt.SubjectPresent()() { authRequest =>
      Future {
        Ok("Content accessible")
      }
    }

#### DeadboltHandler.getSubject()
The `getSubject()` function of the `DeadboltHandler` trait now takes an `AuthenticatedRequest` instead of a `Request`.

    override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] = 
      request.subject match {
        case Some(user) => request.subject
        case None => // get from database, identity platform, cache, whatever
      }

# Quickstart

Deadbolt 2 is an authorization library for Play 2, and features APIs for both Java- and Scala-based applications.  It allows you to apply constraints to controller actions, and to customize template rendering based on the current user.

Here's a short, fast guide to getting started with the Scala version.

## Dependencies

Add the dependency to your build

    "be.objectify" %% "deadbolt-scala" % "2.4.3"

Add the Deadbolt module to your Play application

    play {
        modules {
            enabled += be.objectify.deadbolt.scala.DeadboltModule
        }
    }

If you're already running your app in activator, don't forgot to reload the project.

## Domain model

Implement the `Subject`, `Role` and `Permission` traits.

* `Subject` represents, typically, a user
* A `Role` is a single system privilege, e.g. **admin**, **user** and so on.  A subject can have zero or more roles.
* A `Permission` is a can be used with regular expression matching, e.g. a subject with a permission of `printers.admin` can access a resource constrained to `printers.*`, `*.admin`, etc.    A subject can have zero or more permissions.

## Hooks

Implement the `be.objectify.deadbolt.scala.DeadboltHandler` trait.  This implementation (or implementations - you can more than one) is used to

* get the current user - `getSubject`
* run a pre-authorization task that can block further execution - `beforeAuthCheck`
* handle authorization failure - `onAuthFailure`
* provide a hook into the dynamic constraint types - `getDynamicResourceHandler`

You only need to implement `be.objectify.deadbolt.scala.DynamicResourceHandler` if you're planning to use `Dynamic` or `Pattern.CUSTOM` constraints.  Dynamic constraints are tests implemented entirely by your code.  This trait has two functions:

* `isAllowed` is used by the `Dynamic` constraint
* `checkPermission` is used by the `Pattern` constraint when the pattern type is `CUSTOM`

Implement the `be.objectify.deadbolt.scala.HandlerCache` trait.  This is used by Deadbolt to obtain instances of `DeadboltHandler`'s, and has two concepts

* A default handler.  You can always use a specific handler in a template or controller, but if nothing is specified a well-known instance will be used.
* Named handlers.

An example implementation follows, based on the sample app.

    @Singleton
    class MyHandlerCache extends HandlerCache {
        val defaultHandler: DeadboltHandler = new MyDeadboltHandler
    
        // HandlerKeys is an user-defined object, containing instances of a case class that extends HandlerKey  
        val handlers: Map[Any, DeadboltHandler] = Map(HandlerKeys.defaultHandler -> defaultHandler,
                                                      HandlerKeys.altHandler -> new MyDeadboltHandler(Some(MyAlternativeDynamicResourceHandler)),
                                                      HandlerKeys.userlessHandler -> new MyUserlessDeadboltHandler)
    
        // Get the default handler.
        override def apply(): DeadboltHandler = defaultHandler
    
        // Get a named handler
        override def apply(handlerKey: HandlerKey): DeadboltHandler = handlers(handlerKey)
    }

Finally, expose your handlers to Deadbolt.  To do this, you will need to create a small module that binds your handler cache by type...

    package com.example.modules
    
    import be.objectify.deadbolt.scala.cache.HandlerCache
    import play.api.inject.{Binding, Module}
    import play.api.{Configuration, Environment}
    import com.example.security.MyHandlerCache
    
    class CustomDeadboltHook extends Module {
        override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
            bind[HandlerCache].to[MyHandlerCache]
        )
    }
    
    
    ...and add it to your application.conf
    
    play {
        modules {
            enabled += be.objectify.deadbolt.scala.DeadboltModule
            enabled += com.example.modules.CustomDeadboltHook
        }
    }


## Using compile-time dependency injection

If you prefer to wire everything together with compile-time dependency, you don't need to create a custom module or add `DeadboltModule` to `play.modules`. 

Instead, dependencies are handled using a custom `ApplicationLoader`.  To make things easier, various Deadbolt components are made available via the `be.objectify.deadbolt.scala.DeadboltComponents` trait.  You will still need to provide a couple of things, such as your `HandlerCache` implementation, and you'll then have access to all the usual pieces of Deadbolt.  

Here's an example ApplicationLoader for compile-time DI.

    class CompileTimeDiApplicationLoader extends ApplicationLoader  {
      override def load(context: Context): Application 
                 = new ApplicationComponents(context).application
    }
    
    class ApplicationComponents(context: Context) 
                                extends BuiltInComponentsFromContext(context) 
                                with DeadboltComponents
                                with EhCacheComponents {
    
      // Define a pattern cache implementation
      // defaultCacheApi is a component from EhCacheComponents
      override lazy val patternCache: PatternCache = new DefaultPatternCache(defaultCacheApi)
    
      // Declare something required by MyHandlerCache
      lazy val subjectDao: SubjectDao = new TestSubjectDao
    
      // Specify the DeadboltHandler implementation to use
      override lazy val handlers: HandlerCache = new MyHandlerCache(subjectDao) 
    
      // everything from here down is application-level
      // configuration, unrelated to Deadbolt, such as controllers, routers, etc
      // ...
    }


The components provided by Deadbolt are

* `scalaAnalyzer` - constraint logic
* `deadboltActions` - for composing actions
* `actionBuilders` - for building actions
* `viewSupport` - for template constraints
* `patternCache` - for caching regular expressions.  You need to define this yourself in the application loader, but as in the example above it's easy to use the default implementation
* `handlers` - the implementation of `HandlerCache` that you provide
* `configuration` - the application configuration
* `ecContextProvider` - the execution context for concurrent operations.  Defaults to `scala.concurrent.ExecutionContext.global`
* `templateFailureListenerProvider` - for listening to Deadbolt-related errors that occur when rendering templates.  Defaults to a no-operation implementation

Once you've defined your `ApplicationLoader`, you need to add it to your `application.conf`.

    play {
      application {
        loader=com.example.myapp.CompileTimeDiApplicationLoader
      }
    }


Whichever injection approach you take, you're now ready to secure access to controller functions and templates in your Play 2 application.

## Controller constraints with the action builder

Using the `ActionsBuilders` class, you can quickly assemble constraints around your functions.  To get started, inject `ActionBuilders` into your controller.

    class ExampleController @Inject() (actionBuilder: ActionBuilders) extends Controller


You now have builders for all the constraint types, which we'll take a quick look at in a minute.  In the following examples I'm using the default handler, i.e. `.defaultHandler()` but it's also possible to use a different handler with `.key(HandlerKey)` or pass in a handler directly using `.withHandler(DeadboltHandler)`.

### SubjectPresent and SubjectNotPresent

Sometimes, you don't need fine-grained checks - you just need to see if there is a user present (or not present).

    // DeadboltHandler#getSubject must result in a Some for access to be granted
    def someFunctionA = actionBuilder.SubjectPresentAction().defaultHandler() { Ok(accessOk()) }
    
    // DeadboltHandler#getSubject must result in a None for access to be granted
    def someFunctionB = actionBuilder.SubjectNotPresentAction().defaultHandler() { Ok(accessOk()) }


### Restrict

This uses the `Subject`'s `Role`s to perform AND/OR/NOT checks.  The values given to the builder must match the `Role.name` of the subject's roles.

AND is defined as an `Array[String]` (or more correctly, `String*`, OR is a `List[Array[String]]`, and NOT is a rolename with a `!` preceding it.

    // subject must have the "foo" role 
    def restrictedFunctionA = actionBuilder.RestrictAction("foo").defaultHandler() { Ok(accessOk()) }
    
    // subject must have the "foo" AND "bar" roles 
    def restrictedFunctionB = actionBuilder.RestrictAction("foo", "bar").defaultHandler() { Ok(accessOk()) }
    
    // subject must have the "foo" OR "bar" roles 
    def restrictedFunctionC = actionBuilder.RestrictAction(List(Array("foo"), Array("bar"))).defaultHandler() { Ok(accessOk()) }


### Pattern

This uses the `Subject`'s `Permission`s to perform a variety of checks.  

    // subject must have a permission with the exact value "admin.printer" 
    def permittedFunctionA = actionBuilders.PatternAction("admin.printer", PatternType.EQUALITY).defaultHandler() { Ok(accessOk()) }
    
    // subject must have a permission that matches the regular expression (without quotes) "(.)*\.printer" 
    def permittedFunctionB = actionBuilders.PatternAction("(.)*\.printer", PatternType.REGEX).defaultHandler() { Ok(accessOk()) }
    
    // the checkPermssion function of the current handler's DynamicResourceHandler will be used.  This is a user-defined test 
    def permittedFunctionC = actionBuilders.PatternAction("something arbitrary", PatternType.CUSTOM).defaultHandler() { Ok(accessOk()) }


### Dynamic

The most flexible constraint - this is a completely user-defined constraint that uses `DynamicResourceHandler#isAllowed` to determine access.  


    def foo = actionBuilder.DynamicAction(name = "someClassifier").defaultHandler() { Ok(accessOk()) }


## Controller constraints with action composition

Using the `DeadboltActions` class, you can compose constrained functions.  To get started, inject `DeadboltActions` into your controller.


    class ExampleController @Inject() (deadbolt: DeadboltActions) extends Controller


You now have functions equivalent to those of the builders mentioned above.  In the following examples I'm using the default handler, i.e. no handler is specified, but it's also possible to use a different handler with `handler = &lt;some handler, possibly from the handler cache&gt;`.

### SubjectPresent and SubjectNotPresent

    // DeadboltHandler#getSubject must result in a Some for access to be granted
    def someFunctionA = deadbolt.SubjectPresent() {
        Action {
            Ok(accessOk())
        }
    }
    
    // DeadboltHandler#getSubject must result in a None for access to be granted
    def someFunctionB = deadbolt.SubjectNotPresent() {
        Action {
            Ok(accessOk())
        }
    }


### Restrict

    // subject must have the "foo" role 
    def restrictedFunctionA = deadbolt.Restrict(List(Array("foo")) {
        Action {
            Ok(accessOk())
        }
    }
    
    // subject must have the "foo" AND "bar" roles 
    def restrictedFunctionB = deadbolt.Restrict(List(Array("foo", "bar")) {
        Action {
            Ok(accessOk())
        }
    }
    
    // subject must have the "foo" OR "bar" roles 
    def restrictedFunctionC = deadbolt.Restrict(List(Array("foo"), Array("bar"))) {
        Action {
            Ok(accessOk())
        }
    }


### Pattern

    // subject must have a permission with the exact value "admin.printer" 
    def permittedFunctionA = deadbolt.Pattern("admin.printer", PatternType.EQUALITY) {
        Action {
            Ok(accessOk())
        }
    }
    
    // subject must have a permission that matches the regular expression (without quotes) "(.)*\.printer" 
    def permittedFunctionB = deadbolt.Pattern("(.)*\.printer", PatternType.REGEX) {
        Action {
            Ok(accessOk())
        }
    }
    
    // the checkPermssion function of the current handler's DynamicResourceHandler will be used.  This is a user-defined test in DynamicResourceHandler#checkPermission 
    def permittedFunctionC = deadbolt.Pattern("something arbitrary", PatternType.CUSTOM) {
        Action {
            Ok(accessOk())
        }
    }


### Dynamic

The most flexible constraint - this is a completely user-defined constraint that uses `DynamicResourceHandler#isAllowed` to determine access.  

    def foo = deadbolt.Dynamic(name = "someClassifier") {
        Action {
            Ok(accessOk())
        }
    }



## Template constraints

Using template constraints, you can exclude portions of templates from being generated on the server-side.  This is not a client-side DOM manipulation!  Template constraints have the same possibilities as controller constraints.  

By default, template constraints use the default Deadbolt handler but as with controller constraints you can pass in a specific handler.  The cleanest way to do this is to pass the handler into the template and then pass it into the constraints.  Another advantage of this approach is you can pass in a wrapped version of the handler that will cache the subject; if you have a lot of constraints in a template, this can yield a significant gain.

One important thing to note here is that templates are blocking, so any Futures used need to be completed for the resuly to be used in the template constraints.  As a result, each constraint can take a function that expresses a Long, which is the millisecond value of the timeout.  It defaults to 1000 milliseconds, but you can change this globally by setting the `deadbolt.scala.view-timeout` value in your `application.conf`.

Each constraint has a variant which allows you to define fallback content.  This comes in the format `&lt;constraintName&gt;Or`, e.g.

    @subjectPresentOr {
        this is protected
    } {
        this will be shown if the constraint blocks the other content
    }


### SubjectPresent

    @import be.objectify.deadbolt.scala.views.html.{subjectPresent, subjectPresentOr}
    
    @subjectPresent() {
        This content will be present if handler#getSubject results in a Some 
    }
    
    @subjectPresentOr() {
        This content will be present if handler#getSubject results in a Some 
    } {
        fallback content
    }


### SubjectNotPresent

    @import be.objectify.deadbolt.scala.views.html.{subjectNotPresent, subjectNotPresentOr}
    
    @subjectNotPresent() {
        This content will be present if handler#getSubject results in a None 
    }
    
    @subjectNotPresentOr() {
        This content will be present if handler#getSubject results in a None 
    } {
        fallback content
    }


### Restrict

    @import be.objectify.deadbolt.scala.views.html.{restrict, restrictOr}
    
    @restrict(roles = List(Array("foo", "bar"))) {
        Subject requires the foo role for this to be visible
    }
    
    @restrict(List(Array("foo", "bar")) {
         Subject requires the foo AND bar roles for this to be visible
    }
    
    @restrict(List(Array("foo"), Array("bar"))) {
         Subject requires the foo OR bar role for this to be visible
    }
    
    @restrictOr(List(Array("foo", "bar")) {
         Subject requires the foo AND bar roles for this to be visible
    } {
        Subject does not have the necessary roles
    }


### Pattern

The default pattern type is `PatternType.EQUALITY`.

    @import be.objectify.deadbolt.scala.views.html.{pattern, patternOr}
    
    @pattern("admin.printer") {
        Subject must have a permission with the exact value "admin.printer" for this to be visible
    }
    
    @pattern("(.)*\.printer", PatternType.REGEX) {
        Subject must have a permission that matches the regular expression (without quotes) "(.)*\.printer" for this to be visible
    }
    
    @pattern("something arbitrary", PatternType.CUSTOM) {
        DynamicResourceHandler#checkPermission must result in true for this to be visible
    }
    
    @patternOr("admin.printer") {
        Subject must have a permission with the exact value "admin.printer" for this to be visible
    } {
        Subject did not have necessary permissions
    }


### Dynamic

    @import be.objectify.deadbolt.scala.views.html.{dynamic, dynamicOr}
    
    @dynamic("someName") {
        DynamicResourceHandler#isAllowed must result in true for this to be visible
    }
    
    @dynamicOr("someName") {
        DynamicResourceHandler#isAllowed must result in true for this to be visible
    } {
        Custom test failed
    }


## Execution context<

By default, all futures are executed in the `scala.concurrent.ExecutionContext.global` context.  If you want to provide a separate execution context, you can plug it into Deadbolt by implementing the `DeadboltExecutionContextProvider` trait.

    import be.objectify.deadbolt.scala.DeadboltExecutionContextProvider
    
    class CustomDeadboltExecutionContextProvider extends DeadboltExecutionContextProvider {
        override def get(): ExecutionContext = ???
    }


**NB:** This provider is invoked twice, once in `DeadboltActions` and once in `ViewSupport`.

Once you've implemented the provider, you need to declare it in your custom module (see introduction above).

    class CustomDeadboltHook extends Module {
        override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
            bind[HandlerCache].to[MyHandlerCache],
            bind[DeadboltExecutionContextProvider].to[CustomDeadboltExecutionContextProvider]
        )
    }


## What next?
If you want to explore Deadbolt further, you might want to take a look at the book I'm currently writing on it.  You can find it at [https://leanpub.com/deadbolt-2](https://leanpub.com/deadbolt-2).

![Deadbolt 2 - Powerful authorization for your Play application](http://www.objectify.be/wordpress/wp-content/uploads/2015/09/large.jpg)

2.4.x Release notes
-------------------

Futures everywhere!  Well, almost...

**DeadboltHandler**
- Every function now returns a `Future`
- **NB** `beforeAuthCheck` previously returned `Option[Future[Result]]`.  To bring it in line with the rest of the trait, it now returns `Future[Option[Result]]`

**DynamicResourceHandler**
- Every function now returns a `Future`

**Views**
- there's an unfortunate need to use `Await.result` because we need ordered rendering.  If anyone knows a way around this, please let me know!
- as a result of this call to Await, there's a new template parameter called `timeout`.  This defaults to `1000` (i.e. 1 second), but you can specify your own value as required. 

**Controllers**

- You will need to inject `DeadboltActions` into controllers where you want to use action modifiers like `SubjectPresent`, `Restrict` and `Pattern`.

**application.conf**

- You need to enable the deadbolt-2 module as follows (add this line to `application.conf`)
 - `play.modules.enabled += "be.objectify.deadbolt.scala.DeadboltModule"`

- An example play 2.4 app can be found [here](https://github.com/schaloner/deadbolt-2-scala-examples)

2.3.3 Release notes
-------------------

The primary focus of 2.3.3 was to move to a non-blocking architecture; in the case of Deadbolt for Scala, this just means wrapping DynamicResourceHandler#isAllowed in a Future for non-view calls.

A simplification of structure was also needed, so a change to the DeadboltHandler interface in release 2.3.2 has been backed out; sorry about that.  Semantic versioning will continue shortly.

So, practical changes.

- DeadboltHandler#getSubject returns an Option[Subject] in place of an Future[Option[Subject]].  Where the subject is needed, the internal code will take care of wrapping the call in a Future.
- There are no more timeouts, so deadbolt.before-auth-check-timeout and deadbolt.get-subject-timeout are no longer needed.  If they're defined in your config, they'll be ignored.

**What kind of idiot makes API-level changes in a patch release?**

Me, I'm afraid.  This will probably be the last release of Deadbolt for Play 2.3 and I want to keep the major and minor version of Deadbolt locked into the Play versions.  The change in 2.3.2 was, overall, ill-considered and shouldn't have happened so I'm looking at this as a bug fix more than a change.
