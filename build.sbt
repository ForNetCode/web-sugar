val scala3Version = "3.6.2"

// enumeration need this
ThisBuild / scalacOptions ++= Seq("-Yretain-trees")

import Dependencies.*

lazy val webSugar = project
  .in(file("."))
  .settings(
    name := "web-sugar",
    version := "0.1.0",
    scalaVersion := scala3Version,
    libraryDependencies ++= /* grpc ++ */ persistence ++ logLib ++ configLib ++
      httpClient ++ tapir ++ enumExtraLib ++ Seq(
        "redis.clients" % "jedis" % "5.2.0",
        "io.scalaland" %% "chimney" % "1.6.0", // case class convert easily
        "org.hashids" % "hashids" % "1.0.3", // hashids
        "com.github.pathikrit" %% "better-files" % "3.9.2",
        "org.scalameta" %% "munit" % "1.0.4" % Test,
        // "org.xerial" % "sqlite-jdbc" % "3.47.2.0" % Test,
    )
  )
