ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.0"
ThisBuild / resolvers += "jcenter" at "https://jcenter.bintray.com"

lazy val root = project
  .in(file("."))
  .aggregate(
    common,
    api,
    auth,
    scraper,
    ecad
  )

lazy val common = project
  .settings(
    resolvers += Resolver.githubPackages("nok-ie"),
    libraryDependencies ++= List(
      "dev.zio" %% "zio" % "2.0.13",
      "dev.zio" %% "zio-http" % "0.0.5",
      "dev.zio" %% "zio-json" % "0.5.0",
      "dev.zio" %% "zio-streams" % "2.0.13",
      "com.firebase" % "geofire-java" % "3.0.0",
      "org.apache.pdfbox" % "pdfbox" % "2.0.28",
      "ie.nok" %% "scala-libraries" % "20230610.102516.252288962",
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test
    )
  )

lazy val api = project
  .dependsOn(common, auth)
  .settings(
    dockerRepository := Some("gcr.io/deed-ie/building-energy-rating"),
    dockerAliases ++= Seq(
      s"time-${Environment.instant}",
      s"sha-${Environment.gitShortSha1}"
    )
      .map(Option.apply)
      .map(dockerAlias.value.withTag),
    dockerExposedPorts ++= Seq(8080)
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)

lazy val scraper = project
  .dependsOn(common % "compile->compile;test->test")

lazy val auth = project
  .dependsOn(common % "compile->compile;test->test")

lazy val ecad = project
  .dependsOn(common % "compile->compile;test->test")
