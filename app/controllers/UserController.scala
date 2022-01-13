package controllers

import cats.effect.IO
import doobie.util.transactor.Transactor.Aux
import modules.{DatabaseConnector, SessionModule}
import models.entities.User
import models.enums.StateFlag.AUTHENTICATION
import models.other.WebserverHttpStatus
import models.rest.RequestWrapper

import javax.inject._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import services.UserDataDBAction

import scala.concurrent.{ExecutionContext, Future}
import RequestWrapper.requestWrapperWrites
@Singleton
class UserController @Inject()
(controllerComponents: ControllerComponents, db: DatabaseConnector, session: SessionModule)
(implicit ec: ExecutionContext) extends ElkBaseController(controllerComponents, session)(ec) {

  implicit val dbConnection: Aux[IO, Unit] = db.connection

  def index: Action[AnyContent] = Action.async { implicit req: Request[AnyContent] =>
    val a = UserDataDBAction.fetchAllUsers

    Future(Ok(a.toString))
  }

  def login: Action[AnyContent] = isAuthenticated(AUTHENTICATION)( { case (_, json: JsValue)  =>
    val response = json.validate[User] match {
      case JsSuccess(user, _) =>
        UserDataDBAction.fetchUserByUsername(user.username)
      case JsError(errors) =>
        IO.pure(Left(WebserverHttpStatus.STATUS_400.addLineToMessage(errors.toString())))
    }

    response.unsafeToFuture().flatMap {
      case Right(u) => Future(Ok(Json.toJson(RequestWrapper(session.generateToken(u.username), None))))
      case Left(e) => Future(e.status.apply(e.toString))
    }
  })

  def addUser: Action[AnyContent] = isAuthenticated()({ case (_, json: JsValue) =>
    Future(Ok(""))
  })
}

