package very.util.persistence

import scalasql.DbClient
import scalasql.core.DbApi

class DBHelper(dbClient: DbClient) extends DbApi {
  private val dbApi = dbClient.getAutoCommitClientConnection

  export dbApi.*

  export dbClient.transaction
}
