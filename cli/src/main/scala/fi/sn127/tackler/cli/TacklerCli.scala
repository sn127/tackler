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
import java.io.{File => JFile}
import java.nio.file.{Files, NoSuchFileException, Path, Paths}

import better.files._
import org.slf4j.{Logger, LoggerFactory}

import fi.sn127.tackler.core.{Settings, TacklerException, TxnException}
import fi.sn127.tackler.model.Txns
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
   * Get input files.
   *
   * @param cliCfg cli args (especially those which are not morphosed to the settings
   * @param settings active set of settings
   * @return list of input files
   */
  def getInputs(cliCfg: TacklerCliArgs, settings: Settings): Seq[Path] = {

    cliCfg.input_filename.toOption.fold({
      // No input file
      if (cliCfg.input_txn_glob.isDefined) {
        log.debug("input glob: dir: [" + settings.input_txn_dir.toString + "] " +
          "glob: [" + settings.input_txn_glob.toString() + "]")
      } else {
        log.debug("Using default settings for input, input glob: dir: [" + settings.input_txn_dir.toString + "] " +
          " glob: [" + settings.input_txn_glob.toString() + "]")
      }
      // cli: input.txn.glob is in any case morphed with settings
      //   -> no need for special handling for cli args
      File(settings.input_txn_dir)
        .glob(settings.input_txn_glob)
        .map(f => f.path)
        .toSeq
    }) { inputFilename =>
      // There was an input file on cli
      val inputPath = settings.getPathWithSettings(inputFilename)

      log.debug("input file: [" + inputPath.toString + "]")
      List(inputPath)
    }
  }

  /**
   * Run main program, this will throw an exception
   * in case of error.
   *
   * @param args cmd line args
   */
  def runExceptions(args: Array[String]): Unit = {
    val tsStart = System.currentTimeMillis()

    val cliCfg = new TacklerCliArgs(args)
    val settings = new Settings(getCfgPath(cliCfg.cfg.toOption), cliCfg.toConfig)

    val output: Option[Path] = cliCfg.output.toOption.map(o => settings.getPathWithSettings(o))

    val tt = new TacklerTxns(settings)

    val tsParseStart = System.currentTimeMillis()
    val txns: Txns = if (true) {
      tt.git2Txns()
    } else {
      val inputs = getInputs(cliCfg, settings)
      tt.inputs2Txns(inputs)
    }
    if (txns.isEmpty) {
      throw new TxnException("Empty transaction set")
    }
    val tsParseEnd = System.currentTimeMillis()

    println("Txns size: " + txns.size.toString)

    val tsReportsStart = System.currentTimeMillis()

    val reporter = Reports(settings)

    reporter.doReports(output, txns)

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
      case ex: org.rogach.scallop.exceptions.ScallopException =>
        // Error message is already printed by CliArgs
        FAILURE
      case ex: NoSuchFileException =>
        Console.err.println("Error: File not found: " + ex.getMessage)
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
