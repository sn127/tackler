/*
 * Copyright 2018 sn127.fi
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
package fi.sn127.tackler.filter

import java.time.{ZoneId, ZonedDateTime}

import fi.sn127.tackler.core.Settings
import fi.sn127.tackler.parser.TacklerTxns

class TxnFilterTxnTSBeginTest extends TxnFilterSpec {

  val tt = new TacklerTxns(Settings())

  val uuidTSDate01 = "22e17bf5-3da5-404d-aaff-e3cc668191ee"
  val uuidTSDate02 = "a88f4981-ebe7-4287-a59c-d444e3bd579a"
  val uuidTSDate03 = "16cf7363-45d2-480c-ac49-c710f4ea5f0d"
  val uuidTSDate04 = "205d4a48-471c-4015-856c-1c827f8befdd"

  val txnStrTSDate =
    s"""2018-01-01 txn01
      | ;:uuid: ${uuidTSDate01}
      | e  1
      | a
      |
      |2018-02-01 txn02
      | ;:uuid: ${uuidTSDate02}
      | e  1
      | a
      |
      |2018-03-01 txn03
      | ;:uuid: ${uuidTSDate03}
      | e  1
      | a
      |
      |2018-04-01 txn04
      | ;:uuid: ${uuidTSDate04}
      | e  1
      | a
      |
      |""".stripMargin

  val txnsTSDate = tt.string2Txns(txnStrTSDate)

  val uuidTSTime01 = "ec1b0e67-f990-4761-9667-239a5a6cd46b"
  val uuidTSTime02 = "080ee755-04cf-44d8-94db-e89ca7b9cd13"
  val uuidTSTime03 = "03b13353-ee04-4faa-b139-d5b097f6b9b5"

  val txnStrTSTime =
    s"""2018-01-01T11:00:00
       | ;:uuid: ${uuidTSTime01}
       | e  1
       | a
       |
       |2018-01-01T23:00:00
       | ;:uuid: ${uuidTSTime02}
       | e  1
       | a
       |
       |2018-01-02T00:00:00
       | ;:uuid: ${uuidTSTime03}
       | e  1
       | a
       |
       |""".stripMargin

  val txnsTSTime = tt.string2Txns(txnStrTSTime)


  val uuidTSNano01 = "faeda0cf-2db2-4d91-9d85-8eb31c4cf598"
  val uuidTSNano02 = "7202f635-61d1-41a5-9b2b-d5a9e2ab6f9b"
  val uuidTSNano03 = "520345ad-b06f-4717-b2aa-5f79daf5c44d"

  val txnStrTSNano =
    s"""2018-01-01T14:00:00.123456787
       | ;:uuid: ${uuidTSNano01}
       | e  1
       | a
       |
       |2018-01-01T14:00:00.123456788
       | ;:uuid: ${uuidTSNano02}
       | e  1
       | a
       |
       |2018-01-01T14:00:00.123456789
       | ;:uuid: ${uuidTSNano03}
       | e  1
       | a
       |
       |""".stripMargin

  val txnsTSTNano = tt.string2Txns(txnStrTSNano)

  val uuidTSZone01 = "e06360fd-526e-4725-8d9f-d4aae8f84ad1"
  val uuidTSZone02 = "605fb2eb-def5-41dd-b885-950602685bb4"
  val uuidTSZone03 = "55455800-1aad-4ef4-9795-235eb082f4ea"

  val txnStrTSZone =
    s"""2018-01-04T09:00:00+10:00
       | ;:uuid: ${uuidTSZone01}
       | e  1
       | a
       |
       |2018-01-03T18:00:00-06:00
       | ;:uuid: ${uuidTSZone02}
       | ; Zone support must be tested with offsets which cancel each others (e.g. result is same time in UTC)
       | e  1
       | a
       |
       |2018-01-04T00:00:00
       | ;:uuid: ${uuidTSZone03}
       | e  1
       | a
       |
       |""".stripMargin

  val txnsTSZone = tt.string2Txns(txnStrTSZone)

  behavior of "Timestamp filter: Begin"

  /**
   * test: 701b2c27-d33c-4460-9a5e-64316c6ed946
   */
  it must "filter by date" in {
    val txnTSBeginFilter = TxnFilterTxnTSBegin(ZonedDateTime.of(
      2018, 3, 1,
      0, 0, 0, 0,
      ZoneId.of("UTC")))

    val txnData = txnsTSDate.filter(TxnFilterRoot(txnTSBeginFilter))

    assert(txnData.txns.size === 2)
    assert(checkUUID(txnData, uuidTSDate03))
    assert(checkUUID(txnData, uuidTSDate04))
  }

  /**
   * test: ec7cf2bd-e10e-4f46-9baa-4096881a5fbb
   */
  it must "filter by time" in {
    val txnTSBeginFilter = TxnFilterTxnTSBegin(ZonedDateTime.of(
      2018, 1, 1,
      23, 0, 0, 0,
      ZoneId.of("UTC")))

    val txnData = txnsTSTime.filter(TxnFilterRoot(txnTSBeginFilter))

    assert(txnData.txns.size === 2)
    assert(checkUUID(txnData, uuidTSTime02))
    assert(checkUUID(txnData, uuidTSTime03))
  }

  /**
   * test: f1623bd0-f767-458e-bc68-6eadfa113fd1
   */
  it must "filter by nanoseconds" in {
    val txnTSBeginFilter = TxnFilterTxnTSBegin(ZonedDateTime.of(
      2018, 1, 1,
      14, 0, 0, 123456788,
      ZoneId.of("UTC")))

    val txnData = txnsTSTNano.filter(TxnFilterRoot(txnTSBeginFilter))

    assert(txnData.txns.size === 2)
    assert(checkUUID(txnData, uuidTSNano02))
    assert(checkUUID(txnData, uuidTSNano03))
  }

  /**
   * test: 960cb7e7-b180-4276-a43b-714e53e1789b
   */
  it must "filter by timezone" in {
    val txnTSBeginFilter = TxnFilterTxnTSBegin(ZonedDateTime.of(
      2018, 1, 4,
      0, 0, 0, 0,
      ZoneId.of("UTC")))

    val txnData = txnsTSZone.filter(TxnFilterRoot(txnTSBeginFilter))

    assert(txnData.txns.size === 2)
    assert(checkUUID(txnData, uuidTSZone02))
    assert(checkUUID(txnData, uuidTSZone03))
  }
}
