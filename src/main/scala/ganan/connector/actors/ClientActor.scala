package ganan.connector.actors

import java.util.UUID

import akka.actor.{ActorRef, PoisonPill, Props}
import ganan.common.BaseActor
import ganan.connector.messages.Protocol.CloseConnection

/**
  * Created by Bob Jiang on 2017/4/9.
  */
class ClientActor
(
  val sessionId: UUID,
  val streamAr: ActorRef
) extends BaseActor {

  override def receive: Receive = {
    case "shutdown" =>
      log.debug("收到结束消息：shutdown")

      context stop self

    case message: String =>
      log.debug("收到消息：{}", message)

      streamAr ! "ECHO: " + message

    case _ =>
  }

  override def postStop(): Unit = {
    streamAr ! PoisonPill
    context.parent ! CloseConnection(sessionId)

    super.postStop()
  }
}

object ClientActor {
  def props(sessionId: UUID, streamAr: ActorRef): Props = Props(classOf[ClientActor], sessionId, streamAr)
}
