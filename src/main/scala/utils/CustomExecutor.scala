package utils

import java.util.concurrent.Executors
import com.typesafe.scalalogging.LazyLogging

sealed trait CustomExecutor

final object CustomExecutor extends CustomExecutor with LazyLogging {

  import Global._
val cfgThreadPool = appCfg.getConfig("thread.pool")
  final val numberOfThreads = 2

  final val customExecutor: java.util.concurrent.ExecutorService =
    Executors.newFixedThreadPool(numberOfThreads)

}
