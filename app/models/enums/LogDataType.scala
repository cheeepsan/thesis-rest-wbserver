package models.enums

import io.circe.{Decoder, Encoder}

object LogDataType extends Enumeration {
  self =>

  type LogDataType = Value
  val ERROR, INFO, STARTED, FINISHED, EXITED, RUNNING = Value

  def encode(e: LogDataType.Value): String = {
    e match {
      case ERROR    => "ERROR"
      case INFO     => "INFO"
      case STARTED  => "STARTED"
      case FINISHED => "FINISHED"
      case EXITED   => "EXITED"
      case RUNNING  => "RUNNING"
    }
  }

  def decode(s: String): LogDataType.Value = s match {
    case "ERROR"    =>  ERROR
    case "INFO"     =>  INFO
    case "STARTED"  =>  STARTED
    case "FINISHED" =>  FINISHED
    case "EXITED"   =>  EXITED
    case "RUNNING"  =>  RUNNING
  }

  implicit val logDataTypeDecoder: Decoder[LogDataType.Value] = Decoder.decodeEnumeration(LogDataType)
  implicit val logDataTypeEncoder: Encoder[LogDataType.Value] = Encoder.encodeEnumeration(LogDataType)
}
