package models.rest

import play.api.libs.json.{JsValue, Json, Reads, Writes}

case class RequestWrapper(token: String, data: Option[JsValue]) // just lazy, should be changed to entity
object RequestWrapper {
  implicit val requestWrapperReads: Reads[RequestWrapper] = Json.reads[RequestWrapper]
  implicit val requestWrapperWrites: Writes[RequestWrapper] = Json.writes[RequestWrapper]
}