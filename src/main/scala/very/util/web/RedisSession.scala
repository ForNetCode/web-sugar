package very.util.web

import redis.clients.jedis.JedisPooled
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*

import scala.concurrent.duration.Duration
//import  io.circe.syntax.*
import io.circe.parser.*

trait RedisSession[Target: {Encoder, Decoder}](
  jedis: JedisPooled,
  prefix: String = "p:",
  defaultExpired: Duration = Duration.Zero
) {

  def session(token: String): Option[Target] =
    Option(jedis.get(s"$prefix$token")).map(v => decode[Target](v).toOption.get)

  def setSession(token: String, value: Target): Unit = {
    setSession(token, value, None)
  }

  def setSession(
    token: String,
    value: Target,
    expired: Option[Duration],
  ): Unit = {
    val key = s"$prefix$token"
    jedis.set(key, JsonConfig.jsonPrinter.print(value.asJson))
    val _expired = expired.getOrElse(defaultExpired)
    if (_expired != Duration.Zero) {
      jedis.expire(key, _expired.toSeconds)
    }
  }
}
