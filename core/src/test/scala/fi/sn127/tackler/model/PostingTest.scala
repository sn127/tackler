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

import org.scalatest.FlatSpec

import fi.sn127.tackler.core.TxnException

class PostingTest extends FlatSpec {

  val acctn = AccountTreeNode("a:b")

  behavior of "Posting"

  /**
   * test: 42ad9d32-64aa-4fcd-a4ab-1e8521b921e3
   */
  it should "not accept zero postings" in {
    assertThrows[TxnException]{
      Posting(acctn, BigDecimal(0), None)
    }
    assertThrows[TxnException]{
      // check that difference precision doesn't mess up
      // bigdecimal comparisions
      Posting(acctn, BigDecimal(0.00), None)
    }
  }

  /**
   * test: e3c97b66-318c-4396-8857-0cd2c1dfb0d2
   */
  it should "preserve precision" in {
   val v =
      //          3         2         1                   1         2         3         4
      BigDecimal("123456789012345678901234567890.123456789012345678901234567890123456789012")
    val p = Posting(acctn, v, None)

    assert(p.toString === "a:b   123456789012345678901234567890.123456789012345678901234567890123456789012")
  }

  /**
   * test: 6ce68af4-5349-44e0-8fbc-35bebd8ac1ac
   */
  it should "toString" in {
    val v = BigDecimal("123.01")
    val p = Posting(acctn, v, Some("comment"))

    assert(p.toString === "a:b   123.01 ; comment")
  }
}