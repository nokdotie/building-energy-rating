import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}

ThisBuild / scalaVersion := "3.3.0"
ThisBuild / organization := "ie.nok"
ThisBuild / version := DateTimeFormatter
  .ofPattern("yyyyMMdd.HHmmss.n")
  .withZone(ZoneOffset.UTC)
  .format(Instant.now())

lazy val root = project
  .in(file("."))
  .aggregate(
    `building-energy-rating`,
    scraper
  )

lazy val `building-energy-rating` = project
  .settings(
    githubOwner := "nok-ie",
    githubRepository := "building-energy-rating",
    resolvers += Resolver.githubPackages("nok-ie"),
    resolvers += "jcenter" at "https://jcenter.bintray.com",
    libraryDependencies ++= List(
      "dev.zio" %% "zio" % "2.0.15",
      "dev.zio" %% "zio-http" % "0.0.5",
      "dev.zio" %% "zio-json" % "0.6.0",
      "dev.zio" %% "zio-streams" % "2.0.15",
      "com.google.cloud" % "google-cloud-firestore" % "3.13.8",
      "com.firebase" % "geofire-java" % "3.0.0",
      "org.apache.pdfbox" % "pdfbox" % "2.0.29",
      "ie.nok" %% "scala-libraries" % "20230727.095007.865992958",
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test
    )
  )

lazy val scraper = project
  .dependsOn(`building-energy-rating` % "compile->compile;test->test")
