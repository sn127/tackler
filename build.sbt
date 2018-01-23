/*
 * Copyright 2016-2017 Jani Averbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
import TacklerTests._
import Dependencies._

import sbtcrossproject.{crossProject, CrossType}

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val commonSettings = Seq(
  organization := "fi.sn127",
  version := "0.10.0-SNAPSHOT",
  scalaVersion := "2.12.4",
  compileOrder := CompileOrder.JavaThenScala,
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "utf-8",
    "-explaintypes",
    "-feature",
    "-unchecked",
    "-Xcheckinit",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ypartial-unification",
    "-Ywarn-dead-code",
    "-Ywarn-extra-implicit",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused:implicits",
    "-Ywarn-unused:imports",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:params",
    "-Ywarn-unused:patvars",
    "-Ywarn-unused:privates",
    "-Ywarn-value-discard"
  ),
  Compile / console / scalacOptions --= Seq(
    "-Ywarn-unused:imports",
    "-Xfatal-warnings"
  ),
  Compile / compile / wartremoverWarnings ++= Warts.allBut(
    Wart.ToString,
    Wart.NonUnitStatements,
    Wart.PublicInference,
    Wart.Throw //https://github.com/puffnfresh/wartremover/commit/869763999fcc1fd685c1a8038c974854457b608f
  ),
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
)

/**
 * if "name" is defined in commonSettings, it will cause
  * circular dependencies with sub-projects
  */
lazy val tackler = (project in file(".")).
  aggregate(apiJS, apiJVM, core, cli).
  dependsOn(apiJS, apiJVM, core, cli).
  settings(noPublishSettings).
  settings(commonSettings: _*).
  settings(
    run / fork := true
  )

lazy val api = crossProject(JSPlatform, JVMPlatform).
  crossType(CrossType.Pure).in(file("api")).
  settings(commonSettings: _*).
  settings(
    name := "tackler-api",
    libraryDependencies += "io.circe" %%% "circe-core" % circeVersion,
    libraryDependencies += "io.circe" %%% "circe-generic" % circeVersion,
    libraryDependencies += "io.circe" %%% "circe-java8" % circeVersion,
    libraryDependencies += "io.circe" %%% "circe-parser" % circeVersion
  ).
  jvmSettings(
  ).
  jsSettings(
    coverageEnabled := false,
    coverageExcludedPackages := ".*",
    Test / test := {}
)

lazy val apiJVM = api.jvm
lazy val apiJS = api.js



lazy val core = (project in file("core")).
  dependsOn(apiJVM).
  enablePlugins(Antlr4Plugin).
  settings(commonSettings: _*).
  settings(
    name := "tackler-core",
    run / fork := true,
    antlr4Version in Antlr4 := "4.7.1",
    antlr4GenListener in Antlr4 := false,
    antlr4GenVisitor in Antlr4 := false,
    antlr4PackageName in Antlr4 := Some("fi.sn127.tackler.parser")
  )

lazy val cli = (project in file("cli")).
  enablePlugins(BuildInfoPlugin).
  dependsOn(core).
  settings(noPublishSettings).
  settings(commonSettings: _*).
  settings(
    run / fork := true,
    fork := true,
    Test / baseDirectory := file((Test / baseDirectory).value + "/.."),
    Test / testOptions += {
      // The evaluation of `streams` inside an anonymous function is prohibited.
      // https://github.com/sbt/sbt/issues/3266
      // https://github.com/jeffwilde/sbt-dynamodb/commit/109ea03837b1c1b4f45723c200d7aa5c34bb6e8b
      val log = sLog.value
      Tests.Setup(() => TacklerTests.setup("tests", log))
    },
    assembly / test := {},
    assemblyJarName in assembly := "tackler-cli" + "-" + version.value + ".jar",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoPackage := "fi.sn127.tackler.cli",
    buildInfoUsePackageAsPath := true,
    buildInfoObject := "BuildInfo"
  )
