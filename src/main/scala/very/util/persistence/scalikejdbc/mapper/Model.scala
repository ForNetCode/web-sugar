package very.util.persistence.scalikejdbc.mapper

import java.sql.{JDBCType, Types}
import java.util.UUID

import scalikejdbc._
//TODO: https://blog.csdn.net/qq_31156277/article/details/85260325

case class Model(url: String, username: String, password: String, poolName: String = "default") extends AutoCloseable {

  locally {
    ConnectionPool.add(name = poolName, url = url, user = username, password = password)
  }

  private def columnName(implicit rs: WrappedResultSet): String = rs.string("COLUMN_NAME")

  private def columnDataType(implicit rs: WrappedResultSet): JDBCType = JDBCType.valueOf(rs.string("DATA_TYPE").toInt)

  private def isNotNull(implicit rs: WrappedResultSet): Boolean = {
    val isNullable = rs.string("IS_NULLABLE")
    isNullable == "NO" || isNullable == "N"
  }
  //rs.underlying.getInt("SQL_DATA_TYPE")
  Types.ARRAY

  private def isAutoIncrement(implicit rs: WrappedResultSet): Boolean =
    try {

      val isAutoIncrement = rs.string("IS_AUTOINCREMENT")
      isAutoIncrement == "YES" || isAutoIncrement == "Y"
    } catch { case e: Exception => false }

  private def listAllTables(schema: String, types: List[String]): collection.Seq[String] = {
    using(ConnectionPool.get(poolName).borrow()) { conn =>
      val meta = conn.getMetaData
      val (catalog, _schema) = {
        (schema, meta.getDatabaseProductName) match {
          case (null, _)           => (null, null)
          case (s, _) if s.isEmpty => (null, null)
          case (s, "MySQL")        => (s, null)
          case (s, _)              => (null, s)
        }
      }
      new ResultSetIterator(meta.getTables(catalog, _schema, "%", types.toArray)).map { rs =>
        rs.string("TABLE_NAME")
      }.toList
    }
  }

  def allTables(schema: String = null): collection.Seq[Table] =
    listAllTables(schema, List("TABLE")).map(table(schema, _)).flatten

  def allViews(schema: String = null): collection.Seq[Table] =
    listAllTables(schema, List("VIEW")).map(table(schema, _)).flatten

  def table(schema: String = null, tableName: String): Option[Table] = {
    val catalog = null
    val _schema = if (schema == null || schema.isEmpty) null else schema
    using(ConnectionPool.get(poolName).borrow()) { conn =>
      val meta = conn.getMetaData
      new ResultSetIterator(meta.getColumns(catalog, _schema, tableName, "%"))
        .map { implicit rs =>
          Column(columnName, columnDataType, isNotNull, isAutoIncrement)
        }
        .toList
        .distinct match {
        case Nil => None
        case allColumns =>
          Some(
            Table(
              schema = Option(schema),
              name = tableName,
              allColumns = allColumns,
              autoIncrementColumns = allColumns.filter(c => c.isAutoIncrement).distinct,
              primaryKeyColumns = {
                new ResultSetIterator(meta.getPrimaryKeys(catalog, _schema, tableName))
                  .flatMap { implicit rs =>
                    allColumns.find(column => column.name == columnName)
                  }
                  .toList
                  .distinct
              }
            )
          )
      }
    }
  }

  def getConnectionPool: ConnectionPool = ConnectionPool.get(poolName)
  
  def close(): Unit = {
    ConnectionPool.close(poolName)
  }
}
