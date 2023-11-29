package very.util.web

import io.circe.Printer
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.TapirJsonCirce

sealed trait ErrorInfo
case class NotFound(msg: String) extends ErrorInfo
case class BadRequest(msg: String) extends ErrorInfo
case class Unauthorized(msg: String) extends ErrorInfo
//case class ErrorInfoCustomize(code: StatusCode, msg: String) extends ErrorInfo
//case object NoContent extends ErrorInfo

trait Controller extends LogSupport with TapirJsonCirce {

  protected val route = endpoint.errorOut(
    oneOf[ErrorInfo](
      oneOfVariant(statusCode(StatusCode.NotFound).and(stringBody.mapTo[NotFound])),
      oneOfVariant(statusCode(StatusCode.BadRequest).and(stringBody.mapTo[BadRequest])),
      oneOfVariant(statusCode(StatusCode.Unauthorized).and(stringBody.mapTo[Unauthorized])),
    )
  )

  override val jsonPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)
}
