/*
 * Copyright 2017 SN127.fi
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

sealed trait StorageType
sealed case class GitStorageType() extends StorageType
sealed case class FilesystemStorageType() extends StorageType

object StorageType {
  def apply(storageType: String): StorageType = {
    storageType match {
      case "git" => GitStorageType()
      case "fs" => FilesystemStorageType()
      /* Error*/
      case storage => throw new TacklerException(
        "Unknown storage type [" + storage + "]. Valid types are: git, fs")
    }
  }
}
