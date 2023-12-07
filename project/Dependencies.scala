import sbt.*

object Dependencies {

  lazy val grpc = Seq(
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
    // (optional) If you need scalapb/scalapb.proto or anything from
    // google/protobuf/*.proto
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  )

  lazy val logLib = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "ch.qos.logback" % "logback-classic" % "1.4.7",
  )

  lazy val persistence = Seq(
    "org.flywaydb" % "flyway-core" % "9.19.4",
    "io.getquill" %% "quill-jdbc" % "4.8.0",
    "org.postgresql" % "postgresql" % "42.6.0" % "compile",
    "mysql" % "mysql-connector-java" % "8.0.33" % "compile",
  )

  lazy val httpClient = {
    val version = "3.9.0"
    Seq(
      "com.softwaremill.sttp.client3" %% "core" % version,
      "com.softwaremill.sttp.client3" %% "circe" % version,
      "com.softwaremill.sttp.client3" %% "slf4j-backend" % version,
    )
  }

  lazy val tapir = {
    val version = "1.9.4"
    val circeVersion = "0.14.6"
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
        // monitor, you could use opentelemetry java agent to solve metrics collector.
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
    val version = "1.7.3"
    Seq(
      "com.beachape" %% "enumeratum" % version,
      "com.beachape" %% "enumeratum-quill" % version,
      "com.beachape" %% "enumeratum-circe" % version,
    )
  }

}
