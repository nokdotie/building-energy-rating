ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )

lazy val api = project
  .settings(
    name := "api",
    libraryDependencies += "dev.zio" %% "zio-http" % "0.0.4",
    libraryDependencies += "io.d11" %% "zhttp" % "2.0.0-RC11",
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )

lazy val common = project
  .settings(
    name := "common",
    libraryDependencies ++= List(
      "dev.zio" %% "zio" % "2.0.9",
      "com.google.cloud" % "google-cloud-firestore" % "3.8.1",
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )

lazy val certificateNumberScraper = project
  .dependsOn(common)
  .settings(
    libraryDependencies ++= List(
      "dev.zio" %% "zio" % "2.0.9",
      "dev.zio" %% "zio-http" % "0.0.4",
      "dev.zio" %% "zio-streams" % "2.0.9"
    )
  )

lazy val certificateScraper = project
  .settings(
    libraryDependencies ++= List(
      "com.microsoft.playwright" % "playwright" % "1.30.0"
    )
  )
