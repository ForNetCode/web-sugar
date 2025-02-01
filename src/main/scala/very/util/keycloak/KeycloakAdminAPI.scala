package very.util.keycloak

import com.typesafe.config.Config
import sttp.client3.{ Identity, SttpBackend }

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
class KeycloakAdminAPI(config: KeycloakConfig, httpClient: SttpBackend[Identity, Any]) {

  private var _refreshToken: Option[String] = None
  /*
  {
   "access_token" : ".....",
   "refresh_token" : ".....",
   "expires_in" : "...."
 }
   */
  def getToken() = {}
  // schedule to refresh token
  // https://stackoverflow.com/questions/51386337/refresh-access-token-via-refresh-token-in-keycloak
  def refreshToken() = {}

  // POST /admin/realms/{realm}/users/{user-id}
  def getUserInfo(realm: String, userId: String) = {}

}
