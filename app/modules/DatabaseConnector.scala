package modules

import com.google.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future
import doobie._
import cats.effect._
import doobie.util.transactor.Transactor.Aux
import play.api.Configuration

@Singleton
class DatabaseConnector @Inject()(lifecycle: ApplicationLifecycle, config: Configuration) {

  private val dbUrl = config.get[String]("db.url")
  private val dbUser = config.get[String]("db.user")
  private val dbPass = config.get[String]("db.pass")

  lifecycle.addStopHook { () =>
    Future.successful(())
  }

  def connection: Aux[IO, Unit] = {
    implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

    Transactor.fromDriverManager[IO](
      "org.postgresql.Driver", // driver classname
      dbUrl, // connect URL (driver-specific)
      dbUser, // user
      dbPass // password
      //, Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
    )
  }

}
