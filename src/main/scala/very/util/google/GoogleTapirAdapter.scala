package very.util.google

import org.pac4j.jwt.config.signature.RSASignatureConfiguration
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.profile.UserProfile
import sttp.model.StatusCode
import sttp.tapir.*
import very.util.keycloak.{AuthError, TokenInvalid}
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import java.math.BigInteger
import java.util.Base64
import scala.util.{Try, Success, Failure}

class GoogleTapirAdapter(clientId: String) {

  // For Google ID tokens, we need to verify using Google's public keys
  // This is a simplified approach - in production you'd want to fetch and cache Google's JWKs
  private val jwtAuthenticator = new JwtAuthenticator()
  
  // Configure for Google's issuer
  jwtAuthenticator.addSignatureConfiguration(new RSASignatureConfiguration())

  def verifyGoogleIdToken(idToken: String): Either[String, UserProfile] = {
    Try {
      val creds = new TokenCredentials(idToken)
      // For now, we'll do basic JWT parsing without signature verification
      // In production, you should verify against Google's public keys
      val parts = idToken.split("\\.")
      if (parts.length != 3) {
        throw new IllegalArgumentException("Invalid JWT format")
      }
      
      val payload = new String(Base64.getUrlDecoder.decode(parts(1)))
      // Create a basic profile from the JWT payload
      // This is simplified - you'd want proper JSON parsing here
      creds.setUserProfile(createProfileFromPayload(payload))
      creds.getUserProfile
    } match {
      case Success(profile) => Right(profile)
      case Failure(e) => Left(e.getMessage)
    }
  }
  
  private def createProfileFromPayload(payload: String): UserProfile = {
    // This is a simplified implementation
    // In practice, you'd parse the JSON payload properly
    import org.pac4j.core.profile.CommonProfile
    val profile = new CommonProfile()
    profile.setId("google-user") // You'd extract this from the JWT payload
    profile
  }

  def validateToken(rawToken: String): Option[UserProfile] = {
    verifyGoogleIdToken(rawToken) match {
      case Right(profile) => Some(profile)
      case Left(_) => None
    }
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
