package very.util.persistence.scalikejdbc.mapper

trait Generator {
  def modelAll(): String
  def writeModel(): Unit
  def writeModelIfNonexistentAndUnskippable(): Boolean
}