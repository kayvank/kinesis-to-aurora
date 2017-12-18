package utils

import com.typesafe.config.ConfigFactory

object Global {

  val cfg = ConfigFactory.load
  val appCfg = cfg.getConfig("vevo")
}
