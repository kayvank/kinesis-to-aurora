package utils

import scala.util.Either
import scalaz.concurrent.Task
import scalaz._

object DisjunctionToTask {

  implicit class EitherToDisjunction[A, B](_either: Either[A, B]) {
    def asDisjunction =
      _either match {
        case Right(b) => \/-(b)
        case Left(a) => -\/(a)
      }
  }

  implicit class EitherToTask[A](_either: \/[Exception, A]) {
    def asTask =
      _either match {
        case \/-(b) => Task(b)
        case -\/(e) => Task.fail(e)
      }
  }

}

