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
package fi.sn127.tackler.model

import io.circe._
import io.circe.syntax._

abstract trait Metadata {
  def text(): String
  def asJson(): Json
}

object Metadata {

  /**
   * Combine optional metadata with original JSON (report)
   * @param json original (report) data as json
   * @param metadata optional metadata to be combined
   * @return json with combined metadata (as json)
   */
  def combine(json: Json, metadata: Option[Metadata]): Json = {
    metadata
      .fold(
        json
      )({ md =>
        json.deepMerge(
          Json.obj(("metadata", md.asJson()))
        )
      })
  }
}
/**
 * Metadata from Git storage.
 *
 * @param ref if this transaction set was found by ref.
 * @param commit sha1 of used tree
 * @param shortMessage commit's short message (one-line format)
 */
class GitMetadata(val ref: String, val commit: String, val shortMessage: String)
  extends Metadata {

  override def text(): String = {
    "" +
      "Git storage:\n" +
      "   commit:  " + commit + "\n" +
      "   ref:     " + ref + "\n" +
      "   message: " + shortMessage + "\n"
  }

  override def asJson(): Json = {
    Json.obj(("git",
      Json.obj(
        ("commit", commit.asJson),
        ("ref", ref.asJson),
        ("message", shortMessage.asJson))))
  }
}