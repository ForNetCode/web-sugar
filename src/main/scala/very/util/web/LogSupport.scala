package very.util.web

import com.typesafe.scalalogging.Logger

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{ Failure, Try }
trait LogSupport {

  @transient
  protected lazy val logger: Logger =
    com.typesafe.scalalogging.Logger(getClass.getName.stripSuffix("$"))

  protected inline def logTry[T](inline errorMessage: String)(inline func: T): Try[T] = {
    val result = Try(func)
    result match {
      case Failure(exception) =>
        logger.warn(errorMessage, exception)
      case _ =>
    }
    result
  }

  protected inline def logFuture[T](inline errorMessage: String)(inline func: T)(using ExecutionContext): Future[T] = {
    val result = Future(func)
    result.onComplete(_ match {
      case Failure(exception) =>
        logger.warn(errorMessage, exception)
      case _ =>
    })
    result

  }
}
