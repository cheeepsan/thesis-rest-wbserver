package modules

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID
import scala.collection.mutable.Map
import scala.concurrent.Future

// https://github.com/pedrorijo91/play-auth-example/blob/master/app/models/SessionDAO.scala

case class ElkSession(token: String, username: String, expiration: LocalDateTime, integrationId: Option[Long] = None) {
  // expiration should be within token

  //TODO: def startTimer = ???
}

@Singleton
class SessionModule @Inject()(lifecycle: ApplicationLifecycle, config: Configuration) {

  var sessionMap: Map[String, ElkSession] = Map.empty // change to db?

  lifecycle.addStopHook { () =>
    // clean session
    Future.successful("")
  }

  def generateToken(username: String): String = {
    val token = s"$username-token-${UUID.randomUUID().toString}"
    sessionMap.put(token, ElkSession(token, username, LocalDateTime.now(ZoneOffset.UTC).plusSeconds(30)))

    token
  }

  def getSession(token: String): Option[ElkSession] = {
    sessionMap.get(token)
  }

  def updateElkSession(token: String, integrationId: Int): Option[ElkSession] = {
    getSession(token).map{
      elkSession =>
        val updatedSession = elkSession.copy(integrationId = Option(integrationId))
        // TODO: updatedSession.startTimer
        sessionMap.update(token, updatedSession)
        updatedSession
    }
  }

}
