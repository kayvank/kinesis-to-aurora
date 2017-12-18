package model

object DomainProtocol {

  import io.circe.generic.semiauto._
  import io.circe._

  implicit val LikeEventDecoder: Decoder[UserLikeEvent] = deriveDecoder
  implicit val LikeEventEncoder: Encoder[UserLikeEvent] = deriveEncoder
}
