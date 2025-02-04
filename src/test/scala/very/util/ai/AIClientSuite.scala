package very.util.ai

import com.typesafe.config.ConfigFactory
import munit.FunSuite

import sttp.client4.*
import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.openai.OpenAISyncClient
import sttp.openai.requests.completions.chat.ChatRequestBody.*
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatCompletionModel.CustomChatCompletionModel
import sttp.openai.requests.completions.chat.message.*
import sttp.openai.requests.completions.chat.message.Message.UserMessage

class AIClientSuite extends FunSuite {
  lazy val config = AIClientConfig.loadFromConfig(ConfigFactory.load().getConfig("ai"))
  test("simple") {
    val options = BackendOptions.Default
    // val options = BackendOptions.httpProxy("127.0.0.1", 7890)
    val backend = HttpClientSyncBackend(options = options)

    val client = OpenAISyncClient(config.key, backend, uri"${config.url}")
    // val client = OpenAISyncClient(config.key, backend, uri"${config.url}")
    val body = ChatBody(
      model = CustomChatCompletionModel(config.model),
      messages = List(
        UserMessage(content = Content.TextContent("Hello"))
      )
    )
    val chatResponse = client.createChatCompletion(body)
    println(chatResponse)
  }
}
