ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.2"

lazy val root = project
  .in(file("."))
  .aggregate(common, certificateNumberScraper, certificateScraper)

lazy val common = project
  .settings(
    libraryDependencies ++= List(
      "dev.zio" %% "zio" % "2.0.9",
      "dev.zio" %% "zio-json" % "0.4.2",
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
  .dependsOn(common)
  .settings(
    libraryDependencies ++= List(
      "dev.zio" %% "zio" % "2.0.9",
      "dev.zio" %% "zio-streams" % "2.0.9",
      "com.microsoft.playwright" % "playwright" % "1.30.0"
    )
  )
