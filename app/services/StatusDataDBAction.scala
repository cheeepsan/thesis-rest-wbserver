package services

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor
import models.other.WebserverHttpStatus

object StatusDataDBAction {
  def fetchStatusIdByName(statusName: String)(implicit xa: Transactor[IO]): IO[Either[WebserverHttpStatus, Int]] = {
    sql"""SELECT id FROM elk_integration_status where status=$statusName""".query[Int]
      .unique.transact(xa).attempt.map {
      case Left(e) => Left(WebserverHttpStatus.STATUS_500.addLineToMessage(e.toString))
      case Right(statusId) => Right(statusId)
    }
  }

}
