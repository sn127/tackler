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
import sbt._
import Keys._

object Dependencies {
  /*
   * Versions
   */
  val betterFilesVersion = "3.4.0"
  val catsVersion = "1.1.0"
  val circeVersion = "0.9.3"
  val scalatestVersion = "3.0.5"
  val scallopVersion = "3.1.2"
  val configVersion = "1.3.3"
  val dirsuiteVersion = "0.7.0"
  val jgitVersion = "4.11.0.201803080745-r"
  val logbackVersion = "1.2.3"
  val scalaArmVersion = "2.0"


  /*
   * Libraries
   */
  /* lib: scala */
  val betterFiles = "com.github.pathikrit" %% "better-files" % betterFilesVersion
  val cats_core = "org.typelevel" %% "cats-core" % catsVersion
  val circe_deps = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-java8"
  ).map(_ % circeVersion)

  val circe_deps_test = Seq(
    "io.circe" %% "circe-optics"
  ).map(_ % circeVersion % "test") 

  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
  val scallop = "org.rogach" %% "scallop" % scallopVersion
  val dirsuite = "fi.sn127" %% "dirsuite" % dirsuiteVersion
  val scalaArm = "com.jsuereth" %% "scala-arm" % scalaArmVersion

  /* lib: java */
  val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % jgitVersion
  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
  val typesafeConfig = "com.typesafe" % "config" % configVersion

}
