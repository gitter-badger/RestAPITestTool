import sbt._
import Keys._

object Build extends Build {

  import BuildSettings._
  import Dependencies._

  // -------------------------------------------------------------------------------------------------------------------
  // Root Project
  // -------------------------------------------------------------------------------------------------------------------

  lazy val root = Project("rest-root", base = file("."))
    .settings(basicSettings: _*)
    .settings(libraryDependencies ++=
      lib(
        typesafeConfig,
        scopt,
        imaginationComparator
      )
    )
    .dependsOn(configParser,comparatorProject)
    .aggregate(configParser)

  // -------------------------------------------------------------------------------------------------------------------
  // Modules
  // -------------------------------------------------------------------------------------------------------------------

  lazy val configParser = Project("config-parser", base = file("config-parser"))
    .settings(basicSettings: _*)
    .settings(libraryDependencies ++=
      test(scalatest)
    )

  lazy val comparatorProject = RootProject(uri("git://github.com/ximagination80/Comparator.git"))
}
