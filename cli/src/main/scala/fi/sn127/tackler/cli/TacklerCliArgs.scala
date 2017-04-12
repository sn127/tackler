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

  /**
   * CLI-args which overrides Config settings.
   */
  /**
   * Get CLI Conf-file over-riding arguments as Config.
   *
   * @return config instance with cli-args as config values
   */
  def toConfig: Config = {

    // simple strings
    val strConfig = opts2Config(List(
      basedir,
      input_txn_dir,
      input_txn_glob,
      accounts_strict,
      console))

    /*
     * Chain Lists-type cli-arguments and convert to Config
     */
    val reportsConfig = rpts.toOption match {
      case Some(reports) =>
        strConfig.withValue(
          CfgKeys.reporting_reports,
          ConfigValueFactory.fromIterable(JavaConverters.asJavaIterable(reports)))
      case None =>
        strConfig
    }

    val reportingAccountsConfig = afilt.toOption match {
      case Some(accounts) =>
        reportsConfig.withValue(
          CfgKeys.reporting_accounts,
          ConfigValueFactory.fromIterable(JavaConverters.asJavaIterable(accounts)))
      case None =>
        reportsConfig
    }

    // super-set of all configs
    reportingAccountsConfig
  }

  /**
   * Actual command line arguments which override config settings
   */
  val basedir: ScallopOption[String] = opt[String](
    name=CfgKeys.basedir,required = false, noshort = true)

  val input_txn_dir: ScallopOption[String] = opt[String](
    name=CfgKeys.input_txn_dir, required = false, noshort = true)

  val input_txn_glob: ScallopOption[String] = opt[String](
    name=CfgKeys.input_txn_glob, required = false, noshort = true)

  val accounts_strict: ScallopOption[String] = opt[String](
    name=CfgKeys.accounts_strict, required = false, noshort = true)

  val console: ScallopOption[String] = opt[String](
    name=CfgKeys.reporting_console, required = false, noshort = true)

  val rpts: ScallopOption[List[String]] = opt[List[String]](
    name=CfgKeys.reporting_reports, required = false, noshort = true)

  val afilt: ScallopOption[List[String]] = opt[List[String]](
    name=CfgKeys.reporting_accounts, required = false, noshort = true)

  /*
   * Verify and sanity check cli args
   */
  conflicts(input_filename, List(input_txn_dir, input_txn_glob))
  dependsOnAll(input_txn_dir, List(input_txn_glob))
  verify()
}
