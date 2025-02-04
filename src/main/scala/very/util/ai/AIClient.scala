package very.util.ai

import com.typesafe.config.Config
import io.circe.derivation.{ Configuration, ConfiguredCodec }

given Configuration = Configuration.default.withDefaults

case class AIClientConfig(
  key: String,
  url: String = "https://api.groq.com/openai/v1",
  model: String = "deepseek-r1-distill-llama-70b",
  
) derives ConfiguredCodec

object AIClientConfig {
  def loadFromConfig(config: Config): AIClientConfig = {
    import io.circe.config.syntax.*

    config.as[AIClientConfig].toTry.get
  }
}
class AIClient
