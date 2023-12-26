package very.util.web

import io.circe.Printer
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.TapirJsonCirce
import very.util.entity.{ Page, Pagination2 }
import sttp.tapir.server.model.EndpointExtensions.*

sealed trait ErrorInfo
case class NotFound(msg: String = "Not Exists") extends ErrorInfo
case class BadRequest(msg: String) extends ErrorInfo
case class Unauthorized(msg: String = "Invalid Authorization") extends ErrorInfo
case class InternalServerError(msg: String) extends ErrorInfo

//case class ErrorInfoCustomize(code: StatusCode, msg: String) extends ErrorInfo
//case object NoContent extends ErrorInfo

trait BasicController extends LogSupport with TapirJsonCirce {

  protected def route = endpoint
    .errorOut(
      oneOf[ErrorInfo](
        oneOfVariant(statusCode(StatusCode.NotFound).description("Not Found").and(stringBody.mapTo[NotFound])),
        oneOfVariant(statusCode(StatusCode.BadRequest).description("Bad Parameters").and(stringBody.mapTo[BadRequest])),
        oneOfVariant(statusCode(StatusCode.Unauthorized).description("Invalid Authorization").and(stringBody.mapTo[Unauthorized])),
        oneOfVariant(statusCode(StatusCode.InternalServerError).description("Server Error").and(stringBody.mapTo[InternalServerError]))
      )
    )
    .maxRequestBodyLength(1024 * 8 * 2/*2M*/)

  protected val paging: EndpointInput[Page] =
    query[Int]("page")
      .description("页数")
      .default(1)
      .validate(Validator.min(1))
      .and(
        query[Int]("limit")
          .description("页数量")
          .default(20)
          .validate(Validator.min(5) and Validator.max(50))
      )
      .map((page, limit) => Pagination2(page, limit))(v => (v.page, v.pageSize))

  override val jsonPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)
}
