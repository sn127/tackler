/*
 * Copyright 2017 Jani Averbach
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

import org.scalatest.FlatSpec

class StorageTypeTest extends FlatSpec {

  behavior of "StorageType"

  it should "apply fs" in {
    val t = StorageType("fs")
    assert(t.isInstanceOf[FilesystemStorageType])
  }

  it should "apply git" in {
    val t = StorageType("git")
    assert(t.isInstanceOf[GitStorageType])
  }

  /**
   * test:uuid: 195971d7-f16f-4c1c-a761-6764b28fd4db
   */
  it should "handle unknown type" in {
    assertThrows[TacklerException]{
      StorageType("no-such-type")
    }
  }
}
