package very.util.alert

import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.client3.*
import very.util.config.WithConfig
import io.circe.*
import sttp.client3.circe.*

trait WithAlert extends WithConfig {
  import very.util.config.getOptional
  object alertBot
    extends FeiShuAlertBot(config.getString("alert.feishu.url"), config.getOptional[String]("alert.feishu.url.secret"))
}

//TODO: support send with secret
class FeiShuAlertBot(url: String, secret: Option[String] = None) extends Alert {

  private val client =
    Slf4jLoggingBackend(HttpClientSyncBackend())

  def send(text: String): Boolean = {
    basicRequest
      .post(uri"$url")
      .body(
        Json.obj(
          "msg_type" -> Json.fromString("text"),
          "content" -> Json.obj("text" -> Json.fromString(text)),
        )
      )
      .send(client)
      .is200
  }
}
