Guild
=====

*Simple, fast actors for Scala.*


Requirements
------------

* Java SE 6
* Scala 2.8 Beta1
* JetLang 0.2.0


How To Use
----------

**First**, specify Metrics as a dependency:

    val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
    val guild = "com.codahale" % "guild_2.8.0.Beta1" % "0.1.0" withSources()

(Or whatever gets Ivy or Maven to work for you.)

**Second**, write an actor:
    
    import com.codahale.guild.Actor
    
    class MyActor extends Actor[Any, Any] {
      def onMessage(msg: Any) = {
        msg match {
          case i: Int => i + i
          case s: String => println("Hello " + s)
        }
      }
    }

(Yes, that's it. No partially applied functions, just a method.)

**Third**, start the actor:

    val actor = new MyActor
    actor.start()
    
**Fourth**, send the actor a message asynchronously:
    
    // prints out "Hello world!"
    actor.send("world!")
    
**Fifth**, synchronously send the actor a message and get a response:
    
    // prints out "40"
    println(actor.call(20))
    
(No, there isn't a `!` method or a `!?` method or a `\m/` method. If you feel
that they add clarity to your code, feel free to add them to a trait and extend
your actor classes with them.)

**For your Asynchronous Message-Passing Merit Badge**, check out actor pools,
which dispatch messages to a pool of actor workers:
    
    import com.codahale.guild.{Actor, ActorFactory, ActorPool}
    
    class StringCounter extends Actor[String, Int] {
      def onMessage(s: String) = s.length
    }
    
    class StringCounterFactory extends ActorFactory[String, Int, StringCounter] {
      def createActor() = new StringCounter
    }
    
    val factory = new StringCounterFactory
    val counterPool = new ActorPool(factory)
    
    pool.start(4) // start up 4 instances of StringCounter
    pool.call("yesssss")
    pool.stop()


License
-------

Copyright (c) 2010 Coda Hale; Published under The MIT License, see LICENSE.