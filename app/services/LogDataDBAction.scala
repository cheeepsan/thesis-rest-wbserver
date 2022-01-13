package services

import cats.effect.IO
import doobie.util.transactor.Transactor
import doobie.implicits._
import models.entities.LogData
import models.enums.DataMapping.MESSAGE
import models.other.WebserverHttpStatus
import doobie.postgres.implicits._

import java.time.{LocalDateTime, ZoneOffset}
import cats.implicits._
import models.enums.LogDataType

object LogDataDBAction {


  def insertIntoLogData(logData: LogData, elkIntegration: Long)(implicit xa: Transactor[IO]): IO[Either[WebserverHttpStatus, Int]] = {
    val fStatusId = StatusDataDBAction.fetchStatusIdByName(LogDataType.encode(logData.dataType))
    val message = logData.data.getOrElse(MESSAGE, "")
    val timestamp = LocalDateTime.ofEpochSecond(logData.timestamp, 0, ZoneOffset.UTC)
    /*
      TODO: datatype to id
     */
    fStatusId.map {
      statusId =>
        if (statusId.isRight) {
          sql"""INSERT INTO elk_log_data (data, status, elk_integration, timestamp) values ($message, ${statusId.getOrElse(0)}, $elkIntegration, $timestamp)""".update.run.transact(xa).attempt.map {
            case Left(e) => Left(WebserverHttpStatus.STATUS_500.addLineToMessage(e.toString))
            case Right(v) => Right(v)
          }
        } else IO.pure(statusId)
    }.flatten
  }

  def insertIntoLogData(logData: LogData, message: String, elkIntegration: Long)(implicit xa: Transactor[IO]): IO[Either[WebserverHttpStatus, Int]] = {
    val fStatusId = StatusDataDBAction.fetchStatusIdByName(LogDataType.encode(logData.dataType))
    val timestamp = LocalDateTime.ofEpochSecond(logData.timestamp, 0, ZoneOffset.UTC)
    /*
      TODO: datatype to id
     */
    fStatusId.map {
      statusId =>
        if (statusId.isRight) {
          sql"""INSERT INTO elk_log_data (data, status, elk_integration, timestamp) values ($message, ${statusId.getOrElse(0)}, $elkIntegration, $timestamp)""".update.run.transact(xa).attempt.map {
            case Left(e) => Left(WebserverHttpStatus.STATUS_500.addLineToMessage(e.toString))
            case Right(v) => Right(v)
          }
        } else IO.pure(statusId)
    }.flatten
  }

}
