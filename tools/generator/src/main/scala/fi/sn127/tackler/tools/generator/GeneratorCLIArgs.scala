package fi.sn127.tackler.tools.generator

import org.rogach.scallop.{ScallopConf, ScallopOption}
import org.rogach.scallop.exceptions.{Help, ScallopException, Version}

class GeneratorCLIArgs(args: Seq[String]) extends ScallopConf(args) {

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


  val basedir: ScallopOption[String] = opt[String](
    noshort = true, required = false,
    descr = """basedir for generated txn data. Default is "./data""""
  )

  val compatible: ScallopOption[Boolean] = opt[Boolean](
    noshort = true, required = false,
    descr = """if this is given, then ledger compatible data is generated""")


  val single_file: ScallopOption[Boolean] = opt[Boolean](
    name = "single-file",  noshort = true, required = false,
    descr = """if this is given, then single txn-file is generated""")

  val count: ScallopOption[String] = opt[String](
    noshort = true, required = false,
    descr = """how many txns to generate: [1E3, 1E4, 1E5, 1E6]""".stripMargin)

  verify()
}
