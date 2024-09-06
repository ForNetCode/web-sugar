package very.util.persistence.scalikejdbc

import com.typesafe.config.ConfigFactory
import munit.FunSuite
import scalikejdbc.config.DBs
import scalikejdbc.*
import very.util.persistence.scalikejdbc.mapper.{CodeGenerator, DateTimeClass, GeneratorConfig, Model}

class CodeGeneratorSuite extends FunSuite {


  test("simple") {
    DBs.setupAll()

    val config = ConfigFactory.load()
    val url = config.getString("db.default.url")
    val username = config.getString("db.default.user")
    val password = config.getString("db.default.password")
    val driver = config.getString("db.default.driver")

    val model = Model(url, username, password, "default")



    given session: DBSession = AutoSession
    sql"""create table if not exists users(
         |id INTEGER PRIMARY KEY,
         |username TEXT NOT NULL,
         |info TEXT
         |)""".stripMargin.execute()

    val generatorConfig = GeneratorConfig(
      packageName = "com.timzaak.entity",
      dateTimeClass = DateTimeClass.OffsetDateTime,
      daoExtendImport = Some("very.util.persistence.scalikejdbc.Dao")
    )
    // where table to generate
    val tables = Map(
      //      "consignor"-> "Consignor",
      //      "fee_config" -> "FeeConfig",
    )
    model.allTables().foreach { table =>
      new CodeGenerator(table, None)(generatorConfig).writeModel()
    }
  }
}
