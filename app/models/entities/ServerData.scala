package models.entities

import play.api.libs.json.{Json, Reads, Writes}

case class ServerData(id: Long)
object ServerData {
  implicit val serverDataReads: Reads[ServerData] = Json.reads[ServerData]
  implicit val serverDataWrites: Writes[ServerData] = Json.writes[ServerData]
}
