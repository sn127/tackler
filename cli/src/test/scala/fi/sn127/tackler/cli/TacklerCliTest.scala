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

import better.files.File
import org.rogach.scallop.exceptions.{ExcessArguments, RequiredOptionNotFound, UnknownOption, ValidationFailure}
import resource._

import fi.sn127.tackler.core.{AccountException, GroupByException, ReportException, TxnException}
import fi.sn127.tackler.parser.TacklerParseException
import fi.sn127.utils.fs.Glob
import fi.sn127.utils.testing.DirSuiteLike

class ConsoleTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  override
  protected def mapArgs(testname: Path, args: Array[String]): Array[String] = {

    val test = File(testname)
    val basename = test.nameWithoutExtension

    val stdout = "out." + basename + ".stdout.txt"
    val stderr = "out." + basename + ".stderr.txt"
    val stdoutPath = test.parent / stdout
    val stderrPath = test.parent / stderr

    Array(stdoutPath.toString, stderrPath.toString) ++ args
  }

  runDirSuiteTestCases(basedir, Glob("cli/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {

      val rv = for {
        stdout <- managed(Files.newOutputStream(Paths.get(args(0))))
        stderr <- managed(Files.newOutputStream(Paths.get(args(1))))
      } yield {
        Console.withOut(stdout) {
          Console.withErr(stderr) {
            TacklerCli.runReturnValue(args.drop(2))
          }
        }
      }
      // funky map(u=>u): https://github.com/jsuereth/scala-arm/issues/49
      rv.map(u => u).opt.getOrElse(TacklerCli.FAILURE)
    }
  }
}

class TacklerCliTest extends DirSuiteLike {
  val basedir = Paths.get("tests")

  /**
   * Run all failure tests from everywhere
   */
  runDirSuiteTestCases(basedir, Glob("**/ex/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.FAILURE) {
      TacklerCli.runReturnValue(args)
    }
  }

  /**
   * CLI Exceptions
   */
  runDirSuiteTestCases(basedir, Glob("cli/ex/NoSuchFileException-*.exec")) { args: Array[String] =>
    assertThrows[NoSuchFileException]{
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("cli/ex/TxnException-*.exec")) { args: Array[String] =>
    assertThrows[TxnException]{
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("cli/ex/AccountException-*.exec")) { args: Array[String] =>
    assertThrows[AccountException]{
      TacklerCli.runExceptions(args)
    }
  }

  ignoreDirSuiteTestCases(basedir, Glob("cli/ex/RequiredOptionNotFound-*.exec")) { args: Array[String] =>
    assertThrows[RequiredOptionNotFound]{
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("cli/ex/ValidationFailure-*.exec")) { args: Array[String] =>
    assertThrows[ValidationFailure]{
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("cli/ex/ExcessArguments-*.exec")) { args: Array[String] =>
    assertThrows[ExcessArguments]{
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("cli/ex/UnknownOption-*.exec")) { args: Array[String] =>
    assertThrows[UnknownOption]{
      TacklerCli.runExceptions(args)
    }
  }

  /**
   * cli ok case is done with stdout and stderr validation
   */

  /**
   * Core
   */
  runDirSuiteTestCases(basedir, Glob("core/ex/TxnException-*.exec")) { args: Array[String] =>
    assertThrows[TxnException]{
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("core/ex/NoSuchFileException-*.exec")) { args: Array[String] =>
    assertThrows[NoSuchFileException]{
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("core/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }

  /**
   * Parser.
   */
  runDirSuiteTestCases(basedir, Glob("parser/ex/*.exec")) { args: Array[String] =>
    assertThrows[TacklerParseException]{
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("parser/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }

  /**
   * Accumulator.
   */
  runDirSuiteTestCases(basedir, Glob("accumulator/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }

  /**
   * Reporting.
   */
  runDirSuiteTestCases(basedir, Glob("reporting/ex/ReportException-*.exec")) { args: Array[String] =>
    assertThrows[ReportException]{
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("reporting/ex/GroupByException-*.exec")) { args: Array[String] =>
    assertThrows[GroupByException]{
      TacklerCli.runExceptions(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("reporting/ok/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }

  runDirSuiteTestCases(basedir, Glob("reporting/group-by/*.exec")) { args: Array[String] =>
    assertResult(TacklerCli.SUCCESS) {
      TacklerCli.runReturnValue(args)
    }
  }
}
