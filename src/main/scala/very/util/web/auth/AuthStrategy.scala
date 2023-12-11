package very.util.web.auth

trait AuthStrategy[User] {
  def name:String
  def adminAuth(token:String): Option[User]
  def clientAuth(token:String): Option[User]
}
