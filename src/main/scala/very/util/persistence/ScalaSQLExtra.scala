package very.util.persistence

import scalasql.*
import scalasql.core.SqlStr.SqlStringSyntax
import scalasql.PostgresDialect.*
import scalasql.query.Select
import very.util.entity.Page

// import java.sql.{ JDBCType, PreparedStatement, ResultSet }

object ScalaSQLExtra {

  def nextVal(index: String) = sql"select nextval($index)"

  // def page(param: Expr[Page]) = Expr(implicit ctx => sql"limit {$param.limit} offset {$param.offset}")

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
  /*
  given shortEnumTypeMapper[T<:ShortEnumEntry]: TypeMapper[T] =
    new TypeMapper[T] {
      def jdbcType: JDBCType = JDBCType.TINYINT

      def get(r: ResultSet, idx: Int): T = {
        ???.withValue(r.getShort(idx))
      }

      def put(r: PreparedStatement, idx: Int, v: T): Unit =
        r.setShort(idx, v.value)
    }
   */

}
