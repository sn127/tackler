import sbt._
import Keys._

object Dependencies {
  /*
   * Versions
   */
  val betterFilesVersion = "3.0.0"
  val catsVersion = "0.9.0"
  val scalatestVersion = "3.0.1"
  val scallopVersion = "2.1.1"
  val configVersion = "1.3.1"
  val sn127UtilsTestingVersion = "0.6.0"
  //val jgitVersion = "4.6.0.201612231935-r"
  val logbackVersion = "1.2.3"
  val scalaArmVersion = "2.0"


  /*
   * Libraries
   */
  /* lib: scala */
  val betterFiles = "com.github.pathikrit" %% "better-files" % betterFilesVersion
  val cats_core = "org.typelevel" %% "cats-core" % catsVersion
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
  val scallop = "org.rogach" %% "scallop" % scallopVersion
  val sn127UtilsTesting = "fi.sn127" %% "utils-testing" % sn127UtilsTestingVersion
  val scalaArm = "com.jsuereth" %% "scala-arm" % scalaArmVersion

  /* lib: java */
  // val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % jgitVersion
  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
  val typesafeConfig = "com.typesafe" % "config" % configVersion

}
