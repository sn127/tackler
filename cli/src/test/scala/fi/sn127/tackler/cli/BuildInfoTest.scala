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

import org.scalatest.FlatSpec

class BuildInfoTest extends FlatSpec {

  behavior of "BuildInfo"

  it should "builtAtString" in {
    assert(BuildInfo.builtAtString.startsWith("2018"))
  }

  it should "scalaVersion" in {
    assert(BuildInfo.scalaVersion.startsWith("2.12."))
  }

  it should "name" in {
    assert(BuildInfo.name === "cli")
  }

  it should "sbtVersion" in {
    assert(BuildInfo.sbtVersion.startsWith("1.1.0"))
  }

  it should "builtAtMillis" in {
    assert(BuildInfo.builtAtMillis > 1492008293122L)
  }

  it should "toString" in {
    assert(BuildInfo.toString.nonEmpty)
  }

  it should "version" in {
    assert(BuildInfo.version.nonEmpty)
  }

}
