package kcl


import java.util.concurrent.atomic.AtomicLong

import com.amazonaws.services.kinesis.clientlibrary.exceptions.KinesisClientLibRetryableException
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer
import utils.Global

import scala.util.{Failure, Success, Try}
import scalaz.Scalaz._
import scalaz._
import scalaz.concurrent.Task

object KclCheckpoint extends KclCheckpoint {

  val maxCpRetries: Int = 7
  val cpCheckpointIntervalMillis =
    Global.appCfg.getInt("kinesis.streams.like.time.minutes.checkpoints") * 60 * 1000
}

trait KclCheckpoint {
  val maxCpRetries: Int
  val cpCheckpointIntervalMillis: Int
  val nextCheckPoint: AtomicLong =
    new AtomicLong(System.currentTimeMillis + (cpCheckpointIntervalMillis))

  val doCheckPoint: IRecordProcessorCheckpointer => \/[Throwable, Long] = checkpointer => {

    def checkpoint(checkpointer: IRecordProcessorCheckpointer,
      iteration: Int): \/[Throwable, Long] = {
      Try(checkpointer.checkpoint()) match {
        case Failure(e) if (
          e.isInstanceOf[KinesisClientLibRetryableException]
            && iteration < maxCpRetries) =>
          checkpoint(checkpointer, iteration + 1)
        case Failure(e) => -\/(e)
        case Success(()) =>
          nextCheckPoint.set(System.currentTimeMillis + (cpCheckpointIntervalMillis)) //TODO bad side effect. will clean up
          \/-(System.currentTimeMillis)
      }
    }

    if (nextCheckPoint.get < System.currentTimeMillis)
      checkpoint(checkpointer, 0)
    else nextCheckPoint.get.right
  }

  val doCheckPointTask: IRecordProcessorCheckpointer => Task[Long] = checkpointer => {
    doCheckPoint(checkpointer) match {
      case \/-(s) => Task(s)
      case -\/(e) => Task.fail(e)
    }
  }
}
