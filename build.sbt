ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.2"
ThisBuild / resolvers += "jcenter" at "https://jcenter.bintray.com"

lazy val root = project
  .in(file("."))
  .aggregate(
    common,
    api,
    certificateNumberScraper,
    certificateScraperSeaiIeHtml,
    certificateScraperSeaiIePdf,
    eircodeScraperEircodeIe
  )

lazy val common = project
  .settings(
    libraryDependencies ++= List(
      "dev.zio" %% "zio" % "2.0.10",
      "dev.zio" %% "zio-streams" % "2.0.10",
      "com.google.cloud" % "google-cloud-firestore" % "3.9.2",
      "com.firebase" % "geofire-java" % "3.0.0",
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test
    )
  )

lazy val api = project
  .dependsOn(common)
  .settings(
    dockerRepository := Some("gcr.io/deed-ie/building-energy-rating"),
    dockerAliases ++= Seq(
      s"time-${Environment.instant}",
      s"sha-${Environment.gitShortSha1}"
    )
      .map(Option.apply)
      .map(dockerAlias.value.withTag),
    dockerExposedPorts ++= Seq(8080),
    libraryDependencies ++= List(
      "dev.zio" %% "zio-http" % "0.0.5"
    )
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)

lazy val certificateNumberScraper = project
  .dependsOn(common)
  .settings(
    libraryDependencies ++= List(
      "dev.zio" %% "zio-http" % "0.0.5"
    )
  )

lazy val certificateScraperSeaiIeHtml = project
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= List(
      "com.microsoft.playwright" % "playwright" % "1.31.0"
    )
  )

lazy val certificateScraperSeaiIePdf = project
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= List(
      "org.apache.pdfbox" % "pdfbox" % "2.0.27",
      "dev.zio" %% "zio-http" % "0.0.5"
    )
  )

lazy val eircodeScraperEircodeIe = project
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= List(
      "dev.zio" %% "zio-http" % "0.0.5",
      "dev.zio" %% "zio-json" % "0.4.2"
    )
  )
