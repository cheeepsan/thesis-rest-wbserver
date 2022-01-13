package models.enums

import models.enums
import play.api.libs.json.{Reads, Writes}

object DataMapping extends Enumeration {
  type DataMapping = Value

  val SERVERNAME, MESSAGE, OTHER, META, UNDEFINED, TOTAL, PROGRESS = Value

  def decode(s: String): DataMapping.Value = s match {
    case "SERVERNAME" => DataMapping.SERVERNAME
    case "MESSAGE"    => DataMapping.MESSAGE
    case "OTHER"      => DataMapping.OTHER
    case "TOTAL"      => DataMapping.TOTAL
    case "PROGRESS"   => DataMapping.PROGRESS
    case "META"       => DataMapping.META
    case _            => DataMapping.UNDEFINED
  }

  def encode(e: DataMapping.Value): String = e match {
    case SERVERNAME => "SERVERNAME"
    case MESSAGE    => "MESSAGE"
    case OTHER      => "OTHER"
    case TOTAL      => "TOTAL"
    case PROGRESS   => "PROGRESS"
    case META       => "META"
    case UNDEFINED  => "UNDEFINED"
  }
}
