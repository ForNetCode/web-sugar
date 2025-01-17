package very.util.web

import redis.clients.jedis.JedisPooled
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*

import scala.concurrent.duration.Duration
//import  io.circe.syntax.*
import io.circe.parser.*

trait RedisSession[Target: Encoder: Decoder](
  jedis: JedisPooled,
  expired: Duration
) {
  private val prefix = "p:"

  def session(token: String): Option[Target] =
    Option(jedis.get(s"$prefix$token")).map(v => decode[Target](v).toOption.get)

  def setSession(token: String, value: Target) = {
    val key = s"$prefix$token"
    jedis.set(key, JsonConfig.jsonPrinter.print(value.asJson))
    if(expired != Duration.Zero){
      jedis.expire(key, expired.toSeconds)
    }
  }
}
