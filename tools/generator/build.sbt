import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "fi.sn127",
      scalaVersion := "2.12.1",
      version      := "0.2.2-devel"
    )),
    name := "generator",
    assemblyJarName in assembly := name.value + "-" + version.value + ".jar",
    libraryDependencies += scallop,
    libraryDependencies += betterFiles
  )
