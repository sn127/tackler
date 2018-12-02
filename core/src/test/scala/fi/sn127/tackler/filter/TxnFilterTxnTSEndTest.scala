/*
 * Copyright 2018 SN127.fi
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

class TxnFilterTxnTSEndTest extends TxnFilterSpec {

  val tt = new TacklerTxns(Settings())

  val uuidTSDate01 = "22e17bf5-3da5-404d-aaff-e3cc668191ee"
  val uuidTSDate02 = "a88f4981-ebe7-4287-a59c-d444e3bd579a"
  val uuidTSDate03 = "16cf7363-45d2-480c-ac49-c710f4ea5f0d"

  val txnStrTSDate =
    s"""2018-01-01
       | ;:uuid: ${uuidTSDate01}
       | e  1
       | a
       |
       |2018-02-01
       | ;:uuid: ${uuidTSDate02}
       | e  1
       | a
       |
       |2018-03-01
       | ;:uuid: ${uuidTSDate03}
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

  behavior of "Timestamp filter: End"
  /**
   * test: 42a42f07-dea5-45ee-b563-187f9121e1e1
   */
  it must "filter by date" in {
    val txnFilter = TxnFilterTxnTSEnd(
      ZonedDateTime.of(
        2018, 2, 1,
        0, 0, 0, 0,
        ZoneId.of("UTC")))

    val txnData = txnsTSDate.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTSDate01))
  }

  /**
   * test: 4e566d2b-da32-4336-9b7f-d7c4a59658d2
   */
  it must "filter by time" in {
    val txnFilter = TxnFilterTxnTSEnd(
      ZonedDateTime.of(
        2018, 1, 1,
        23, 0, 0, 0,
        ZoneId.of("UTC")))

    val txnData = txnsTSTime.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTSTime01))
  }

  /**
   * test: f6081a60-92a9-4051-85d7-c993e3cc03be
   */
  it must "filter by nanoseconds" in {
    val txnFilter = TxnFilterTxnTSEnd(
      ZonedDateTime.of(
        2018, 1, 1,
        14, 0, 0, 123456788,
        ZoneId.of("UTC")))

    val txnData = txnsTSTNano.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTSNano01))
  }

  /**
   * test: ab53df34-d22a-4256-9c4d-6d1ccf0ef32e
   */
  it must "filter by timezone" in {
    val txnFilter = TxnFilterTxnTSEnd(
      ZonedDateTime.of(
        2018, 1, 4,
        0, 0, 0, 0,
        ZoneId.of("UTC")))

    val txnData = txnsTSZone.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTSZone01))
  }
}
