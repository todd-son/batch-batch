package com.kakao.bcbc.learn.ch03

import akka.actor.{Actor, ActorRef, Props}
import FilteringActor.FilterEvent

object FilteringActor {
  def props(receiver: ActorRef, bufferSize: Int) = Props(new FilteringActor(receiver, bufferSize))

  case class FilterEvent(num: Int)
}

class FilteringActor(receiver: ActorRef, bufferSize: Int) extends Actor {
  var lastMessages = Vector[FilterEvent]()

  override def receive: Receive = {
    case msg : FilterEvent => {
      if (!lastMessages.contains(msg)) {
        lastMessages = lastMessages :+ msg
        receiver ! msg

        if (lastMessages.size > bufferSize) {
          lastMessages = lastMessages.tail
        }
      }
    }
  }
}
