package very.util.persistence

import javax.sql.DataSource
import java.sql.Connection

object TenantContext {
  private val currentTenant = new InheritableThreadLocal[Int]()

  def setTenantId(tenantId: Int): Unit = {
    currentTenant.set(tenantId)
  }

  def getTenantId(): Option[Int] = {
    Option(currentTenant.get())
  }

  def remove(): Unit = {
    currentTenant.remove()
  }
}

class TenantDataSource(dataSource: DataSource) extends DataSource {

  export dataSource.{ getConnection as _, close as _, * }
  override def getConnection(username: String, password: String): java.sql.Connection = {
    dataSource.getConnection(username, password)
  }
  override def getConnection(): Connection = {
    val conn = dataSource.getConnection()
    setTenantId(conn)
    conn
  }

  private def setTenantId(connection: Connection): Unit = {
    val tenantId: Option[Int] = TenantContext.getTenantId()
    tenantId match {
      case None =>
        throw new IllegalStateException("Tenant id is not set")
      case Some(id) =>
        val sql = connection.createStatement
        sql.execute("SET app.tenant_id TO " + tenantId)
    }
  }

}
