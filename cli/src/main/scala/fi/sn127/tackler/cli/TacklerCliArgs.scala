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
package fi.sn127.tackler.cli

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import org.rogach.scallop.exceptions.{Help, ScallopException, Version}
import org.rogach.scallop.{ScallopConf, ScallopOption}

import scala.collection.JavaConverters

import fi.sn127.tackler.core.CfgKeys

/**
 * Handles Tackler CLI Arguments, especially provides converter to Config object.
 *
 * @param args command-line arguments
 */
class TacklerCliArgs(args: Seq[String]) extends ScallopConf(args) {

  override def onError(e: Throwable): Unit = e match {
    case Help("") =>
      printHelp
      throw e
    case Version =>
      throw e
    case ex: ScallopException => {
      printHelp
      println("\nError with CLI Arguments: " + ex.getMessage)
      throw ex
    }
    case other => super.onError(other)
  }

  /**
   * Translate list of Scallop options to Config instance
   * 
   * @param opts list of Scallop options
   * @return Config instance
   */
  private def opts2Config(opts: List[ScallopOption[String]]): Config = {
    def opt2MapItem(opt: ScallopOption[String]): List[(String,String)] = {
      opt.map(o => (opt.name, o)).toOption.toList
    }

    val optsAsMap: Map[String, String] = opts.flatMap(o => opt2MapItem(o)).toMap

    ConfigFactory.parseMap(JavaConverters.mapAsJavaMap(optsAsMap))
  }

  version("Version: " + BuildInfo.version + " [" + BuildInfo.builtAtString + "]")

  /**
   * Pure CLI-args (e.g. these don't have a setting in Config)
   */
  val cfg: ScallopOption[String] = opt[String](noshort = true)
  val output: ScallopOption[String] = opt[String](noshort = true)

  val input_filename: ScallopOption[String] = opt[String](
    name="input.file", required = false, noshort = true)

  val input_git_commit: ScallopOption[String] = opt[String](
    name="input.git.commit", required = false, noshort = true)

  /*
   * This is mostly used for testing,
   * Hence api at begin and dot instead of dash
   */
  val api_filter_def: ScallopOption[String] = opt[String](
    name="api-filter-def", required = false, noshort = true)


  //
  // CLI-args which overrides Config settings.
  //
  /**
   * Get CLI arguments which overrides conf-file settings as Config.
   *
   * @return config instance with cli-args merged as config values
   */
  def toConfig: Config = {
    // Handle simple strings
    val stringArgsConf = opts2Config(List(
      basedir,
      input_fs_dir,
      input_fs_glob,
      input_git_ref,
      accounts_strict,
      console))

    //
    // Handle List[String] style cli-arguments
    //
    // Handle these by making one config (with one key with list-values)
    // per one cli-argument, and then chain/glue these together
    // with existing config ("withValue").
    // End result is one config which have these cli-args
    // as key[String], valueList[String]
    val reportsConfig = reports.toOption match {
      case Some(reports) =>
        stringArgsConf.withValue(
          CfgKeys.reporting_reports,
          ConfigValueFactory.fromIterable(JavaConverters.asJavaIterable(reports)))
      case None =>
        stringArgsConf
    }

    val exportsConfig = exports.toOption match {
      case Some(exports) =>
        reportsConfig.withValue(
          CfgKeys.reporting_exports,
          ConfigValueFactory.fromIterable(JavaConverters.asJavaIterable(exports)))
      case None =>
        reportsConfig
    }

    val reportingAccountsConfig = afilt.toOption match {
      case Some(accounts) =>
        exportsConfig.withValue(
          CfgKeys.reporting_accounts,
          ConfigValueFactory.fromIterable(JavaConverters.asJavaIterable(accounts)))
      case None =>
        exportsConfig
    }

    val formatsConfig = formats.toOption match {
      case Some(formats) =>
        reportingAccountsConfig.withValue(
          CfgKeys.reporting_formats,
          ConfigValueFactory.fromIterable(JavaConverters.asJavaIterable(formats)))
      case None =>
        reportingAccountsConfig
    }

    // End of Handle List[String] style cli-arguments.
    // this is super-set of all above configs merged together
    formatsConfig
  }

  //
  // Actual command line arguments which override config settings
  //
  val basedir: ScallopOption[String] = opt[String](
    name=CfgKeys.basedir,required = false, noshort = true)

  val input_fs_dir: ScallopOption[String] = opt[String](
    name=CfgKeys.input_fs_dir, required = false, noshort = true)

  val input_fs_glob: ScallopOption[String] = opt[String](
    name=CfgKeys.input_fs_glob, required = false, noshort = true)

  val input_git_ref: ScallopOption[String] = opt[String](
    name=CfgKeys.input_git_ref, required = false, noshort = true)

  val accounts_strict: ScallopOption[String] = opt[String](
    name=CfgKeys.accounts_strict, required = false, noshort = true)

  val console: ScallopOption[String] = opt[String](
    name=CfgKeys.reporting_console, required = false, noshort = true)

  val reports: ScallopOption[List[String]] = opt[List[String]](
    name=CfgKeys.reporting_reports, required = false, noshort = true)

  val exports: ScallopOption[List[String]] = opt[List[String]](
    name=CfgKeys.reporting_exports, required = false, noshort = true)

  val afilt: ScallopOption[List[String]] = opt[List[String]](
    name=CfgKeys.reporting_accounts, required = false, noshort = true)

  val formats: ScallopOption[List[String]] = opt[List[String]](
    name=CfgKeys.reporting_formats, required = false, noshort = true)

  //
  // Verify and sanity check cli args
  //
  // no git.commit and git.ref
  // no (git.commit | git.ref) && input.file
  // no (git.ref | git.commit) x (txn.dir | txn.glob)
  private val gitArgs = List(input_git_ref, input_git_commit)
  conflicts(input_git_ref, List(input_git_commit))
  conflicts(input_fs_dir, gitArgs)
  conflicts(input_fs_glob, gitArgs)
  conflicts(input_filename, gitArgs)

  conflicts(input_filename, List(input_fs_dir, input_fs_glob))
  dependsOnAll(input_fs_dir, List(input_fs_glob))
  verify()
}
