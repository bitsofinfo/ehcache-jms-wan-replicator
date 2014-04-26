ehcache-jms-wan-replicator
====================

This is customizable module that builds upon Ehcache's replication framework (specifically the JMS Replicated Caching module) that allows for lighter weight cache events to be disseminated across different data-centers in a hub/spoke pub-sub model w/ batching. First implementation leverages Nevado over AWS SNS/SQS

Caveat: this has yet to be used in a production environment, but is currently being evaluated/tested as part of a larger project where it (or something like it) might be levereged.

What does this add to Ehcache JMS replication?
----------------------
* Decorates the OUTBOUND JMS Messages with additional custom key-value pairs (strings)
* Defines primitive JMS selectors to control which INBOUND replication messages are actually processed
* Adds batching of events in a simplified JMS TextMessage optimized for sending hundreds of events in one message; works much better with SNS/SQS JMS backends. (avoiding Java serialization based ObjectMessages when possible)
* Permit totally overriding how local Ehcache events actions are to be replicated. For example saying a local PUT will be replicated to the other DC's as a EXPIRE, or a PUT will do *NOTHING*, or an UPDATE yields a REMOVE etc. Ehcache already gives you some hooks for via *replicatePuts*, *replicatePutsViaCopy* and *replicateUpdatesViaCopy*, but you may want to completey override everthing as you wish, and this module lets you do that explicitly.

Background
-----------------
The original need for this little module came out of a need ensure that caches across a GSLB'd (globally load balanced) application could be kept reasonably in "sync" without having to push the actual cached data around across WANs and allow the user to configure more explicitly what local events actions get translated to for cross-dc event replication. The specific need is for an app that uses Ehcache internally for caching various data points.

In the use-case that drove this little test; *within any given data-center* that runs the app, Ehcache is already configured to use the existing Ehcache replication facilities (RMI/JGroups) for *within-DC* peer node replication. Generally, for cache puts/updates Ehcache always includes the actual cached data in its replication. Note that this is configurable via the *replicatePuts*, *replicatePutsViaCopy* and *replicateUpdatesViaCopy* etc to force *removes* on update/put.

Point being, that for a different data-center across the world, there is really no need to send that cached data over the wire; or even go so far as to just not send anything (i.e. a PUT might warrant nothing happening) Instead you really want to be able to fully override the replication behavior per local DC action. So in the case of a local update forcing a dc-replication cache *remove*, then just let the next request for the data trigger a cache-miss and repopulate the latest data from the shared data-source (which is replicated/clustered via a totally separate process). The overall *speed* of how soon a cache event, triggerring a *remove* in another DC actually happens.... basically comes down to the principal of eventual-consistency. Everyones individual requirements will vary, but my requirement is basically within a few minutes; which is better than forcing disseparate DC's to rely soley on TTLs which might be much much longer.

So what does this all mean in how it relates to this chunk of code? In a nutshell this code allows you to define separate *cacheManagerPeerProviderFactories* and *cacheEventListenerFactories* that build upon the Ehcache JMS Replication functionality, but specifically adds the following features, key do doing whats described above. 

* Decorate the OUTBOUND JMS Messages with additional custom key-value pairs (strings)
* Define primitive JMS selectors to control which INBOUND replication messages are actually processed
* Support batching of events in a simplified JMS TextMessage optimized for sending hundreds of events in one message; works much better with SNS/SQS JMS backends. (avoiding Java serialization based ObjectMessages when possible)
* Permit totally overriding how local Ehcache events actions are to be replicated. For example saying a local PUT will be replicated to the other DC's as a EXPIRE, or a PUT will do *NOTHING*, or an UPDATE yields a REMOVE etc. Ehcache already gives you some hooks for via *replicatePuts*, *replicatePutsViaCopy* and *replicateUpdatesViaCopy*, but you may want to completey override everthing as you wish, and this module lets you do that explicitly.

With the above three features you can then configure the JMS replication to ignore messages generated from the local DC (via selector and custom replication message properties) and publish messages to other DC's that are lighterweight (i.e. we only tell other DC's to REMOVE for any cache event that occurs locally)

Think of this as a *second DC to DC cluster layer* that is entirely separate from your *within DC echache replication*. See diagram below

This implementation was specifically built around using [Nevado JMS](https://github.com/skyscreamer/nevado "Nevado JMS") which lets us leverage AWS (SNS/SQS) for the globally available "topic" that all DC's subscribe too.

Getting Started
-----------------

* Look at the [Unit Test](https://github.com/bitsofinfo/ehcache-jms-wan-replicator/blob/master/src/test/java/org/bitsofnfo/ehcache/jms/custom/EhcacheNevadoJMSTest.java "link") that boots up 3 separate instances of Ehcache and validates the behavior of "updates" resulting in "removes" on the other "dc instances" and utilizes the batching behavior.
* Check at the [echcache.xml example config file](https://github.com/bitsofinfo/ehcache-jms-wan-replicator/blob/master/src/test/resources/ehcache.xml "config") 
* You will need an AWS account and access to SNS/SQS, define a topic there and get an accessKey/secretKey that need to go into the peer provider configuration. See the ehcache.xml example files.

If this evolves I'll update this w/ more info.

Reference
------------

* http://bitsofinfo.wordpress.com/2014/04/21/ehcache-replicated-caching-with-jms-aws-sqs-sns-nevado/
* http://bitsofinfo.wordpress.com/2014/04/22/part-2-nevado-jms-ehcache-jms-wan-replication-and-aws/
* http://ehcache.org/documentation/get-started/cache-topologies
* http://ehcache.org/documentation/replication/index
* http://ehcache.org/documentation/replication/jms-replicated-caching
* https://github.com/skyscreamer/nevado
* http://nevado.skyscreamer.org/
* http://aws.amazon.com/sns/
* http://aws.amazon.com/sqs/

author: bitsofinfo.g[at]gmail.com


![Alt text](/docs/diagram.png "Diagram")
