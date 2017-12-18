package model

sealed trait Domain

final case class UserLikeEvent(
  action: String, // LIKE or UNLIKE
  entity_id: String,
  user_id: String,
  entity_type: String
) extends Domain

case class InvalidUserLikeException(
  message: String = "Invalid UserLikeEvent"
) extends Exception with Domain
