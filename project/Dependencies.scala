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
  val betterFilesVersion = "3.0.0"
  val catsVersion = "0.9.0"
  val scalatestVersion = "3.0.2"
  val scallopVersion = "2.1.1"
  val configVersion = "1.3.1"
  val sn127UtilsTestingVersion = "0.6.0"
  val jgitVersion = "4.7.0.201704051617-r"
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
  val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % jgitVersion
  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
  val typesafeConfig = "com.typesafe" % "config" % configVersion

}
