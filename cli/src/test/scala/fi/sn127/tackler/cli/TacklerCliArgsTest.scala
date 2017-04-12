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

import java.nio.file.NoSuchFileException

import org.rogach.scallop.exceptions.UnknownOption
import org.scalatest.FlatSpec

class TacklerCliArgsTest extends FlatSpec {

  behavior of "Tackler with cli-args"

  it should "use default config" in {
    /**
     * This is implicit way to test the use of default config.
     * Default config is:
     *  - basedir=../
     *  - input.txn.dir=txns/
     *
     * Let's assert that these path components are used to build basedir,
     * which obviously won't be found under cli/target/scala-2.12
     * so we intercept and inspect NoSuchFileException
     */
    val ex = intercept[NoSuchFileException] {
      TacklerCli.runExceptions(Array[String]())
    }
    assert(ex.getMessage.endsWith("cli/target/scala-2.12/txns"), ex.getMessage)
  }

  it should "support --help" in {
    assertThrows[org.rogach.scallop.exceptions.Help] {
      TacklerCli.runExceptions(Array[String]("--help"))
    }
  }

  it should "support --version" in {
    val lifeIsGood = try {
      TacklerCli.runExceptions(Array[String]("--version"))
      false
      } catch  {
        case org.rogach.scallop.exceptions.Version =>
          true
        case _: Exception =>
          false
    }
    assert(lifeIsGood)
  }

  it should "check return result of --help" in {
    assertResult(TacklerCli.FAILURE) {
      TacklerCli.runReturnValue(Array[String]("--help"))
    }
  }

  it should "check return result of --version" in {
    assertResult(TacklerCli.FAILURE) {
      TacklerCli.runReturnValue(Array[String]("--version"))
    }
  }

  it should "reject unknown args" in {
    assertThrows[UnknownOption] {
      TacklerCli.runExceptions(Array[String]("--not-an-argument"))
    }
  }
}
