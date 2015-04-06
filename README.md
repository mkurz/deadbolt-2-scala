deadbolt-2-scala
================

[![Build Status](https://travis-ci.org/schaloner/deadbolt-2-scala.svg?branch=2.3.x)](https://travis-ci.org/schaloner/deadbolt-2-scala)

Idiomatic Scala API for Deadbolt 2, an authorisation module for Play 2.

Deadbolt 2 comprises of several modules - a common core, and language-specific implementations for Java and Scala.  Example applications and a user guide are also available.  

All modules related to Deadbolt 2, including the user guide, are grouped together in the [Deadbolt 2](https://github.com/schaloner/deadbolt-2) Github super-module.  Installation information, including Deadbolt/Play compatibility, can also be found here.

2.3.3 Release notes
-------------------

The primary focus of 2.3.3 was to move to a non-blocking architecture; in the case of Deadbolt for Scala, this just means wrapping DynamicResourceHandler#isAllowed in a Future for non-view calls.

A simplification of structure was also needed, so a change to the DeadboltHandler interface in release 2.3.2 has been backed out; sorry about that.  Semantic versioning will continue shortly.

So, practical changes.

- DeadboltHandler#getSubject returns a Subject in place of an F.Promise<Subject>.  Where the subject is needed, the internal code will take care of wrapping the call in a Promise.
- There are no more timeouts, so deadbolt.before-auth-check-timeout and deadbolt.get-subject-timeout are no longer needed.  If they're defined in your config, they'll be ignored.

**What kind of idiot makes API-level changes in a patch release?**

Me, I'm afraid.  This will probably be the last release of Deadbolt for Play 2.3 and I want to keep the major and minor version of Deadbolt locked into the Play versions.  The change in 2.3.2 was, overall, ill-considered and shouldn't have happened so I'm looking at this as a bug fix more than a change.
