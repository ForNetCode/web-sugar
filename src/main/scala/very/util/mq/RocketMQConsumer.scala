package very.util.mq

import com.typesafe.config.Config
import io.circe.derivation.{ Configuration, ConfiguredCodec }
import org.apache.rocketmq.client.apis.consumer.FilterExpression
import org.apache.rocketmq.client.apis.message.MessageView
import org.apache.rocketmq.client.apis.{ ClientConfiguration, ClientServiceProvider, StaticSessionCredentialsProvider }
import very.util.web.LogSupport

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.*
import scala.jdk.FutureConverters.*

// https://github.com/apache/rocketmq-clients/blob/master/java/client/src/main/java/org/apache/rocketmq/client/java/example/AsyncSimpleConsumerExample.java

given Configuration = Configuration.default.withDefaults

case class RocketMQConsumerConfig(
  accessKey: String,
  secretKey: String,
  endpoint: String,
  maxMessageNum: Int = 16,
  invisibleSeconds: Int = 20
) derives ConfiguredCodec

object RocketMQConsumerConfig {

  import io.circe.config.syntax.*
  import io.circe.generic.auto.*

  def loadFromConfig(config: Config): RocketMQConsumerConfig = config.as[RocketMQConsumerConfig].toTry.get

}
class RocketMQConsumer(config: RocketMQConsumerConfig) extends LogSupport {
  val provider = ClientServiceProvider.loadService()

  import scala.concurrent.ExecutionContext.Implicits.global

  private val _conf = ClientConfiguration
    .newBuilder()
    .setEndpoints(config.endpoint)
    .setCredentialProvider(
      StaticSessionCredentialsProvider(config.accessKey, config.secretKey)
    )
    .build()

  def listen(
    consumerGroup: String,
    awaitDuration: Duration,
    options: Map[String, (FilterExpression, MessageView => Unit)]
  )(
  ) = {
    val consumer = provider
      .newSimpleConsumerBuilder()
      .setClientConfiguration(_conf)
      .setAwaitDuration(java.time.Duration.ofSeconds(awaitDuration.toSeconds))
      .setConsumerGroup(consumerGroup)
      .setSubscriptionExpressions(options.view.mapValues(_._1).toMap.asJava)
      .build()

    val future =
      consumer
        .receiveAsync(config.maxMessageNum, java.time.Duration.ofSeconds(config.invisibleSeconds))
        .asScala

    future.foreach { _msgList =>
      val msgList = _msgList.asScala
      for (msg <- msgList) {
        try {
          options(msg.getTopic)._2(msg)
          consumer.ackAsync(msg).asScala.foreach(identity)
        } catch {
          case e: Throwable =>
            logger.error(s"handle message: ${msg.getTopic} ${msg.getMessageId} failed", e)
        }
      }
    }
  }

}
