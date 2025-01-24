package very.util.task.retry

import com.typesafe.scalalogging.LazyLogging
import very.util.web.LogSupport

import scala.annotation.tailrec

object EasyRetry extends LogSupport {

  def retryEither[P, R, E](
    sync: P => Either[E, R],
    time: Int = 3
  ): P => Either[E, R] = {
    retryTime(sync, _.isLeft, time)
  }

  // when return true, need retry
  def retryTime[T, R](
    sync: T => R,
    needRetry: R => Boolean,
    time: Int = 3
  ): T => R = {
    var _retryTime = time
    retry(
      p => {
        val r = sync(p)
        _retryTime -= 1
        r
      },
      r => _retryTime > 0 && needRetry(r)
    )
  }

  // should not use this, this may cause always retry
  def retry[T, R](sync: T => R, needRetry: R => Boolean): T => R = {
    @tailrec
    def _retry(sync: T => R, param: T): R = {
      val r = sync(param)
      if (needRetry(r)) {
        _retry(sync, param)
      } else {
        r
      }
    }
    (p: T) => _retry(sync, p)
  }
  // when return false, need retry
  def retryBoolean[T](sync: T => Boolean, time: Int = 3): T => Boolean = {
    retryTime(
      sync,
      x => !x,
      time
    )

  }

  // when Option is Some, then retry
  def retryOption[T](sync: T => Option[T], time: Int = 3): T => Option[T] = {
    retryTime(sync, _.isDefined, time)
  }

}
