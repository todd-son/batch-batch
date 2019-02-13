package com.kakao.bcbc.learn.ch03

class MixedInTest extends MixedIn {
  this: Super =>

  override def name(): Unit = super.name()
}


trait MixedIn {
  this: Super =>

  def name() = println(this.getClass)
}

trait Super {

}
