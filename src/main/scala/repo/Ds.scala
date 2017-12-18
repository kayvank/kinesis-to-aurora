package repo

import doobie.imports._
import scalaz._, Scalaz._
import scalaz.concurrent.Task
import utils.Global._
import doobie.contrib.hikari.hikaritransactor._

object Ds {
  private final val jdbcUrl = appCfg.getString("db.jdbc.url")
  private final val jdbcUser = appCfg.getString("db.jdbc.user")
  private final val jdbcPass = appCfg.getString("db.jdbc.password")
  private final val jdbcDriver = appCfg.getString("db.jdbc.driver")
  println(s"connecting to jdbcUrl = $jdbcUrl as jdbcUser=${jdbcUser}")

  implicit lazy val hxa: HikariTransactor[Task] =
    HikariTransactor[Task](
      jdbcDriver,
      jdbcUrl,
      jdbcUser,
      jdbcPass).unsafePerformSync

  val connectionPoolThreads = appCfg.getInt("db.connection.pool.threads")
  val _ = (hxa.configure(hx =>
    Task.delay(hx.setMaximumPoolSize(connectionPoolThreads)))).unsafePerformSync

  def connectionStatus: String = {
    val program3 = sql"select 42".query[Int].unique
    (program3.transact(hxa).unsafePerformSync == 42) ? "up" | "down"

  }
  }
