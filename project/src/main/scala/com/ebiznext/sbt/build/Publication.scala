package com.ebiznext.sbt.build

import sbt._

object Publication {

  def settings: Seq[Def.Setting[_]] = Seq(
    (Keys.publishTo in ThisBuild) := selectDestination((Keys.version in Keys.publish).value),
    Keys.publishMavenStyle := true,
    Keys.credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
  )

  val artifactoryUrl = "https://art.ebiznext.com/artifactory/"

  val releasesRepository = "releases" at artifactoryUrl + "libs-release-local"

  val snapshotsRepository = "snapshots" at artifactoryUrl + "libs-snapshot-local"


  private def selectDestination(version: String) =
    if(version.trim.toUpperCase.endsWith("SNAPSHOT")) Some(snapshotsRepository)
    else Some(releasesRepository)
}
