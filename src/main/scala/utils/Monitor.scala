package utils

import kamon.Kamon

object Monitor {

  val counterDecodingFailure =
    Kamon.metrics.counter("endo-qos-decoding-failure")

  val counterEndoWithQos =
    Kamon.metrics.counter("endo-with-qos-counter")

  val histogramEndoWithQos =
    Kamon.metrics.histogram("endo-with-qos-histogram")

  val gaugeEndoWithQos =
    Kamon.metrics.gauge("endo-with-qos-gauge")(0L)

  val counterEndoWithNoQos =
    Kamon.metrics.counter("endo-with-no-qos-counter")

  val histogramEndoWithNoQos =
    Kamon.metrics.histogram("endo-with-no-qos-histogram")

  val gaugeEndoWithNoQos =
    Kamon.metrics.gauge("ndo-with-no-qos-gauge")(0L)





  def incCounterDecodingFailure() =
    counterDecodingFailure.increment


  val updateAllEndoWithQos: () => Unit = () => {
    val currentMillis = System.currentTimeMillis
    counterEndoWithQos.increment
    histogramEndoWithQos.record(currentMillis % 1000)
    gaugeEndoWithQos.record(currentMillis % 1000)
  }



  val updateAllEndoWithNoQos: () => Unit = () => {
    val currentMillis = System.currentTimeMillis
    counterEndoWithNoQos.increment
    histogramEndoWithNoQos.record(currentMillis % 1000)
    gaugeEndoWithNoQos.record(currentMillis % 1000)
  }
}
