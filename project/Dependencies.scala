import sbt._

object Dependencies {

  def compile (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")
  def lib (deps: ModuleID*): Seq[ModuleID] = deps

  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4"
  val typesafeConfig =  "com.typesafe" % "config" % "1.3.0"
  val imaginationComparator =  "imagination" % "comparator_2.11" % "0.3-SNAPSHOT"
  val scopt = "com.github.scopt" %% "scopt" % "3.3.0"
}