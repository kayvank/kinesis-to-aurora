package repo

import org.specs2.mutable.Specification
import io.circe.parser._
import doobie.imports._
import scalaz._, Scalaz._
import scalaz._
import model._
import DomainProtocol._
import utils._

class UserLikesSpec extends Specification  {
  import DisjunctionToTask._

  "UserLikes Specs".title
  val unlikes_event =

    """
      |{
      |  "entity_type": "USER",
      |  "entity_id": "USUV71400762",
      |  "user_id": "26985937",
      |  "action": "UNLIKE"
      |}
    """.stripMargin
  val likes_event =

    """
      |{
      |  "entity_type": "USER",
      |  "entity_id": "USUV71400762",
      |  "user_id": "26985937",
      |  "action": "LIKE"
      |}
    """.stripMargin
  "Transformer will construct UserLike events from String" >> {
    val likeEvent = for {
      json <- parse(likes_event).asDisjunction
      like <- (json.as[UserLikeEvent]).asDisjunction
    } yield (like)
    println(s"${likeEvent}")
    likeEvent.toOption.isDefined &&
      UserLikeEvent(
        "LIKE",
        "USUV71400762",
        "26985937",
        "USER"
      ) === likeEvent.toOption.get
  }
  "repository should store a valid likeEvent" >> {

    val drop =
      sql"""
         drop TABLE  IF EXISTS like_events
  """.update.run.transact(Ds.hxa)
    val create =
      sql"""
         CREATE TABLE like_events (
         id varchar(40) not null PRIMARY KEY,
         user_id varchar(40) not null ,
         entity_id varchar(20),
         entity_type varchar(20),
         ts timestamp default current_timestamp,
         created_at DATETIME)
  """.update.run.transact(Ds.hxa)

    val unLikeEvent = UserLikeEvent(
      "UNLIKE",
      "USUV71400762",
      "26985937",
      "USER"
    )
    val likeEvent = UserLikeEvent(
      "LIKE",
      "USUV71400762",
      "26985937",
      "USER"
    )
    val computedTask = for {
      _ <- drop
      _ <- create
      a0 <- UserLikeRepository(Ds.hxa).processLikeEvent(likeEvent)(Ds.hxa)
//      a1 <- UserLikeRepository(Ds.hxa).processLikeEvent(likeEvent)(Ds.hxa)
      a2 <- UserLikeRepository(Ds.hxa).processLikeEvent(unLikeEvent)(Ds.hxa)
    } yield (a0  -a2)
      val computed = computedTask.run
    println(s"--- computed number = ${computed}")
   0 === computed
  }
}
