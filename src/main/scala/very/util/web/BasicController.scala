package very.util.web

import io.circe.Printer
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.TapirJsonCirce
import very.util.entity.{ Page, Pagination2 }

sealed trait ErrorInfo
case class NotFound(msg: String = "Not Exists") extends ErrorInfo
case class BadRequest(msg: String) extends ErrorInfo
case class Unauthorized(msg: String = "Invalid Authorization") extends ErrorInfo
case class InternalServerError(msg: String) extends ErrorInfo

//case class ErrorInfoCustomize(code: StatusCode, msg: String) extends ErrorInfo
//case object NoContent extends ErrorInfo

trait BasicController extends LogSupport with TapirJsonCirce {

  protected val route = endpoint.errorOut(
    oneOf[ErrorInfo](
      oneOfVariant(statusCode(StatusCode.NotFound).and(stringBody.mapTo[NotFound])),
      oneOfVariant(statusCode(StatusCode.BadRequest).and(stringBody.mapTo[BadRequest])),
      oneOfVariant(statusCode(StatusCode.Unauthorized).and(stringBody.mapTo[Unauthorized])),
      oneOfVariant(statusCode(StatusCode.InternalServerError).and(stringBody.mapTo[InternalServerError]))
    )
  )

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
