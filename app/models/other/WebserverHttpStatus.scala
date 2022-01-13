package models.other

import play.api.mvc.Results.Status
import play.mvc.Http

case class WebserverHttpStatus(status: Status, message: String) {
  self =>
  def addLineToMessage(additionalLine: String): WebserverHttpStatus = {
    self.copy(message = self.message ++ s""", ${additionalLine}""")
  }

  override def toString: String = {
    s"""Status ${self.status}: ${self.message}"""
  }
}

object WebserverHttpStatus {
  val STATUS_400 = WebserverHttpStatus(Status(Http.Status.BAD_REQUEST), "Request malformed")
  val STATUS_500 = WebserverHttpStatus(Status(Http.Status.INTERNAL_SERVER_ERROR), "Internal server error")
}
