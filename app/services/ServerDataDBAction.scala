package services

import cats.effect.IO
import doobie.util.transactor.Transactor
import doobie.implicits._
import models.entities.ServerData
import models.other.WebserverHttpStatus

object ServerDataDBAction {
  def fetchServerIdByName(serverName: String)(implicit xa: Transactor[IO]): IO[Either[WebserverHttpStatus, Int]] = {
    sql"""SELECT id FROM elk_log_server_data where server_name=$serverName""".query[Int]
      .unique.transact(xa).attempt.map {
      case Left(e) => Left(WebserverHttpStatus.STATUS_500.addLineToMessage(e.toString))
      case Right(serverId) => Right(serverId)
    }
  }

}
