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

import java.nio.file.Paths

import better.files.File
import org.scalatest.{FlatSpec, Inside, Matchers}

import fi.sn127.tackler.core.Settings

/**
 * Tests to validate interaction between and combination of
 * CLI args and Settings (conf-file).
 */
class TacklerCliSettingsTest extends FlatSpec  with Matchers with Inside {
  val respath = "core/target/scala-2.12/test-classes/"

  it should "respect *ALL* cli args (input.txn)" in {
    val absBasepath = File(respath)
    val args = Array(
      "--basedir", absBasepath.toString, // this must be real path so that --input.fs.dir works correctly with rel-path
      "--input.fs.dir", "cli-args-txns/",
      "--input.fs.glob", "**/cliargs*.txn"
    )
    val cliCfg = new TacklerCliArgs(args)

    val settings = Settings(Paths.get("/not/there/cfg.conf"), cliCfg.toConfig)

    assert(settings.basedir === absBasepath.path)
    assert(settings.input_fs_dir ===  (absBasepath / "cli-args-txns").path)
    assert(settings.input_fs_glob.toString === "**/cliargs*.txn")
  }

  it should "respect *ALL* cli args (input.file)" in {
    val absBasepath = File(respath)
    val args = Array(
      "--basedir", absBasepath.toString, // this must be real path so that --input.fs.dir works correctly with rel-path
      "--input.file", (absBasepath / "filename.txn").toString,
      "--accounts.strict", "false"
    )
    val cliCfg = new TacklerCliArgs(args)

    val settings = Settings(Paths.get("/not/there/cfg.conf"), cliCfg.toConfig)

    assert(settings.basedir === absBasepath.path)
    assert(settings.getPathWithSettings(cliCfg.input_filename.getOrElse("")) ===
      (absBasepath / "filename.txn").path)
    assert(settings.accounts_strict === false)
  }


  it should "merge configs (cli, ext-conf, embedded conf)" in {
    val args = Array(
      "--input.fs.glob", "**/cli-args*.txn"
    )
    val cliCfg = new TacklerCliArgs(args)

    val settings = Settings(Paths.get(respath + "cfg-as-ext-file-rel.conf"), cliCfg.toConfig)

    // this is coming from cfg-as-ext-file.conf
    assert(settings.basedir.toString.endsWith(File(respath + "cfg/as/ext/file").toString) === true, settings.basedir)
    
    // this is coming from cli-args
    assert(settings.input_fs_glob.toString === "**/cli-args*.txn")
  }

  ignore should "handle relative --basedir from cmd line" in {
    // either handle relative basedir from cmd line,
    // or enforce that basedir is absolute by CliArgs
  }
}
