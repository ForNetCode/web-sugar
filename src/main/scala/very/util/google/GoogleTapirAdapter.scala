package very.util.google

import org.pac4j.core.profile.UserProfile
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration
import sttp.model.StatusCode
import sttp.tapir.*
import very.util.keycloak.{AuthError, TokenInvalid}

class GoogleTapirAdapter(clientId: String, clientSecret: String) {

  private val oidcConfiguration = {
    val conf = new OidcConfiguration()
    conf.setClientId(clientId)
    conf.setSecret(clientSecret)
    conf.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration")
    conf
  }

  private val oidcClient = new OidcClient(oidcConfiguration)

  def validateToken(rawToken: String): Option[UserProfile] = {
    import org.pac4j.core.context.session.SessionStore
    import org.pac4j.core.context.WebContext
    import org.pac4j.oidc.credentials.OidcCredentials
    import java.util.Optional

    val credentials = new OidcCredentials()
    credentials.setAccessToken(null)
    credentials.setIdTokenString(rawToken)

    val context: WebContext = new org.pac4j.core.context.MockWebContext()
    val sessionStore: SessionStore = new org.pac4j.core.context.session.MockSessionStore()

    oidcClient.getAuthenticator
      .validate(credentials, context, sessionStore)
      .toOption
  }

  def tokenExtract[F[_]] = endpoint
    .errorOut(
      statusCode(StatusCode.Unauthorized)
        .and(stringBody.mapTo[TokenInvalid])
    )
    .securityIn(auth.bearer[String]())
    .serverSecurityLogicPure[UserProfile, F] { token =>
      validateToken(token) match {
        case Some(userProfile) =>
          Right(userProfile)
        case _ =>
          Left(TokenInvalid())
      }
    }
}
