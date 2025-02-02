package very.util.keycloak

import com.typesafe.config.Config
import org.quartz.SimpleScheduleBuilder
import sttp.client3.*
import sttp.client3.circe.*
import io.circe.generic.auto.*
import very.util.task.executor.singleTaskExecutor
import very.util.task.{ QuartzManager, UnitTask }
import very.util.web.LogSupport

case class KeycloakConfig(
  url: String,
  // realm:String =
  user: String,
  password: String,
)

object KeycloakConfig {
  def loadFromConfig(config: Config) = {
    import io.circe.config.syntax.*
    import io.circe.generic.auto.*

    config.as[KeycloakConfig].toTry.get
  }
}
// https://www.keycloak.org/docs-api/26.1.0/rest-api/#_clients
case class TokenResponse(access_token: String, refresh_token: String, expires_in: Int)

case class UserRepresentation(
  id: String,
  username: String,
  firstName: String,
  lastName: String,
  realmRoles: List[String],
)

class KeycloakAdminAPI(config: KeycloakConfig, httpClient: SttpBackend[Identity, Any]) extends LogSupport {

  private var _refreshToken: Option[String] = None
  private var _token: String = null
  /*
  {
   "access_token" : ".....",
   "refresh_token" : ".....",
   "expires_in" : "...."
 }
   */
  def getTokenByPassword() = {
    val result = basicRequest
      .post(uri"${config.url}/realms/master/protocol/openid-connect/token")
      // .contentType("application/x-www-form-urlencoded")
      .body(
        Map(
          "grant_type" -> "password",
          "username" -> config.user,
          "password" -> config.password,
          "client_id" -> "admin-cli",
          "scope" -> "openid",
        )
      )
      .response(asJsonAlways[TokenResponse])
      .send(httpClient)
      .body

    result
  }

  def getTokenByRefreshToken(refreshToken: String) = {
    basicRequest
      .post(uri"${config.url}/realms/master/protocol/openid-connect/token")
      .response(asJsonAlways[TokenResponse])
      .body(
        Map(
          "grant_type" -> "refresh_token",
          "refresh_token" -> refreshToken,
          "client_id" -> "admin-cli",
          "scope" -> "openid",
        )
      )
      .send(httpClient)
  }
  // schedule to refresh token
  // https://stackoverflow.com/questions/51386337/refresh-access-token-via-refresh-token-in-keycloak
  def refreshToken() = {
    _refreshToken match {
      case Some(refreshToken) => getTokenByRefreshToken(refreshToken)
      case None               => getTokenByPassword()
    } match {
      case Left(value) =>
        logger.error(s"refresh token failed: $value")
        None
      case Right(value) =>
        _token = value.access_token
        _refreshToken = Some(value.refresh_token)
        logger.debug(s"refresh token: ${value}")
        Some(value)
    }
  }

  // GET /admin/realms/{realm}/users/{user-id}
  def getUserInfo(realm: String, userId: String) = {
    val result = basicRequest
      .get(uri"${config.url}/admin/realms/$realm/users/$userId")
      .header("Authorization", s"Bearer ${_token}")
      .response(asJsonAlways[UserRepresentation])
      .send(httpClient)
      .body
  }

}

class KeycloakTokenRefreshTask(adminAPI: KeycloakAdminAPI) extends UnitTask with LogSupport {

  override def name: String = "KeycloakTokenRefresh"

  override def run(param: Unit)(using context: Unit): Unit = {
    adminAPI.refreshToken()
  }
}
object KeycloakTokenRefreshTask {
  def scheduler(adminAPI: KeycloakAdminAPI)(using quartzManager: QuartzManager): Unit = {
    val task = KeycloakTokenRefreshTask(adminAPI)
    quartzManager.schedulerTask(
      singleTaskExecutor(task)(using {}),
      task.name,
      () => {},
      SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(50).repeatForever()
    )

  }
}
