package very.util.alert

trait Alert {
  def send(text: String): Boolean
}
