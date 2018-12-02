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
import java.nio.file.{Files, NoSuchFileException, Path, Paths}

import better.files._
import org.slf4j.{Logger, LoggerFactory}
import io.circe.parser.decode

import fi.sn127.tackler.core.{FilesystemStorageType, GitStorageType, Settings, TacklerException, TxnException}
import fi.sn127.tackler.filter.TxnFilterRoot
import fi.sn127.tackler.model.TxnData
import fi.sn127.tackler.parser.{TacklerParseException, TacklerTxns}
import fi.sn127.tackler.report.Reports



object TacklerCli {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  val SUCCESS: Int = 0
  val FAILURE: Int = 127


  /**
   * Get CFG path either by optional arg, or if arg is not provided
   * then using default location based on "my" exe-path
   * (e.g. jar-file or class directory).
   *
   * This does not check if file exists or not. That should be done
   * by config-loaders.
   *
   * @param cfgPathArg optional cfg path
   * @return canonical path to config
   */
  def getCfgPath(cfgPathArg: Option[String]): Path = {
    def getExeDir(c: Class[_]): Path = {
      val runRawPath = Paths.get(c.getProtectionDomain.getCodeSource.getLocation.toURI)
      if (Files.isDirectory(runRawPath))
        runRawPath
      else
        runRawPath.getParent
    }

    val cfgPath = cfgPathArg match {
      case Some(path) => File(path)
      case None => File(getExeDir(getClass)) / "tackler.conf"
    }

    cfgPath.path
  }

  /**
   * Get input files paths.
   *
   * @param cliCfg cli args (especially those which are not morphosed to the settings
   * @param settings active set of settings
   * @return list of input files
   */
  def getInputPaths(cliCfg: TacklerCliArgs, settings: Settings): Seq[Path] = {

    cliCfg.input_filename.toOption.fold({
      // cli: input file: NO

      // cli: input.fs.glob is merged with settings -> no need for special handling for cli args
      TacklerTxns.inputPaths(settings)
    }) { cliArgsInputFilename =>
      // cli: input file: YES

      val inputPath = settings.getPathWithSettings(cliArgsInputFilename)
      log.debug("CLI: input file: [{}]", inputPath.toString)
      List(inputPath)
    }
  }

  /**
   * Get input ref or commit id for Git storage
   * based on settings and command line arguments.
   *
   * Use in that order:
   * - If cli: commit => use that
   * - If cli: ref => use that
   * - If nothing above => use ref from settings
   *
   * ref and commit both are strings, but semantically
   * they are different, hence Either return type.
   *
   * @param cliCfg command line args
   * @param settings configuration
   * @return either ref or commit
   */
  def getInputRef(cliCfg: TacklerCliArgs, settings: Settings): Either[String, String] = {

    cliCfg.input_git_commit.toOption.fold[Either[String, String]]({
      // cli: git commit: NO
      // cli: input.git.ref is merged with settings -> no need for special handling for cli args
      TacklerTxns.gitReference(settings)
    }){ cliArgCommit =>
      // cli: git commit: YES
      TacklerTxns.gitCommitId(cliArgCommit)
    }
  }

  /**
   * Run main program, this will throw an exception
   * in case of error.
   *
   * @param args cmd line args
   */
  @SuppressWarnings(Array("org.wartremover.warts.EitherProjectionPartial"))
  def runExceptions(args: Array[String]): Unit = {
    val tsStart = System.currentTimeMillis()

    val cliCfg = new TacklerCliArgs(args)
    val settings = Settings(getCfgPath(cliCfg.cfg.toOption), cliCfg.toConfig)

    val output: Option[Path] = cliCfg.output.toOption.map(o => settings.getPathWithSettings(o))

    val tt = new TacklerTxns(settings)

    val tsParseStart = System.currentTimeMillis()

    val txnDataAll: TxnData = settings.input_storage match {
      case GitStorageType() => {
        val inputRef = getInputRef(cliCfg, settings)
        tt.git2Txns(inputRef)
      }

      case FilesystemStorageType() => {
        val paths = getInputPaths(cliCfg, settings)
        tt.paths2Txns(paths)
      }
    }

    val txnData = cliCfg.api_filter_def.toOption.fold({
      // cli: api-filter-def: NO
      txnDataAll
    }) { filterJsonStr =>
      // cli: api-filter-def: YES

      val jsonDecodeResult = decode[TxnFilterRoot](filterJsonStr)

      if (jsonDecodeResult.isLeft) {
        val err = jsonDecodeResult.left.get
        throw new TxnException("JSON parse error: " + err.getMessage())
      }

      jsonDecodeResult.toOption.map(txnFilterRoot => {
        txnDataAll.filter(txnFilterRoot)
      }).getOrElse(txnDataAll) // this is not possible
    }

    if (txnData.txns.isEmpty) {
      throw new TxnException("Empty transaction set")
    }
    val tsParseEnd = System.currentTimeMillis()

    println("Txns size: " + txnData.txns.size.toString)

    val tsReportsStart = System.currentTimeMillis()

    val reporter = Reports(settings)

    reporter.writeReports(output, txnData)

    val tsReportsEnd = System.currentTimeMillis()

    val tsEnd = System.currentTimeMillis()

    Console.err.println("\nTotal processing time: %d, parse: %d, reporting: %d".format(
      tsEnd - tsStart,
      tsParseEnd - tsParseStart,
      tsReportsEnd - tsReportsStart))
  }

  /**
   * Run main function and catches all Exceptions.
   *
   * @param args cmd line args
   * @return Zero on success, non-zero in case of error.
   */
  def runReturnValue(args: Array[String]): Int = {
    try {
      runExceptions(args)
      SUCCESS
    } catch {
      case org.rogach.scallop.exceptions.Help("") =>
        // do not report success
        FAILURE
      case org.rogach.scallop.exceptions.Version =>
        // do not report success
        Console.out.println("Version: " + BuildInfo.version + " [" + BuildInfo.builtAtString + "]")
        FAILURE
      case _: org.rogach.scallop.exceptions.ScallopException =>
        // Error message is already printed by CliArgs
        FAILURE
      case ex: NoSuchFileException =>
        Console.err.println("Error: File not found: " + ex.getMessage)
        FAILURE
      case ex: java.util.regex.PatternSyntaxException =>
        Console.err.println("Error: regexp syntax error: " + ex.getMessage)
        FAILURE
      case ex: TacklerParseException =>
        Console.err.println("" +
          "Exception: \n" +
          "  class: " + ex.getClass.toString + "\n" +
          "  msg: " + ex.getMessage + "\n")
        FAILURE
      case ex: TacklerException =>
        Console.err.println("" +
          "Exception: \n" +
          "  class: " + ex.getClass.toString + "\n" +
          "  msg: " + ex.getMessage + "\n")
        FAILURE
    }
  }

  def main(args: Array[String]): Unit = {
    System.exit(runReturnValue(args))
  }
}
