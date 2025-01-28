package very.util.persistence

import enumeratum.values.ShortEnumEntry
import scalasql.*
import scalasql.core.SqlStr.SqlStringSyntax
import scalasql.PostgresDialect.*

// import java.sql.{ JDBCType, PreparedStatement, ResultSet }

object ScalaSQLExtra {

  def nextVal(index: String) = sql"select nextval($index)"

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
