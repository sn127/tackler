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
package fi.sn127.tackler.core

import java.nio.file.Paths

import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Inside, Matchers}

class SettingsTest extends FlatSpec  with Matchers with Inside {

  behavior of "Settings"

  val respath = "core/target/scala-2.12/test-classes/"

  it should "combine cfg-path with relative basedir" in {
    val cfg = new Settings(Paths.get(respath + "cfg-as-ext-file-rel.conf"), ConfigFactory.empty())
    assert(cfg.basedir.endsWith(respath + "cfg/as/ext/file") === true, cfg.basedir)
  }

  it should "not change abs basedir " in {
    val cfg = new Settings(Paths.get(respath + "cfg-as-ext-file-abs.conf"), ConfigFactory.empty())
    assert(cfg.basedir === Paths.get("/basedir/as/abs/path/by/ext/conf"))
  }

  it should "find embedded config" in {
    val cfg = new Settings(Paths.get("./not/found/config/dir/cfg-is-not-there.conf"), ConfigFactory.empty())
    //  basedir will be merged with exe path because test path in conf file is relative
    assert(cfg.basedir.endsWith("not/found/config/dir/this/is/tackler_conf") === true, cfg.basedir)
  }
}
