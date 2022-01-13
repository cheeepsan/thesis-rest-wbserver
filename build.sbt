name := """elk-thesis-webserver"""
organization := "elk-thesis"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.6"
//scalacOptions += "-Ypartial-unification"


libraryDependencies += guice
libraryDependencies += "org.typelevel" %% "cats-core" % "2.4.2"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

libraryDependencies ++= Seq(

  // Start with this one
  "org.tpolecat" %% "doobie-core"      % "0.12.1",

  "org.tpolecat" %% "doobie-postgres"  % "0.12.1",          // Postgres driver 42.2.19 + type mappings.
  "org.tpolecat" %% "doobie-specs2"    % "0.12.1" % "test", // Specs2 support for typechecking statements. ?????
  "org.tpolecat" %% "doobie-scalatest" % "0.12.1" % "test"  // ScalaTest support for typechecking statements. ??????

)

val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "elk-thesis.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "elk-thesis.binders._"
