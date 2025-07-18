package very.util.keycloak

import io.circe.Json
import org.pac4j.core.profile.UserProfile
import org.pac4j.jwt.config.signature.RSASignatureConfiguration
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import sttp.client4.*
import sttp.client4.logging.{ DefaultLog, LogLevel }
import sttp.client4.logging.slf4j.Slf4jLoggingBackend
import sttp.model.StatusCode
import very.util.task.retry.EasyRetry
import very.util.web.JsonConfig
import sttp.tapir.*

import scala.jdk.CollectionConverters.*

trait ErrorResp
sealed trait AuthError extends ErrorResp
case class TokenInvalid(msg: String = "Token Invalid") extends AuthError
case class PermissionInvalid(msg: String = "Permission Invalid")
  extends AuthError

/*
//TODO:
1. JWKS 变更后该如何 check
2. token 依据新 kid 生成，如何判定并获取新 JWKS
3. JWKS 获取失败。 目前重试3次解决
4. token cache、黑名单
5. 添加更多的token 校验规则，例如 Email Verified
 */
class TapirOIDCAdapter(
  jwksUrl: String =
    "http://127.0.0.1:8180/realms/test/protocol/openid-connect/certs"
) {

  private val httpClient = {
    import sttp.client4.logging.LogConfig
    Slf4jLoggingBackend(
      DefaultSyncBackend(),
      LogConfig(
        logRequestBody = true,
        logResponseBody = true,
        logRequestHeaders = false,
        logResponseHeaders = false,
        responseLogLevel = { status =>
          if (status.code == 200) LogLevel.Debug
          else LogLevel.Warn
        }
      )
    )
  }

  private def getJWKS
    : Either[io.circe.Error, List[RSASignatureConfiguration]] = {
    val data =
      basicRequest.get(uri"$jwksUrl").response(asStringAlways).send(httpClient)
    import io.circe.parser.*
    parse(data.body).flatMap(_.hcursor.get[List[Json]]("keys")).map { list =>
      list.map { v =>
        val jwk = JsonConfig.jsonPrinter.print(v)
        val config = RSASignatureConfiguration()
        config.setKeysFromJwk(jwk)
        config
      }
    }
  }

  private val jwtAuth: JwtAuthenticator = JwtAuthenticator(
    EasyRetry.retryEither((_: String) => getJWKS)("").toOption.get.asJava
  )

  def validateToken(rawToken: String): Option[UserProfile] = {
    Option(jwtAuth.validateToken(rawToken))
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

  def hasRole[F[_]](roles: String*) = {
    endpoint
      .errorOut(
        oneOf[ErrorResp](
          oneOfVariant(
            statusCode(StatusCode.Forbidden)
              .and(stringBody.mapTo[PermissionInvalid])
          ),
          oneOfVariant(
            statusCode(StatusCode.Unauthorized)
              .and(stringBody.mapTo[TokenInvalid])
          )
        )
      )
      .securityIn(auth.bearer[String]())
      .serverSecurityLogicPure[String, F] { token =>
        validateToken(token) match {
          case Some(userProfile)
            if userProfile.getRoles.containsAll(roles.asJava) =>
            Right(userProfile.getId)
          case Some(_) =>
            Left(PermissionInvalid())
          case _ =>
            Left(TokenInvalid())
        }
      }
  }

}
