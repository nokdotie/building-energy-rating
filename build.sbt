ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )

lazy val certificateNumberScraper = project
  .settings(
    libraryDependencies ++= List(
      "dev.zio" %% "zio" % "2.0.6",
      "dev.zio" %% "zio-http" % "0.0.4",
      "dev.zio" %% "zio-streams" % "2.0.6",
    )
  )
