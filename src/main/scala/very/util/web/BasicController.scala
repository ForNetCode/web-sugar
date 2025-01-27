package very.util.web

import io.circe.Printer
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.TapirJsonCirce
import very.util.entity.{Offset, Page}
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
    query[Int]("_start")
      .description("start")
      .default(10)
      .and(
        query[Int]("_end")
          .description("end")
          .default(1)
      ).validate(Validator.custom((start,end) =>
        ValidationResult.validWhen(end-start <=100 && end > start && start>=0), Some("end and start should > 0 and end - start <=10")))
      .map((start, end) =>
        Offset(start, end): Page
      )(v => (v.offset, v.limit + v.offset))

  override val jsonPrinter: Printer = JsonConfig.jsonPrinter
}
