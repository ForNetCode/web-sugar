package very.util.persistence

import enumeratum.values.*
import io.circe.{ Decoder, Encoder }
import org.postgresql.util.PGobject
import scalasql.*
import scalasql.PostgresDialect.*
import scalasql.core.SqlStr.SqlStringSyntax
import scalasql.query.Select
import very.util.entity.Page
import very.util.web.JsonConfig

import java.sql.{ JDBCType, PreparedStatement, ResultSet }

object ScalaSQLExtra {

  def nextVal(index: String) = sql"select nextval($index)"

  extension [Q, R](v: Select[Q, R]) {
    def page(param: Page) = v.take(param.limit).drop(param.offset)

    def search(params: (Q => Expr[Boolean] | Option[Expr[Boolean]])*) = v.filter { entity =>
      params.foldLeft(Expr(true)) { case (sum, func) =>
        func(entity) match {
          case r: Expr[Boolean] => sum && r
          case None             => sum
          case Some(r)          => sum && r
        }
      }
    }
  }

  // Next is for Enum TypeMapper

  def shortEnumTypeMapper[T <: ShortEnumEntry](using obj:ShortEnum[T]): TypeMapper[T] = new TypeMapper[T] {
    def jdbcType: JDBCType = JDBCType.TINYINT

    def get(r: ResultSet, idx: Int): T = {
      obj.withValue(r.getShort(idx))
    }

    def put(r: PreparedStatement, idx: Int, v: T): Unit =
      r.setShort(idx, v.value)
  }

  def intEnumTypeMapper[T <: IntEnumEntry](using obj: IntEnum[T]): TypeMapper[T] = new TypeMapper[T] {
    def jdbcType: JDBCType = JDBCType.INTEGER

    def get(r: ResultSet, idx: Int): T = {
      obj.withValue(r.getInt(idx))
    }

    def put(r: PreparedStatement, idx: Int, v: T): Unit =
      r.setInt(idx, v.value)
  }

  def stringEnumTypeMapper[T <: StringEnumEntry](using obj: StringEnum[T]): TypeMapper[T] = new TypeMapper[T] {
    def jdbcType: JDBCType = JDBCType.OTHER

    def get(r: ResultSet, idx: Int): T = {
      obj.withValue(r.getString(idx))
    }

    def put(r: PreparedStatement, idx: Int, v: T): Unit =
      r.setString(idx, v.value)
  }

  // jsonbTypeMapper
  def jsonbTypeMapper[T: {Decoder, Encoder}]: TypeMapper[T] = new TypeMapper[T] {
    def jdbcType: JDBCType = JDBCType.OTHER

    def get(r: ResultSet, idx: Int): T = {
      val json = r.getString(idx)
      io.circe.parser.decode[T](json).toTry.get
    }

    override def put(r: PreparedStatement, idx: Int, v: T): Unit = {
      import io.circe.syntax.*
      val jsonObject = PGobject()
      jsonObject.setType("jsonb")
      jsonObject.setValue(JsonConfig.jsonPrinter.print(v.asJson))
      r.setObject(idx, jsonObject)
    }
  }
}
