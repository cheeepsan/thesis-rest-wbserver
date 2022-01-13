package models.entities

import play.api.libs.json.{Json, Reads, Writes}

object User {
  implicit val userReads: Reads[User] = Json.reads[User]
  implicit val userWrites: Writes[User] = Json.writes[User]
}
case class User(id: Option[Long] = None, username: String, password: String)


