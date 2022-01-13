package controllers

import cats.effect.IO
import doobie.util.transactor.Transactor.Aux
import models.other.WebserverHttpStatus
import models.entities.LogData
import models.enums.DataMapping._
import modules.{DatabaseConnector, ElkSession, SessionModule}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request}
import services.{DBUtil, IntegrationDataDBAction, LogDataDBAction}

import java.time.{LocalDateTime, ZoneOffset}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import LogData.writes
import models.enums.LogDataType
import models.enums.LogDataType._

import scala.collection.immutable.{AbstractMap, SeqMap, SortedMap}

@Singleton
class ElkDataController @Inject()
(controllerComponents: ControllerComponents, db: DatabaseConnector, session: SessionModule)
(implicit ec: ExecutionContext) extends ElkBaseController(controllerComponents, session)(ec) {

  implicit val dbConnection: Aux[IO, Unit] = db.connection
  // TODO: ADD TIMEOUT TO SET INTEGRATION TO FAILED IF NO DATA RECEIVED WITHIN N MINUTES
  // TIMEOUT => FUN => RET
  def receiveLogData(): Action[AnyContent] = isAuthenticated()({ case (req: Request[AnyContent], json: JsValue) =>
    val elkSession = session.getSession(req.headers.get("AuthToken").getOrElse(""))
    val response = json.validate[LogData] match {
      case JsSuccess(logData, _) =>
        logData.dataType match {
          case STARTED =>
            val serverName = logData.data.getOrElse(SERVERNAME, "")
            for {
              a <- IntegrationDataDBAction.startNewIntegration(serverName, elkSession.fold("")(_.username), logData.timestamp, LogDataType.encode(logData.dataType))
              updatedSession = session.updateElkSession(req.headers.get("AuthToken").getOrElse(""), a.getOrElse(-1)) // mm sideeffect, TODO: fix getOrElse
              b <- IntegrationDataDBAction.updateIntegrationStatus(updatedSession.fold(0.toLong)(_.integrationId.getOrElse(0.toLong)), "RUNNING")
              res <- if (a.isRight && b.isRight) IO.pure(b) else DBUtil.unifyErrors[Int](List(a, b))
            } yield res
          case FINISHED => IntegrationDataDBAction.finishIntegration(elkSession.fold(0.toLong)(_.integrationId.getOrElse(0.toLong)), "FINISHED", logData)
          case EXITED => IntegrationDataDBAction.finishIntegration(elkSession.fold(0.toLong)(_.integrationId.getOrElse(0.toLong)), "EXITED", logData)
          case ERROR => LogDataDBAction.insertIntoLogData(logData, elkSession.fold(0.toLong)(_.integrationId.getOrElse(0.toLong))) // INTEGRATION SHOULD BE STOPPED ON ERROR? ADD BOOL?
          case RUNNING =>
            //LogDataDBAction.insertIntoLogData(logData, elkSession.fold(0.toLong)(_.integrationId.getOrElse(0.toLong)))
            elkSession match {
              case Some(value) =>
                processLogData(logData, value)
              case None =>IO.pure(Left(WebserverHttpStatus.STATUS_400.addLineToMessage("NO SESSION"))) // TODO: FIX
            }
          case _ =>
            //LogDataDBAction.insertIntoLogData(logData, elkSession.fold(0.toLong)(_.integrationId.getOrElse(0.toLong)))
            elkSession match {
              case Some(value) =>
                processLogData(logData, value)
              case None =>IO.pure(Left(WebserverHttpStatus.STATUS_400.addLineToMessage("NO SESSION"))) // TODO: FIX
            }
        }
      case JsError(errors) =>
        IO.pure(Left(WebserverHttpStatus.STATUS_400.addLineToMessage(errors.toString())))
    }

    response.unsafeToFuture().flatMap {
      case Right(v) =>
        Future {
          Ok {
            Json.obj (
              "token" -> elkSession.fold("")(_.token),
              "data" -> Json.toJson(LogData(LogDataType.INFO, Map(MESSAGE -> v.toString), LocalDateTime.now().toInstant(ZoneOffset.UTC).getEpochSecond))
            )
          }
        }
      case Left(e) =>
        Future {
          Ok {
            Json.obj (
              "token" -> elkSession.fold("")(_.token),
              "data" -> Json.toJson(LogData(LogDataType.ERROR, Map(MESSAGE -> e.status.apply(e.toString).toString()), LocalDateTime.now().toInstant(ZoneOffset.UTC).getEpochSecond))
            )
          }
        }
    }
  })

  def processLogData(logData: LogData, elkSession: ElkSession) = {

    val message = logData.data.map {
      case (k, v) =>
        k match {
          case SERVERNAME => v
          case MESSAGE => v
          case OTHER => v
          case META => v
          case UNDEFINED => v
          case TOTAL => v
          case PROGRESS => v
        }
    }

    println(logData.toString)
    LogDataDBAction.insertIntoLogData(logData, message.headOption.getOrElse(""), elkSession.integrationId.fold(0.toLong)(x => x))
  }

}