package services

import cats.effect.IO
import models.other.WebserverHttpStatus
import cats.implicits._

object DBUtil {
  def unifyErrors[A](objList: List[Either[WebserverHttpStatus, A]], message: String = "Error while processing DB request: "): IO[Either[WebserverHttpStatus, A]] = {
    val default: Either[WebserverHttpStatus, A] =
      Either.left(WebserverHttpStatus.STATUS_500.addLineToMessage(message))

    IO.pure {
      objList.foldLeft(default) {
        case (err, model) =>
          if (model.isLeft) {
            for {
              a <- model.left
              b <- err.left
            } yield {
              b.addLineToMessage("\n" + a.toString)
            }
          } else err
      }
    }
  }
}
