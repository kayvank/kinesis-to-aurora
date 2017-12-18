package api

import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import cats.syntax.either._
import io.circe._, io.circe.parser._
import repo.Ds._
object StatusApi extends BaseApi {
  def apply(): HttpService = service

  val service = HttpService {
    case GET -> Root =>
      Ok(
        parse(info.BuildInfo.toJson).getOrElse(Json.Null).deepMerge(
          Map("data-source-status" -> connectionStatus).asJson) )
  }
}
