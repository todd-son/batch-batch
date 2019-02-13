package com.kakao.bcbc.learn.ch03

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, UnhandledMessage}
import akka.testkit.{CallingThreadDispatcher, EventFilter, TestKit}
import com.kakao.bcbc.learn.ch03.GreeterActor.Greeting
import com.kakao.bcbc.learn.ch03.GreeterTest.testSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.WordSpecLike

class GreeterTest extends TestKit(testSystem) with WordSpecLike with StopSystemAfterAll {
  "The Greeter" must {
    """say Hello World! when a Greeting("world") is send to it""" in {
      val dispatcherId = CallingThreadDispatcher.Id
      val props = Props[GreeterActor].withDispatcher(dispatcherId)
      val greeter = system.actorOf(props, "greeter-1")

      EventFilter.info(message = "Hello World!", occurrences = 1)
        .intercept(
          greeter ! Greeting("World")
        )
    }

    """say Hello World! when a Greeting("world") is send to it using listenr""" in {
      val props = GreeterActor2.props(Some(testActor))
      val greeter = system.actorOf(props, "greeter-2")

      greeter ! Greeting("World")

      expectMsg("Hello World!")
    }

    """say something else and what happens""" in {
      val props = GreeterActor2.props(Some(testActor))
      val greeter = system.actorOf(props, "greeter-3")

      system.eventStream.subscribe(testActor, classOf[UnhandledMessage])
      greeter ! "World"
      expectMsg(UnhandledMessage("World", system.deadLetters, greeter))
    }
  }
}

object GreeterTest {
  val testSystem = {
    val config = ConfigFactory.parseString(
      """
        akka.loggers = [akka.testkit.TestEventListener]
      """
    )

    ActorSystem("testSystem", config)
  }
}

object GreeterActor {
  case class Greeting(message: String)
}

class GreeterActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case Greeting(msg) => log.info("Hello {}!", msg)
  }
}

object GreeterActor2 {
  def props(listener: Option[ActorRef] = None) = Props(new GreeterActor2(listener))
}

class GreeterActor2(listener: Option[ActorRef]) extends Actor with ActorLogging {
  override def receive: Receive = {
    case Greeting(msg) => {
      val message = s"Hello ${msg}!"
      log.info(message)
      listener.foreach(_ ! message)
    }

  }
}
