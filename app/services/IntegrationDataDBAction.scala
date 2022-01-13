package services

import akka.http.scaladsl.model.DateTime
import cats.effect.IO
import doobie.util.transactor.Transactor
import doobie.implicits._
import models.entities.LogData
import models.other.WebserverHttpStatus
import cats.implicits._
import cats.data._
import doobie.postgres.implicits._

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global

object IntegrationDataDBAction {
  def startNewIntegration(serverName: String, username: String, timestamp: Long, status: String)(implicit xa: Transactor[IO]): IO[Either[WebserverHttpStatus, Int]] = {
    val fServerId = ServerDataDBAction.fetchServerIdByName(serverName)
    val fStatusId = StatusDataDBAction.fetchStatusIdByName(status)
    val fUserId = UserDataDBAction.fetchUserIdByUsername(username)

    (fServerId, fStatusId, fUserId).mapN {
      case (se, st, u) =>
        if (se.isRight && st.isRight && u.isRight) {
          val t = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)
          sql"""INSERT INTO elk_log_integration (time_started, status, elk_log_user, elk_log_server)
               values (${t}, ${se.getOrElse(0)}, ${st.getOrElse(0)}, ${u.getOrElse(0)})""".update.withUniqueGeneratedKeys[Int]("id").transact(xa).attempt.map {
            case Left(e) => Left(WebserverHttpStatus.STATUS_500.addLineToMessage(e.toString))
            case Right(v) => Right(v)
          }
        } else {
          DBUtil.unifyErrors[Int](List(se, st, u), "Error while fetching server data:")
        }
    }.flatten
  }

  def updateIntegrationStatus(integrationId: Long, status: String)(implicit xa: Transactor[IO]): IO[Either[WebserverHttpStatus, Int]] = {
    val fStatusId = StatusDataDBAction.fetchStatusIdByName(status)
    fStatusId.map {
      statusId =>
        if (statusId.isRight) {
          sql"""UPDATE elk_log_integration SET status = ${statusId.getOrElse(0)} WHERE id = $integrationId""".update.run.transact(xa).attempt.map {
            case Left(e) => Left(WebserverHttpStatus.STATUS_500.addLineToMessage(e.toString))
            case Right(v) => Right(v)
          }
        } else IO.pure(statusId) // WHY?
    }.flatten // TODO Fix this (through traverse?)
  }
  def finishIntegration(integrationId: Long, status: String, logData: LogData)(implicit xa: Transactor[IO]): IO[Either[WebserverHttpStatus, Int]] = {
    val fStatusId = StatusDataDBAction.fetchStatusIdByName(status)
    fStatusId.map {
      statusId =>
        if (statusId.isRight) {
          val t = LocalDateTime.ofEpochSecond(logData.timestamp, 0, ZoneOffset.UTC)
          sql"""UPDATE elk_log_integration SET status = ${statusId.getOrElse(0)}, time_finished = ${t} WHERE id = $integrationId""".update.run.transact(xa).attempt.map {
            case Left(e) => Left(WebserverHttpStatus.STATUS_500.addLineToMessage(e.toString))
            case Right(v) => Right(v)
          }
        } else IO.pure(statusId)
    }.flatten
  }


}
