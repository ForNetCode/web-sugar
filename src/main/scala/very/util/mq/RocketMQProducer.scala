package very.util.mq

import com.typesafe.config.Config
import io.circe.{ Encoder, Json }
import org.apache.rocketmq.client.apis.message.MessageId
import org.apache.rocketmq.client.apis.{ ClientConfiguration, ClientServiceProvider, StaticSessionCredentialsProvider }
import very.util.web.{ JsonConfig, LogSupport }

import java.nio.charset.StandardCharsets
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success, Try }

case class RocketMQConfig(accessKey: String, secretKey: String, endpoint: String, topics: List[String] = List.empty)

object RocketMQConfig {
  import io.circe.config.syntax.*
  import io.circe.generic.auto.*
  def loadFromConfig(config: Config): RocketMQConfig = config.as[RocketMQConfig].toTry.get
}

class RocketMQProducer(config: RocketMQConfig) extends LogSupport {
  private val provider = ClientServiceProvider.loadService()
  private val clientConfiguration = ClientConfiguration
    .newBuilder()
    .setEndpoints(config.endpoint)
    .setCredentialProvider(StaticSessionCredentialsProvider(config.accessKey, config.secretKey))
    .build()
  private val producer =
    provider.newProducerBuilder().setClientConfiguration(clientConfiguration).setTopics(config.topics*).build()

  def sendMessage(
    topic: String,
    body: Json,
    tag: Option[String] = None,
    delayTime: Option[Duration] = None
  ): Try[MessageId] = {
    val message = provider
      .newMessageBuilder()
      .setTopic(topic)
      .setBody(JsonConfig.jsonPrinter.printToByteBuffer(body, StandardCharsets.UTF_8).array())
    for (t <- tag) {
      message.setTag(t)
    }
    for (time <- delayTime) {
      message.setDeliveryTimestamp(time.toMillis + System.currentTimeMillis())
    }

    val result = Try {
      val sendReceipt = producer.send(message.build())
      sendReceipt.getMessageId
    }
    result match {
      case Success(msgId) =>
        logger.debug(
          s"send message,topic:$topic, tag:$tag, body:${JsonConfig.jsonPrinter.print(body)} successful, msgId: ${msgId.toString}"
        )
      case Failure(exception) =>
        logger.warn(
          s"send message, topic:$topic, tag:$tag, body:${JsonConfig.jsonPrinter.print(body)} failure",
          exception
        )
    }
    result
  }
}

object RocketMQProducer {
  extension [T](body: T)(using encode: Encoder[T]) {
    def toMessage = encode(body)
  }
}
