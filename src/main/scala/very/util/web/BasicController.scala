package very.util.web

import io.circe.Printer
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.TapirJsonCirce
import very.util.entity.{ Page, Pagination2 }
import sttp.tapir.server.model.EndpointExtensions.*

//sealed trait ErrorInfo
//case class NotFound(msg: String = "Not Exists") extends ErrorInfo
//case class BadRequest(msg: String) extends ErrorInfo
//case class Unauthorized(msg: String = "Invalid Authorization") extends ErrorInfo
//case class InternalServerError(msg: String) extends ErrorInfo

//case class ErrorInfoCustomize(code: StatusCode, msg: String) extends ErrorInfo
//case object NoContent extends ErrorInfo

trait BasicController extends LogSupport with TapirJsonCirce {

//  protected def route = endpoint
//    .errorOut(
//      oneOf[ErrorInfo](
//        oneOfVariant(statusCode(StatusCode.NotFound).and(stringBody.mapTo[NotFound])),
//        oneOfVariant(statusCode(StatusCode.BadRequest).and(stringBody.mapTo[BadRequest])),
//        oneOfVariant(statusCode(StatusCode.Unauthorized).and(stringBody.mapTo[Unauthorized])),
//        oneOfVariant(statusCode(StatusCode.InternalServerError).and(stringBody.mapTo[InternalServerError]))
//      )
//    )
//    .maxRequestBodyLength(1024 * 1 * 1024 /*1M*/ )

  protected val paging: EndpointInput[Page] =
    query[Option[Int]]("page")
      .description("页数")
      .default(Some(1))
      .validateOption(Validator.min(1))
      .and(
        query[Option[Int]]("limit")
          .description("页数量")
          .default(Some(20))
          .validateOption(Validator.min(5) and Validator.max(50))
      )
      .map((page, limit) => Pagination2(page.getOrElse(1), limit.getOrElse(20)))(v => (Some(v.page), Some(v.pageSize)))

  override val jsonPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)
}
