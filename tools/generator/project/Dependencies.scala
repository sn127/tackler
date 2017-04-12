import sbt._

object Dependencies {
  /*
   * Versions
   */
  val betterFilesVersion = "2.17.2-SNAPSHOT"
  val scallopVersion = "2.1.0"

  /*
   * Libraries
   */
  /* lib: scala */
  lazy val scallop = "org.rogach" %% "scallop" % scallopVersion
  lazy val betterFiles = "com.github.pathikrit" %% "better-files" % betterFilesVersion

}
