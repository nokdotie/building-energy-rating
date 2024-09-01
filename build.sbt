import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}

ThisBuild / scalaVersion := "3.3.1"
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
    githubOwner      := "nokdotie",
    githubRepository := "building-energy-rating",
    resolvers += Resolver.githubPackages("nokdotie"),
    libraryDependencies ++= List(
      "com.google.cloud"  % "google-cloud-firestore" % "3.15.7",
      "ie.nok"           %% "scala-libraries"        % "20240627.143942.92337699" % "compile->compile;test->test",
      "org.apache.pdfbox" % "pdfbox"                 % "3.0.0",
      "org.scalameta"    %% "munit"                  % "0.7.29"                   % Test,
      "org.scalameta"    %% "munit-scalacheck"       % "0.7.29"                   % Test
    ),
    Test / publishArtifact := true
  )

lazy val scraper = project
  .dependsOn(`building-energy-rating` % "compile->compile;test->test")
