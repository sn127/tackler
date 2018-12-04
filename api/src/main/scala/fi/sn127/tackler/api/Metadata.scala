/*
 * Copyright 2016-2018 SN127.fi
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
package fi.sn127.tackler.api

import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

sealed trait MetadataItem {
  def text(): String
}
object MetadataItem {
  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val decodeBalanceItem: Decoder[MetadataItem] = deriveDecoder[MetadataItem]

  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val encodeBalanceItem: Encoder[MetadataItem] = deriveEncoder[MetadataItem]
}

final case class Metadata(metadataItems: Seq[MetadataItem]) {

  def text(): String = {
    metadataItems.map(_.text()).mkString("\n")
  }
}
object Metadata {
  implicit val decodeMetadata: Decoder[Metadata] = deriveDecoder[Metadata]
  implicit val encodeMetadata: Encoder[Metadata] = deriveEncoder[Metadata]
}

/**
 * Metadata of used Git commit.
 *
 * @param ref if this transaction set was defined by git-ref.
 * @param commit this commitid (sha1) of used git tree
 * @param message commit's short message (one-line format)
 */
final case class GitInputReference(commit: String, ref: Option[String], message: String)
  extends MetadataItem {

  override def text(): String = {
    "" +
      "Git storage:\n" +
      "   commit:  " + commit + "\n" +
      "   ref:     " + ref.getOrElse("FIXED by commit") + "\n" +
      "   message: " + message + "\n"
  }
}

final case class TxnFilterDefinition(txnFilterRoot: TxnFilterRoot) extends MetadataItem {
  override def text(): String = txnFilterRoot.text("")
}