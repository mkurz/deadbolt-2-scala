DeadBolt 2 Scala - Quick start
==============================

Add the dependency to your build

    `"be.objectify" %% "deadbolt-scala" % "2.4.0-SNAPSHOT"`

Don't forget to add the snapshot repository, if you don't already have it.

    resolvers += Resolver.sonatypeRepo("snapshots")

Add the Deadbolt module to your Play application

    play {
    	modules {
    		enabled += be.objectify.deadbolt.scala.DeadboltModule
    	}
    }


Implement the `Subject`, `Role` and `Permission` traits.

- `Subject` represents, typically, a user
- A `Role` is a single system privilege, e.g. **admin**, **user** and so on.  A subject can have zero or more roles.
- A `Permission` is a can be used with regular expression matching, e.g. a subject with a permission of `printers.admin` can access a resource constrained to `printers.*`, `*.admin`, etc.    A subject can have zero or more permissions.

Implement the `be.objectify.deadbolt.scala.DeadboltHandler` trait.  This implementation (or implementations - you can more than one) is used to

 - get the current user - `getSubject`
 - run a pre-authorization task that can block further execution - `beforeAuthCheck`
 - handle authorization failure - `onAuthFailure`
 - provide a hook into the dynamic constraint types - `getDynamicResourceHandler`

You only need to implement `be.objectify.deadbolt.scala.DynamicResourceHandler` if you're planning to use `Dynamic` or `Pattern.CUSTOM` constraints.  This will be covered in detail in another section.


Implement the `be.objectify.deadbolt.scala.HandlerCache` trait.  This is used by Deadbolt to obtain instances of `DeadboltHandler`s, and has two concepts

1. A default handler.  You can always use a specific handler in a template or controller, but if nothing is specified a well-known instance will be used.
2. Named handlers.  

An example implementation follows, based on the sample app.  You can also use `be.objectify.deadbolt.scala.cache.DefaultHandlerCache`.

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

You're now ready to secure access to controller functions and templates in your Play 2 application.

Controller constraints with the action builder
==============================================

Using the ActionsBuilders class, you can quickly assemble constraints around your functions.  To get started, inject ActionBuilders into your controller.

    class ExampleController @Inject() (actionBuilder: ActionBuilders) extends Controller

You now have builders for all the constraint types, which we'll take a quick look at in a minute.  In the following examples I'm using the default handler, i.e. `.defaultHandler()` but it's also possible to use a different handler with `.key(HandlerKey)` or pass in a handler directly using `.withHandler(DeadboltHandler)`.

**SubjectPresent** and **SubjectNotPresent**

Sometimes, you don't need fine-grained checked - you just need to see if there is a user present (or not present)

    // DeadboltHandler#getSubject must result in a Some for access to be granted
    def someFunctionA = actionBuilder.SubjectPresentAction().defaultHandler() { Ok(accessOk()) }

    // DeadboltHandler#getSubject must result in a None for access to be granted
    def someFunctionB = actionBuilder.SubjectNotPresentAction().defaultHandler() { Ok(accessOk()) }

**Restrict**

This uses the `Subject`s `Role`s to perform AND/OR/NOT checks.  The values given to the builder must match the `Role.name` of the subject's roles.

