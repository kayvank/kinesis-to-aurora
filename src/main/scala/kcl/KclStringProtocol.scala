package kcl



import com.amazonaws.services.kinesis.model.Record

import scala.util.{Failure, Success, Try}
import scalaz.Scalaz._
import scalaz._

object KclStringProtocol {

  implicit class KclStringProtocol(event: Record) {

    def asString: \/[Exception, String] =
      Try(new java.lang.String(
        (event.getData.array()), "utf-8")) match {
        case Success(s) => s.right
        case Failure(e) => new Exception(e).left
      }
  }
}
