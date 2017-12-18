package kcl

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.{IRecordProcessor, IRecordProcessorFactory}
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput
import scalaz.concurrent.Task


class KclEndoStreamFactory(
  eventSink: ProcessRecordsInput => Task[Int],
  checkpoint: IRecordProcessorCheckpointer => Task[Long])
  extends IRecordProcessorFactory {

  @Override
  def createProcessor: IRecordProcessor = {
    new KclProcessor(eventSink, checkpoint)
  }
}
