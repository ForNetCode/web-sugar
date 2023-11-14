package very.util.alert

import very.util.web.LogSupport

class LogAlert extends Alert with LogSupport {
  override def send(text: String): Boolean = {
    logger.warn(text)
    true
  }
}
