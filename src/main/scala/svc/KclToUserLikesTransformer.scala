package svc

import com.amazonaws.services.kinesis.model.Record
import io.circe.parser._
import scalaz._, Scalaz._
import scalaz.concurrent.Task
import model._

object KclToUserLikesTransformer {

  implicit class KclRecordsToUserLikes(events: List[Record]) {

    import DomainProtocol._
    import utils.DisjunctionToTask._
    import kcl.KclStringProtocol._

    val asUserLikeEventTask: Record => Task[UserLikeEvent] = event => {
      val likeEvent = for {
        str <- event.asString
        json <- parse(str).asDisjunction
        like <- (json.as[UserLikeEvent]).asDisjunction
      } yield (like)
      likeEvent.asTask
    }

    def asUserLikeEventTasks: Task[List[UserLikeEvent]] =
      events map asUserLikeEventTask sequence
  }
}
