package controllers

import models.entities.LogData
import models.enums.DataMapping.MESSAGE
import models.enums.LogDataType
import models.enums.StateFlag.{AUTHENTICATION, DEFAULT, StateFlag}
import models.other.WebserverHttpStatus
import modules.SessionModule
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request, Result}

import java.time.{LocalDateTime, ZoneOffset}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ElkBaseController @Inject()
(val controllerComponents: ControllerComponents, sessionModule: SessionModule)
(implicit val ec: ExecutionContext) extends BaseController {

  def requestWrapper(f: => (Request[AnyContent], JsValue) => Future[Result])(implicit req: Request[AnyContent]): Future[Result] = {
        val default: Future[Result] = Future.successful(WebserverHttpStatus.STATUS_400.status)

        if (req.hasBody) {
          req.body.asJson.fold(default) {
            json =>
              f(req, json)
          }
        } else {
          default
        }
  }

  def isAuthenticated(flag: StateFlag = DEFAULT)
                     (f: => (Request[AnyContent], JsValue) => Future[Result]): Action[AnyContent]  = {
    Action.async {
      implicit req: Request[AnyContent] =>
        val authorized = req.headers.get("AuthToken").fold(false) {
          s =>
            if(sessionModule.getSession(s).isDefined) true else false
        }

        if (authorized || flag == AUTHENTICATION)
          requestWrapper(f)
        else {
          Future{
            Ok {
              Json.obj(
                "token" -> "",
                "data" -> Json.toJson(LogData(LogDataType.ERROR, Map(MESSAGE -> WebserverHttpStatus.STATUS_500.addLineToMessage("Not auth").status.toString()), LocalDateTime.now().toInstant(ZoneOffset.UTC).getEpochSecond))
              )
            }
          }
        }
    }
  }
}