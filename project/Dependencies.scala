import sbt.*

object Dependencies {

//  lazy val grpc = Seq(
//    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
//    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
//    // (optional) If you need scalapb/scalapb.proto or anything from
//    // google/protobuf/*.proto
//    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
//  )

  lazy val logLib = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "ch.qos.logback" % "logback-classic" % "1.5.16",
  )

  lazy val persistence = Seq(
    "org.flywaydb" % "flyway-core" % "11.3.0",
    "org.flywaydb" % "flyway-database-postgresql" % "11.3.0",
    "com.lihaoyi" %% "scalasql" % "0.1.15",
    "com.zaxxer" % "HikariCP" % "6.2.1",
    "org.postgresql" % "postgresql" % "42.7.5" % "compile",
    "mysql" % "mysql-connector-java" % "8.0.33" % "compile",
  )

  lazy val httpClient = {
    val version = "3.10.2"
    Seq(
      "com.softwaremill.sttp.client3" %% "core" % version,
      "com.softwaremill.sttp.client3" %% "circe" % version,
      "com.softwaremill.sttp.client3" %% "slf4j-backend" % version,
    )
  }

  lazy val tapir = {
    val version = "1.11.12"
    val circeVersion = "0.14.9"
    Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
    ).map(_ % circeVersion) ++
      Seq(
        "io.circe" %% "circe-optics" % "0.15.0",
        "com.softwaremill.sttp.tapir" %% "tapir-netty-server" % version,
        "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % version,
        // docs
        "com.softwaremill.sttp.tapir" %% "tapir-redoc-bundle" % version,
        // static file
        "com.softwaremill.sttp.tapir" %% "tapir-files" % version,
        "com.softwaremill.sttp.tapir" %% "tapir-enumeratum" % version,
        // monitor, you could use openTelemetry java agent to solve metrics collector.
        // "com.softwaremill.sttp.tapir" %% "tapir-opentelemetry-metrics" % version,
      )
  }

  lazy val configLib = {
    Seq(
      // "com.typesafe" % "config" % "1.4.2",
      "io.circe" %% "circe-config" % "0.10.1"
    )
  }

  lazy val enumExtraLib = {
    val version = "1.7.5"
    Seq(
      "com.beachape" %% "enumeratum" % version,
      // "com.beachape" %% "enumeratum-quill" % version,
      "com.beachape" %% "enumeratum-circe" % version,
    )
  }

  lazy val quartzLib = {
    Seq(
      "org.quartz-scheduler" % "quartz" % "2.5.0",
    )
  }

  lazy val jwkLib = {
    Seq(
      "org.pac4j" % "pac4j-jwt" % "6.1.0",
      // "org.keycloak" % "keycloak-admin-client" % "26.0.4",
    )
  }

  lazy val rocketMQLib = {
    Seq(
      "org.apache.rocketmq" % "rocketmq-client-java" % "5.0.7",
    )
  }

  lazy val aiLib = {
    Seq(
      "com.softwaremill.sttp.openai" %% "core" % "0.2.6"
    )
  }
  // lazy val templateLib = Seq("nl.big-o" % "liqp"% "0.9.0.3")
}
