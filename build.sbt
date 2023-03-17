ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.2"
ThisBuild / resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// https://oss.sonatype.org/content/repositories/snapshots/dev/zio/zio-http_3/

lazy val root = project
  .in(file("."))
  .aggregate(
    common,
    api,
    certificateNumberScraper,
    certificateScraperSeaiIeHtml,
    certificateScraperSeaiIePdf
  )

lazy val common = project
  .settings(
    libraryDependencies ++= List(
      "dev.zio" %% "zio" % "2.0.9",
      "dev.zio" %% "zio-streams" % "2.0.9",
      "com.google.cloud" % "google-cloud-firestore" % "3.8.1",
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )

lazy val api = project
  .dependsOn(common)
  .settings(
    dockerRepository := Some("gcr.io/deed-ie"),
    dockerExposedPorts ++= Seq(8080),
    libraryDependencies ++= List(
      "dev.zio" %% "zio-http" % "0.0.4+42-6f1aa906-SNAPSHOT"
    )
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)

lazy val certificateNumberScraper = project
  .dependsOn(common)
  .settings(
    libraryDependencies ++= List(
      "dev.zio" %% "zio-http" % "0.0.4+42-6f1aa906-SNAPSHOT"
    )
  )

lazy val certificateScraperSeaiIeHtml = project
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= List(
      "com.microsoft.playwright" % "playwright" % "1.30.0"
    )
  )

lazy val certificateScraperSeaiIePdf = project
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= List(
      "org.apache.pdfbox" % "pdfbox" % "2.0.27",
      "dev.zio" %% "zio-http" % "0.0.4+42-6f1aa906-SNAPSHOT"
    )
  )
