package ganan.connector.messages

import java.util.UUID

import akka.actor.ActorRef

/**
  * 与外界的通讯协议
  * Created by Bob Jiang on 2017/4/9.
  */
object Protocol {
  final case class SignedMessage(uuid: UUID, msg: String)
  final case class OpenConnection(actor: ActorRef, uuid: UUID)
  final case class CloseConnection(uuid: UUID)
}
