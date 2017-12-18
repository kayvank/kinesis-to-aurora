package kcl

import com.amazonaws.services.kinesis.clientlibrary.exceptions._
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor
import com.amazonaws.services.kinesis.clientlibrary.types.{InitializationInput, ProcessRecordsInput, ShutdownInput, ShutdownReason}
import com.typesafe.scalalogging.LazyLogging
import io.circe.DecodingFailure
import utils._
import scala.collection.JavaConversions._
import scalaz.concurrent.Task

class KclProcessor(
  eventSink: ProcessRecordsInput => Task[Int],
  checkpoint: IRecordProcessorCheckpointer => Task[Long]
) extends IRecordProcessor
  with LazyLogging {

  import KclCfg._

  override def shutdown(shutdownInput: ShutdownInput) = {
    logger.info(s"Ending kcl stream processing!")
    shutdownInput.getShutdownReason match {
      case ShutdownReason.TERMINATE =>
        logger.warn(s"Received Shard:  ${ShutdownReason.TERMINATE} for stream= $likeStream}")
        checkpoint(shutdownInput.getCheckpointer).run // dont wait, do checkpoint now
      case ShutdownReason.ZOMBIE =>
        logger.warn(s"Received Shard: ${ShutdownReason.ZOMBIE} for stream= $likeStream}   ")
    }
  }

  override def initialize(initializationInput: InitializationInput): Unit = {
    logger.info(s"kcl stream processing begins")
  }

  override def processRecords(processRecordsInput: ProcessRecordsInput): Unit = {

    val recordsProcessedTask =
      Task.fork(eventSink(processRecordsInput)).handleWith {
        case e: DecodingFailure =>
          logger.warn(s"DecodingFailure.  ${e.toString}")
          checkpoint(processRecordsInput.getCheckpointer)
        case e: KinesisClientLibRetryableException =>
          logger.info(s"KinesisClientLibRetryableException.  ${e.getMessage}")
          Task(())
        case e: KinesisClientLibException =>
          logger.info(s"KinesisClientLibException.  ${e.getMessage}")
          Task(())
      }
    val recordsProcessed = recordsProcessedTask.unsafePerformSync

    logger.info(s"processed kcl records=${recordsProcessed}")
  }
}
