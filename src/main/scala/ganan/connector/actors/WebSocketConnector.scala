package ganan.connector.actors

import java.util.UUID

import akka.actor.{ActorRef, PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import ganan.common.BaseActor
import ganan.common.messages.{Bootup, Shutdown}
import ganan.connector.messages.Protocol
import ganan.connector.messages.Protocol.{CloseConnection, OpenConnection, SignedMessage}

import scala.concurrent.Future

/**
  * 基于 WebSocket 的连接器
  *
  * Created by Bob Jiang on 2017/4/9.
  */
class WebSocketConnector
(
  val host: String, val port: Int
) extends BaseActor {

  implicit private val system = context.system
  implicit private val executor = system.dispatcher
  implicit private val materializer = ActorMaterializer()

  override def receive: Receive = process(Map.empty[UUID, ActorRef], 1)

  /**
    * 服务消息的处理器
    * @param clients      当前已连接的客户端
    * @param limit        当前服务最大承担的客户端上限
    * @return
    */
  private def process(clients: Map[UUID, ActorRef], limit: Int): Receive = {

    case SignedMessage(uuid, msg) => clients.collect {
      case (id, clientAr) if id == uuid => clientAr ! msg
    }

    case OpenConnection(ar, _) if clients.size >= limit => ar ! PoisonPill

    case OpenConnection(ar, uuid) =>
      val clientAr = context.actorOf(ClientActor.props(uuid, ar), uuid.toString)
      context.become(process(clients.updated(uuid, clientAr), limit))

    case CloseConnection(uuid) => context.become(process(clients - uuid, limit))

    case Bootup =>
      // 启动

    case Shutdown =>
      // 停止
      bindFuture.flatMap(_.unbind())

    case _ =>
  }

  private def websocketFlow: Flow[Message, Message, Any] =
    Flow[Message].mapAsync(1) {
      case TextMessage.Strict(s) => Future.successful(s)
      case TextMessage.Streamed(s) => s.runFold("")(_ + _)
      case b: BinaryMessage => throw new Exception("Binary message cannot be handled")
    }.via(actorFlow(UUID.randomUUID())).map(TextMessage(_))

  private def actorFlow(connectionId: UUID): Flow[String, String, Any] = {
    val sink =
      Flow[String].map(msg => Protocol.SignedMessage(connectionId, msg))
        .to(Sink.actorRef(self, Protocol.CloseConnection(connectionId)))

    val source = Source.actorRef(16, OverflowStrategy.fail)
      .mapMaterializedValue {
        actor: ActorRef => self ! Protocol.OpenConnection(actor, connectionId)
      }

    Flow.fromSinkAndSource(sink, source)
  }

  private val route = path("game") {
    get {
      handleWebSocketMessages(websocketFlow)
    }
  }

  private val bindFuture = Http(system).bindAndHandle(route, host, port)

  log.info("WebSocket 服务：{}:{} 成功启动", host, port)

  self ! Bootup
}

object WebSocketConnector {
  def props(host: String, port: Int): Props = Props(classOf[WebSocketConnector], host, port)
}
