package very.util.security

import org.hashids.Hashids

import scala.util.Random

trait ID[T] {
  def id: T
  def secretId: String
}

case class IntID(id: Int, secretId: String) extends ID[Int]

object IntID {
  def apply(id: Int)(using hashId: Hashids): IntID = IntID(id, hashId.encode(id))
  def apply(secretId: String)(using hashId: Hashids): IntID = IntID(hashId.decode(secretId).head.toInt, secretId)

  // given Conversion[IntID, Int] = _.id
}

object ID {
  extension (secretId: String)(using hashId: Hashids) {
    def toIntID: IntID = IntID(secretId)
    def toTokenId: TokenID = TokenID(secretId)
  }
}

case class LongID(id: Long, secretId: String) extends ID[Long]

// This protect hashId from brute force attack
// secretId: $hashId_$token
case class TokenID(intId: IntID, token: String) extends ID[Int] {
  override def secretId: String = s"${intId.secretId}_$token"
  override def id: Int = intId.id
}

object TokenID {

  def randomToken(length: Int = 5): String = Random.alphanumeric.take(length).mkString

  def apply(secretId: String)(using hashId: Hashids): TokenID = {
    val Array(_secretId, token) = secretId.split('_')
    TokenID(IntID(_secretId), token)
  }

  def apply(id: Int, token: String = randomToken())(using hashId: Hashids): TokenID = TokenID(IntID(id), token)
}
