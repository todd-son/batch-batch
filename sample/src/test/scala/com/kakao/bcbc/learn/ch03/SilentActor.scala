package com.kakao.bcbc.learn.ch03

import akka.actor.{Actor, ActorRef}
import SilentActor.{GetState, SilentMessage}

object SilentActor {
  case class SilentMessage(data: String)

  case class GetState(receiver: ActorRef)
}

class SilentActor extends Actor {
  var internalState = Vector[String]()

  override def receive: Receive = {
    case SilentMessage(data) =>
      internalState = internalState :+ data
    case GetState(receiver) =>
      receiver ! internalState
  }

  def state = internalState
}