package very.util.web.auth

import org.mindrot.jbcrypt.BCrypt

object PasswordUtil {
  def secret(password: String): String = {
    BCrypt.hashpw(password, BCrypt.gensalt(12))
  }

  def check(candidate: String, hashed: String): Boolean = {
    BCrypt.checkpw(candidate, hashed)
  }

}
