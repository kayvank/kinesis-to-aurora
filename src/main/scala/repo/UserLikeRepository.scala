package repo

import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput
import scalaz._
import Scalaz._
import doobie.contrib.hikari.hikaritransactor.{HikariTransactor, _}
import doobie.imports._
import scala.collection.JavaConversions._
import scalaz.concurrent.Task
import model.{InvalidUserLikeException, UserLikeEvent}
import svc.KclToUserLikesTransformer._
import utils.IdGen.idGen


object UserLikeRepository {
  def apply(hxa: HikariTransactor[Task]): UserLikeRepository =
    new UserLikeRepository(hxa)
}

class UserLikeRepository(hxa: HikariTransactor[Task]) {
  val eventSink: ProcessRecordsInput => Task[Int] = kclUserLikesRecords =>
    for {
      l <- kclUserLikesRecords.getRecords.toList.asUserLikeEventTasks
      l1 <- l.map(processLikeEvent(_)(hxa)).sequence
    } yield (
      l1.foldLeft(0)(_ + _)
      )

  val insert: UserLikeEvent => ConnectionIO[Int] = e =>
    sql"""insert into like_events
          (
         id,
         user_id,
         entity_id,
         entity_type,
         ts,
         created_at
         ) values(
         ${idGen(s"${e.user_id}${e.entity_id}${e.entity_type}")},
         ${e.user_id},
         ${e.entity_id},
         ${e.entity_type},
         ${new java.sql.Timestamp(System.currentTimeMillis)},
         ${new java.sql.Timestamp(System.currentTimeMillis)}
         )""".update.run

  val delete: UserLikeEvent => ConnectionIO[Int] = e =>
    sql"""delete from like_events where
          id = ${idGen(s"${e.user_id}${e.entity_id}${e.entity_type}")}
         """.update.run

  def processLikeEvent(userLikeEvent: UserLikeEvent): HikariTransactor[Task] => Task[Int] =
    (tx: HikariTransactor[Task]) =>
      userLikeEvent.action.toUpperCase match {
        case "LIKE" => insert(userLikeEvent).transact(tx)
        case "UNLIKE" => delete(userLikeEvent).transact(tx)
        case _ => Task.fail(InvalidUserLikeException(
          s"${userLikeEvent.action} is invalid for the objec ${userLikeEvent})")
        )
      }
}
