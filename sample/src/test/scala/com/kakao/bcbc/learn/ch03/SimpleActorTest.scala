package com.kakao.bcbc.learn.ch03

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit}
import FilteringActor.FilterEvent
import SendingActor.{Event, SortEvents, SortedEvents}
import SilentActor.{GetState, SilentMessage}
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.util.Random

class SimpleActorTest extends TestKit(ActorSystem("testSystem"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  "A Silent Actor" must {
    "change internal state when it receives a message, single-threaded" in {
      val silentActor = TestActorRef[SilentActor]

      silentActor ! SilentMessage("whisper")
      silentActor.underlyingActor.state must (contain("whisper"))
    }

    "change internal state when it receives a message, multi-threaded" in {
      val silentActor = system.actorOf(Props[SilentActor], "s3")
      silentActor ! SilentMessage("whisper1")
      silentActor ! SilentMessage("whisper2")
      silentActor ! GetState(testActor)
      expectMsg(Vector("whisper1", "whisper2"))
    }
  }

  "A Sending Actor" must {
    "send a message to another actor when it has finished processing" in {
      val props = SendingActor.props(testActor)
      val sendingActor = system.actorOf(props, "sendingActor")

      val size = 1000
      val maxInclusive = 10000

      def randomEvents() = (0 until size).map { _ =>
        Event(Random.nextInt(maxInclusive))
      }.toVector

      val unsorted = randomEvents()
      val sortEvents = SortEvents(unsorted)

      sendingActor ! sortEvents

      expectMsgPF() {
        case SortedEvents(events) =>
          events.size must be(size)
          unsorted.sortBy(_.id) must be(events)

      }
    }
  }

  "A Filtering Actor" must {
    "filter out particular messages" in {
      val props = FilteringActor.props(testActor, 5)
      val filter = system.actorOf(props, "filter-1")

      filter ! FilterEvent(1)
      filter ! FilterEvent(2)
      filter ! FilterEvent(1)
      filter ! FilterEvent(3)
      filter ! FilterEvent(1)
      filter ! FilterEvent(4)
      filter ! FilterEvent(5)
      filter ! FilterEvent(6)

      val eventIds = receiveWhile() {
        case FilterEvent(id) if id <= 5 => id
      }

      eventIds must be(List(1, 2, 3, 4, 5))
      expectMsg(FilterEvent(6))
    }

    "filter out particular messages using expectNoMsg" in {
      val props = FilteringActor.props(testActor, 5)
      val filter = system.actorOf(props, "filter-2")

      filter ! FilterEvent(1)
      filter ! FilterEvent(2)
      expectMsg(FilterEvent(1))
      expectMsg(FilterEvent(2))

      filter ! FilterEvent(1)
      expectNoMessage()

      filter ! FilterEvent(3)
      expectMsg(FilterEvent(3))

      filter ! FilterEvent(1)
      expectNoMessage()
    }
  }

}








