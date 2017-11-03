import TacklerTests._

lazy val commonSettings = Seq(
  version := "0.6.1-next",
  scalaVersion := "2.12.4",
  compileOrder := CompileOrder.JavaThenScala,
  scalacOptions ++= Seq(
    "-Xlint",
    "-feature",
    "-Xfatal-warnings",
    "-deprecation"),
  wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
    Wart.ToString,
    Wart.NonUnitStatements,
    Wart.Throw //https://github.com/puffnfresh/wartremover/commit/869763999fcc1fd685c1a8038c974854457b608f
  )
)
/**
  * if "name" is defined in commonSettings, it will cause
  * circular dependencies with sub-projects
  */
lazy val tackler = (project in file(".")).
  aggregate(core, cli).
  dependsOn(core, cli).
  settings(commonSettings: _*).
  settings(
    name := "tackler",
    fork in run := true
  )

lazy val core = (project in file("core")).
  enablePlugins(Antlr4Plugin).
  settings(commonSettings: _*).
  settings(
    fork in run := true,
    test in assembly := {},
    antlr4Version in Antlr4 := "4.7",
    antlr4GenListener in Antlr4 := false,
    antlr4GenVisitor in Antlr4 := false,
    antlr4PackageName in Antlr4 := Some("fi.sn127.tackler.parser")
    )

lazy val cli = (project in file("cli")).
  enablePlugins(BuildInfoPlugin).
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    fork in run := true,
    fork := true,
    baseDirectory in Test := file((baseDirectory in Test).value + "/.."),
    testOptions in Test += {
      // The evaluation of `streams` inside an anonymous function is prohibited.
      // https://github.com/sbt/sbt/issues/3266
      // https://github.com/jeffwilde/sbt-dynamodb/commit/109ea03837b1c1b4f45723c200d7aa5c34bb6e8b
      val log = sLog.value
      Tests.Setup(() => TacklerTests.setup("tests", log))
    },
    assemblyJarName in assembly := "tackler-cli" + "-" + version.value + ".jar",
    test in assembly := {},
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoPackage := "fi.sn127.tackler.cli",
    buildInfoUsePackageAsPath := true,
    buildInfoObject := "BuildInfo"
  )
