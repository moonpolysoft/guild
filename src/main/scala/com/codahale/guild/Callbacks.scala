package com.codahale.guild

import org.jetlang.core.Callback
import org.jetlang.channels.{AsyncRequest, Request, MemoryRequestChannel}
import java.util.concurrent.TimeUnit
import concurrent.forkjoin.{TransferQueue, LinkedTransferQueue}
import scala.reflect.Manifest

/**
 * A callback wrapper which sends a message to an actor and relays its reply
 * to the scheduler.
 */
private case class RequestCallback[M, R](actor: ActorBehavior[M,R]) extends Callback[Request[M,R]] {
  def onMessage(message: Request[M,R]) {
    message.reply(actor.onMessage(message.getRequest))
  }
}

/**
 * A callback wrapper which relays the reply to the receiver.
 */
private case class ReplyCallback[R](queue: TransferQueue[R]) extends Callback[R] {
  def onMessage(reply: R) {
    queue.put(reply)
  }
}

private case class ActorInit(actor : ActorBehavior[_,_]) extends Runnable {
  def run() {
    actor.onStart
  }
}

private case class PollQueue[M,R](actor : ActorBehavior[M,R], queue : TransferQueue[Message[M]]) extends Runnable {
  def run() {
    try {
      queue.poll(Long.MaxValue, TimeUnit.DAYS) match {
        case m : AsyncMessage[_] => dispatchAsync(m.asInstanceOf[AsyncMessage[M]])
        case m : CallMessage[_,_] => dispatch(m.asInstanceOf[CallMessage[M,R]])
      }
    } catch {
      case e : InterruptedException => run
    }
  }
  
  private def dispatchAsync(message : AsyncMessage[M]) {
    actor.onMessage(message.msg)
  }
  
  private def dispatch(message : CallMessage[M,R]) {
    message.replyQueue.put(actor.onMessage(message.msg))
  }
}

private case class AsyncCallback[M,R](actor : ActorBehavior[M,R]) extends Callback[M] {
  def onMessage(msg : M) {
    actor.onMessage(msg)
  }
}

/**
 * A callback wrapper which sends a message to an actor.
 */
private case class ActorExecution[M,R](actor: ActorBehavior[M,R], msg: M) extends Runnable {
  def run() {
    actor.onMessage(msg)
  }
}