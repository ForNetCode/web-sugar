val scala3Version = "3.2.1"


// zio-json default value needs this
ThisBuild / scalacOptions ++= Seq("-Yretain-trees")

import Dependencies.*

lazy val webSugar = project
  .in(file("."))
  .settings(
    name := "web-sugar",
    version := "0.1.0",

    scalaVersion := scala3Version,

    libraryDependencies ++= grpc ++ persistence ++ logLib ++ webServer ++ configLib ++
      keycloakLib ++ httpClient ++ Seq(
      "dev.zio" %% "zio-prelude" % "1.0.0-RC16", // for validate
      "com.github.seancfoley" % "ipaddress" % "5.4.0", // for ip parse
      "org.hashids" % "hashids" % "1.0.3", // hashids
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )
