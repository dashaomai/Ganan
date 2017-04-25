package ganan

import akka.actor.ActorSystem
import ganan.connector.actors.WebSocketConnector

/**
  * Created by Bob Jiang on 2017/4/9.
  */
object LauncherApp extends App {
  private val system = ActorSystem("ganan")
  system.actorOf(WebSocketConnector.props("127.0.0.1", 9595))

  private val log = system.log

  Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
    override def run(): Unit = {
      log.info("系统即将关闭")

      system.terminate()
    }
  }))
}
