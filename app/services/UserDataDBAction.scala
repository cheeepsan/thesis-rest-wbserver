package services

import cats.effect.IO
import doobie.util.transactor.Transactor
import doobie.implicits._
import models.entities.User
import models.other.WebserverHttpStatus

object UserDataDBAction {
  def fetchAllUsers(implicit xa: Transactor[IO]): List[User] = {
    sql"SELECT * FROM elk_log_user".query[User]    // Query0[String]
      .stream           // Stream[ConnectionIO, String]
      .compile.toList   // ConnectionIO[List[String]]
      .transact(xa)     // IO[List[String]]
      .unsafeRunSync()    // List[String]
  }

  def fetchUserByUsername(username: String)(implicit xa: Transactor[IO]): IO[Either[WebserverHttpStatus, User]] = {
    sql"""SELECT * FROM elk_log_user where username=$username""".query[User]
      .unique.transact(xa).attempt.map {
      case Left(e) => Left(WebserverHttpStatus.STATUS_500.addLineToMessage(e.toString))
      case Right(user) => Right(user)
    }
  }
  def fetchUserIdByUsername(username: String)(implicit xa: Transactor[IO]): IO[Either[WebserverHttpStatus, Int]] = {
    sql"""SELECT id FROM elk_log_user where username=$username""".query[Int]
      .unique.transact(xa).attempt.map {
      case Left(e) => Left(WebserverHttpStatus.STATUS_500.addLineToMessage(e.toString))
      case Right(user) => Right(user)
    }
  }
}
