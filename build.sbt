import sbt.Resolver

import Common._

name := "kafka-consumers"

organization in ThisBuild := "com.ebiznext"

logLevel in Global := Level.Info

crossScalaVersions in ThisBuild := Seq("2.11.8")

scalaVersion in ThisBuild := "2.11.8"

parallelExecution in Test := false

/////////////////////////////////
// Defaults
/////////////////////////////////

com.ebiznext.sbt.build.Publication.settings

/////////////////////////////////
// Useful aliases
/////////////////////////////////

addCommandAlias("cd", "project") // navigate the projects

addCommandAlias("cc", ";clean;compile") // clean and compile

addCommandAlias("pl", ";clean;publishLocal") // clean and publish locally

addCommandAlias("pr", ";clean;publish") // clean and publish globally

addCommandAlias("pld", ";clean;local:publishLocal;dockerComposeUp") // clean and publish/launch the docker environment

(shellPrompt in ThisBuild) := prompt

resolvers in ThisBuild ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "mvn repo" at "https://mvnrepository.com/artifact/",
  "ebiz repo" at "https://art.ebiznext.com/artifactory/libs-release-local",
  Resolver.bintrayRepo("cakesolutions", "maven")
)

val scalaTestV = "2.2.6"
val kafkaV = "0.10.1.1"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "connect-api" % kafkaV,
  "net.cakesolutions" %% "scala-kafka-client" % kafkaV,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "net.cakesolutions" %% "scala-kafka-client-testkit" % kafkaV % "test",
  "org.scalatest" %% "scalatest" % scalaTestV % "test",
  "log4j" % "log4j" % "1.2.17" % "test"
)

