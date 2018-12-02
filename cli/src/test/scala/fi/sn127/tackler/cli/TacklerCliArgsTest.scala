/*
 * Copyright 2016-2018 sn127.fi
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

import org.rogach.scallop.exceptions.{UnknownOption, ValidationFailure}
import org.scalatest.FlatSpec

class TacklerCliArgsTest extends FlatSpec {

  behavior of "Tackler with cli-args"

  it should "use default config" in {
    /**
     * This is an implicit way to test the use of default config.
     * Default config is:
     *  - basedir=./
     *  - input.fs.dir=txns
     *
     * Let's assert that these path components are used to build basedir,
     * which obviously won't be found under cli/target/scala-2.12
     * so we intercept and inspect NoSuchFileException
     */
    val ex = intercept[NoSuchFileException] {
      TacklerCli.runExceptions(Array[String]())
    }
    assert(ex.getMessage.endsWith("/txns"), ex.getMessage)
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

  /**
   * test:uuid: a2ca374a-1323-413b-aaff-64bc3c8d4d30
   */
  it should "git: cli err: ref and commit" in {
    assertThrows[ValidationFailure] {
      TacklerCli.runExceptions(
        Array[String]("--input.git.ref", "ref", "--input.git.commit", "id"))
    }
  }

  /**
   * test:uuid: 1822f1b2-f749-4f63-be44-fa29c58c4fe2
   */
  it should "git: cli err: input.file + git.ref" in {
    assertThrows[ValidationFailure] {
      TacklerCli.runExceptions(
        Array[String]("--input.file", "filename", "--input.git.ref", "ref"))
    }
  }

  /**
   * test:uuid: 97bf542e-55b5-437f-9878-7f436f50c428
   */
  it should "git: cli err: input.file + git.commit" in {
    assertThrows[ValidationFailure] {
      TacklerCli.runExceptions(
        Array[String]("--input.file", "filename", "--input.git.commit", "id"))
    }
  }


  /**
   * test:uuid: 3eba26fe-821d-4d36-94cb-09427b1c004f
   */
  it should "git: cli err: fs.dir + git.ref" in {
    assertThrows[ValidationFailure] {
      TacklerCli.runExceptions(
        Array[String]("--input.fs.dir", "txns", "--input.git.ref", "ref"))
    }
  }

  /**
   * test:uuid: 400bd1e9-6f7a-4e0c-913c-45401ee73181
   */
  it should "git: cli err: fs.dir + git.commit" in {
    assertThrows[ValidationFailure] {
      TacklerCli.runExceptions(
        Array[String]("--input.fs.dir", "txns", "--input.git.commit", "id"))
    }
  }

  /**
   * test:uuid: 7d4984c7-633f-4403-a2b7-5ea0cd4f07e8
   */
  it should "git: cli err: fs.glob + git.ref" in {
    assertThrows[ValidationFailure] {
      TacklerCli.runExceptions(
        Array[String]("--input.fs.glob", "glob", "--input.git.ref", "ref"))
    }
  }

  /**
   * test:uuid: 6ec6431e-a443-4633-8f26-df3218a8657c
   */
  it should "git: cli err: fs.glob + git.commit" in {
    assertThrows[ValidationFailure] {
      TacklerCli.runExceptions(
        Array[String]("--input.fs.glob", "glob", "--input.git.commit", "id"))
    }
  }

}
