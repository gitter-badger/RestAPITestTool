import sbt.Keys._
import sbt._

object BuildSettings {
  val _version = "0.1-SNAPSHOT"
  val _scalaVersion = "2.11.7"

  lazy val basicSettings = Seq(
    version               := _version,
    homepage              := Some(new URL("https://github.com/ximagination80")),
    organization          := "imagination",
    organizationHomepage  := Some(new URL("https://github.com/ximagination80")),
    description           := "Rest API test tool ",
    startYear             := Some(2015),
    scalaVersion          := _scalaVersion,
    scalacOptions         := Seq(
      "-encoding", "utf8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-target:jvm-1.6",
      "-language:_",
      "-Xlog-reflective-calls"
    )
  )

}
