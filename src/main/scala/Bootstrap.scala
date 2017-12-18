import api.StatusApi
import com.amazonaws.services.kinesis.clientlibrary.exceptions.KinesisClientLibNonRetryableException
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import kamon.Kamon
import kcl.KclCfg._
import kcl.KclCheckpoint._
import kcl.{KclCfg, KclEndoStreamFactory}
import org.http4s.server.ServerApp
import org.http4s.server.blaze.BlazeBuilder
import repo._
import scalaz._
import scalaz.concurrent.Task

object Bootstrap extends ServerApp {

  Kamon.start()

  val awsKclworker = new Worker.Builder()
    .recordProcessorFactory(new KclEndoStreamFactory(
      UserLikeRepository.apply(Ds.hxa).eventSink,
      doCheckPointTask))
    .config(KclCfg.streamClientMap(likeStream))
    .build

  def startKcl = Task.delay(
    awsKclworker.run()
  )

  def server(args: List[String]) = {
    for {
      s <- BlazeBuilder.bindHttp(port = 9000, host = "0.0.0.0")
        .mountService(StatusApi.service, "/status").start
      _ = startKcl.unsafePerformAsync {
        case -\/(e: KinesisClientLibNonRetryableException) =>
          e.printStackTrace
          awsKclworker.shutdown
          System.exit(-1)
        case _ =>
      }
    } yield (s)
  }
}
