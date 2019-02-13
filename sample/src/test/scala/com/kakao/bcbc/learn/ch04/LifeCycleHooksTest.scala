package com.kakao.bcbc.learn.ch04

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.TestKit
import com.kakao.bcbc.learn.ch03.StopSystemAfterAll
import org.scalatest.WordSpecLike

class LifeCycleHooksTest extends TestKit(ActorSystem("testSystem")) with WordSpecLike with StopSystemAfterAll {
  "Life Cycle Hook Actor" must {
    "Simple" in {
      val testActorRef = system.actorOf(Props[LifeCycleHooks], "LifeCycleHooks")
      testActorRef ! "restart"
      testActorRef.tell("msg", testActor)
      expectMsg("msg")
      system.stop(testActorRef)
      Thread.sleep(10000)
    }
  }
}

class LifeCycleHooks extends Actor with ActorLogging {
  println("Constructor")

  override def preStart(): Unit = println("preStart")

  override def postStop(): Unit = println("postStop")

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("preRestart")
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable): Unit = {
    println("postRestart")
    super.postRestart(reason)
  }

  override def receive: Receive = {
    case "restart" =>
      throw new IllegalArgumentException("force restart")
    case msg =>
      println("Receive")
      sender() ! msg
  }
}