AND is defined as an `Array[String]` (or more correctly, `String*`, OR is a `List[Array[String]]`, and NOT is a rolename with a `!` preceding it.

    // subject must have the "foo" role 
    def restrictedFunctionA = actionBuilder.RestrictAction("foo").defaultHandler() { Ok(accessOk()) }

    // subject must have the "foo" AND "bar" roles 
    def restrictedFunctionB = actionBuilder.RestrictAction("foo", "bar").defaultHandler() { Ok(accessOk()) }

    // subject must have the "foo" OR "bar" roles 
    def restrictedFunctionC = actionBuilder.RestrictAction(List(Array("foo"), Array("bar"))).defaultHandler() { Ok(accessOk()) }

**Pattern**

This uses the `Subject`s `Permission`s to perform a variety of checks.  

    // subject must have a permission with the exact value "admin.printer" 
    def permittedFunctionA = actionBuilders.PatternAction("admin.printer", PatternType.EQUALITY).defaultHandler() { Ok(accessOk()) }

    // subject must have a permission that matches the regular expression (without quotes) "(.)*\.printer" 
    def permittedFunctionB = actionBuilders.PatternAction("(.)*\.printer", PatternType.REGEX).defaultHandler() { Ok(accessOk()) }

    // the checkPermssion function of the current handler's DynamicResourceHandler will be used.  This is a user-defined test 
    def permittedFunctionC = actionBuilders.PatternAction("something arbitrary", PatternType.CUSTOM).defaultHandler() { Ok(accessOk()) }


**Dynamic**

The most flexible constraint - this is a completely user-defined constraint that uses `DynamicResourceHandler#isAllowed` to determine access.  

    def foo = actionBuilder.DynamicAction(name = "someClassifier").defaultHandler() { Ok(accessOk()) }

Controller constraints with action composition
==============================================

Using the DeadboltActions class, you can compose constrained functions.  To get started, inject DeadboltActions into your controller.

    class ExampleController @Inject() (deadbolt: DeadboltActions) extends Controller

You now have functions equivalent to those of the builders mentioned above.  In the following examples I'm using the default handler, i.e. no handler is specified, but it's also possible to use a different handler with `handler = <some handler, possibly from the handler cache>`.

**SubjectPresent** and **SubjectNotPresent**

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

**Restrict**

    // subject must have the "foo" role 
    def restrictedFunctionA = deadbolt.Restrict(List(Array("foo")) {
    	Action {
    		Ok(accessOk())
    	}
    }

    // subject must have the "foo" AND "bar" roles 
    def restrictedFunctionB = deadbolt.Restrict(List(Array["foo", "bar"]) {
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

**Pattern**

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


**Dynamic**

The most flexible constraint - this is a completely user-defined constraint that uses `DynamicResourceHandler#isAllowed` to determine access.  

    def foo = actionBuilder.DynamicAction(name = "someClassifier").defaultHandler() { Ok(accessOk()) }


Template constraints
====================

Using template constraints, you can exclude portions of templates from being generated on the server-side.  This is not a client-side DOM manipulation!  Template constraints have the same possibilities as controller constraints.  

By default, template constraints use the default Deadbolt handler but as with controller constraints you can pass in a specific handler.  The cleanest way to do this is to pass the handler into the template and then pass it into the constraints.  Another advantage of this approach is you can pass in a wrapped version of the handler that will cache the subject; if you have a lot of constraints in a template, this can yield a significant gain.

One important thing to note here is that templates are blocking, so any Futures used need to be completed for the resuly to be used in the template constraints.  As a result, each constraint can take a function that expresses a Long, which is the millisecond value of the timeout.  It defaults to 1000 milliseconds, but you can change this globally by setting the `deadbolt.scala.view-timeout` value in your `application.conf`.

Each constraint has a variant which allows you to define fallback content.  This comes in the format `<constraintName>Or`, e.g.

    @subjectPresentOr {
    	this is protected
    } {
    	this will be shown if the constraint blocks the other content
    }

**SubjectPresent**

    @subjectPresent() {
        This content will be present if handler#getSubject results in a Some 
    }
    
    @subjectPresentOr() {
        This content will be present if handler#getSubject results in a Some 
    } {
    	fallback content
    }

**SubjectNotPresent**

    @subjectNotPresent() {
        This content will be present if handler#getSubject results in a None 
    }
    
    @subjectNotPresentOr() {
        This content will be present if handler#getSubject results in a None 
    } {
    	fallback content
    }

**Restrict**

    @restrict(roles = List(Array("foo", "bar"))) {
        Subject requires the foo role for this to be visible
    }
    
    @restrict(List(Array["foo", "bar"]) {
         Subject requires the foo AND bar roles for this to be visible
    }
    
    @restrict(List(Array("foo"), Array("bar"))) {
         Subject requires the foo OR bar role for this to be visible
    }
    
    @restrictOr(List(Array["foo", "bar"]) {
         Subject requires the foo AND bar roles for this to be visible
    } {
    	Subject does not have the necessary roles
    }


**Pattern**

 The default pattern type is `PatternType.EQUALITY`.

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

**Dynamic**

    @dynamic("someName") {
        DynamicResourceHandler#isAllowed must result in true for this to be visible
    }
    
    @dynamicOr("someName") {
        DynamicResourceHandler#isAllowed must result in true for this to be visible
    } {
    	Custom test failed
    }

