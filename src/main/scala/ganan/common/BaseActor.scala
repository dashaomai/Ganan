package ganan.common

import akka.actor.Actor
import org.slf4j.{Logger, LoggerFactory}

/**
  * 框架内 Actor 公用基础类
  * Created by Bob Jiang on 2017/4/9.
  */
trait BaseActor extends Actor {
  protected val log: Logger = LoggerFactory.getLogger(this.getClass)
}
