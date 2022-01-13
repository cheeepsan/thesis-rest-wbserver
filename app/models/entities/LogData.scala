package models.entities

import models.enums.{DataMapping, LogDataType}
import models.enums.LogDataType.LogDataType
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.json._
import play.api.libs.functional.syntax._
case class LogData(dataType: LogDataType, data: Map[DataMapping.Value, String], timestamp: Long)
object LogData {

  implicit val writes: Writes[LogData] = new Writes[LogData] {
    override def writes(logData: LogData): JsValue = {
      val data = logData.data.map{
        case (k, v) => (DataMapping.encode(k) -> v)
      }

      Json.obj(
        "dataType" -> LogDataType.encode(logData.dataType),
        "data" -> data,
        "timestamp" -> logData.timestamp
      )
    }
  }

  implicit val logDataReads: Reads[LogData] = (
    (JsPath \ "dataType").read[String].map(d => LogDataType.decode(d)) and
      (JsPath \ "data").read[Map[String, String]].map {
        d =>
         d.map {case (e, v) =>
            (DataMapping.decode(e) -> v)
          }
      } and
      (JsPath \ "timestamp").read[Long]
    )(LogData.apply _)
}
