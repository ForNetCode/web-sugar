package very.util.ai

import com.typesafe.config.ConfigFactory
import munit.FunSuite
import sttp.client4.*
import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.openai.OpenAISyncClient
import sttp.openai.requests.completions.chat.ChatRequestBody.*
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatCompletionModel.CustomChatCompletionModel
import sttp.openai.requests.completions.chat.message.*
import sttp.openai.requests.completions.chat.message.Message.{ ToolMessage, UserMessage }
import sttp.openai.requests.completions.chat.message.Tool.FunctionTool

class AIClientSuite extends FunSuite {
  lazy val config = AIClientConfig.loadFromConfig(ConfigFactory.load().getConfig("ai"))

  def getClient = {
    val options = BackendOptions.Default
    // val options = BackendOptions.httpProxy("127.0.0.1", 7890)
    val backend = HttpClientSyncBackend(options = options)

    OpenAISyncClient(config.key, backend, uri"${config.url}")
  }
  test("simple") {
    val client = getClient
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
  test("call function") {
    val client = getClient
    val request = ChatBody(
      model = CustomChatCompletionModel(config.model),
      tools = Some(
        Seq(
          FunctionTool(
            name = "get_weather",
            description = "Get weather of an location, the user should supply a location first",
            parameters = Map(
              "type" -> "object",
              "properties" -> Map(
                "location" -> Map(
                  "type" -> "string",
                  "description" -> "The location to get weather"
                )
              ),
              "required" -> List("location")
            )
          )
        )
      ),
      messages = List(
        UserMessage(content = Content.TextContent("What is the weather in Beijing?"))
      )
    )

    val result = client.createChatCompletion(request)
    println(result)
    result.choices.collectFirst {
      case v if v.message.functionCall.isDefined => v.message.functionCall.get
    }.foreach {func =>
      func.name.get
      import io.circe.*, io.circe.parser.*
      parse(func.arguments).toTry.get
    }
  }
}
