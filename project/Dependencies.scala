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
    "ch.qos.logback" % "logback-classic" % "1.5.18",
  )

  lazy val persistence = Seq(
    "org.flywaydb" % "flyway-core" % "11.10.3",
    "org.flywaydb" % "flyway-database-postgresql" % "11.10.3",
    "com.lihaoyi" %% "scalasql" % "0.1.20",
    "com.zaxxer" % "HikariCP" % "6.3.0",
    "org.postgresql" % "postgresql" % "42.7.7" % "compile",
    "mysql" % "mysql-connector-java" % "8.0.33" % "compile",
  )

  lazy val httpClient = {
    val version = "4.0.9"
    Seq(
      "com.softwaremill.sttp.client4" %% "core" % version,
      "com.softwaremill.sttp.client4" %% "circe" % version,
      "com.softwaremill.sttp.client4" %% "slf4j-backend" % version,
    )
  }

  lazy val tapir = {
    val version = "1.11.37"
    val circeVersion = "0.14.9"
    Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
    ).map(_ % circeVersion) ++
      Seq(
        "io.circe" %% "circe-optics" % "0.15.1",
        "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % version,
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
      // "com.typesafe" % "config" % "1.4.3",
      "io.circe" %% "circe-config" % "0.10.2"
    )
  }

  lazy val enumExtraLib = {
    val version = "1.9.0"
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
      "org.pac4j" % "pac4j-jwt" % "6.2.0",
      "org.pac4j" % "pac4j-oidc" % "6.2.0",
      // "org.keycloak" % "keycloak-admin-client" % "26.0.4",
    )
  }

  lazy val rocketMQLib = {
    Seq(
      "org.apache.rocketmq" % "rocketmq-client-java" % "5.0.8",
    )
  }

  lazy val aiLib = {
    Seq(
      "com.softwaremill.sttp.openai" %% "core" % "0.3.6",
    )
  }
  // lazy val templateLib = Seq("nl.big-o" % "liqp"% "0.9.0.3")
}
