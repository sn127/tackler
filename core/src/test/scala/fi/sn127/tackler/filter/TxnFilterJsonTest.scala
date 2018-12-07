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

import java.time.ZonedDateTime
import java.util.UUID

import io.circe.syntax._
import io.circe.parser.decode
import org.scalatest.FunSpecLike

import fi.sn127.tackler.api._
import fi.sn127.tackler.core.Settings
import fi.sn127.tackler.model.TxnData
import fi.sn127.tackler.parser.TacklerTxns

class TxnFilterJsonTest extends TxnFilterSpec with FunSpecLike {
  val filterJsonStr =
    """{
      |  "txnFilter" : {
      |    "TxnFilterAND" : {
      |      "txnFilters" : [
      |        {
      |          "TxnFilterAND" : {
      |            "txnFilters" : [
      |              {
      |                "TxnFilterTxnTSBegin" : {
      |                  "begin" : "2018-01-01T10:11:22.345+02:00"
      |                }
      |              },
      |              {
      |                "TxnFilterTxnTSEnd" : {
      |                  "end" : "2018-12-01T14:11:22.678+02:00"
      |                }
      |              },
      |              {
      |                "TxnFilterTxnCode" : {
      |                  "regex" : "txn.code"
      |                }
      |              },
      |              {
      |                "TxnFilterTxnDescription" : {
      |                  "regex" : "txn.desc"
      |                }
      |              },
      |              {
      |                "TxnFilterTxnUUID" : {
      |                  "uuid" : "29c548db-deb7-44bd-a6a2-e5e4258d256a"
      |                }
      |              },
      |              {
      |                "TxnFilterTxnComments" : {
      |                  "regex" : "txn.comments"
      |                }
      |              }
      |            ]
      |          }
      |        },
      |        {
      |          "TxnFilterOR" : {
      |            "txnFilters" : [
      |              {
      |                "TxnFilterPostingAccount" : {
      |                  "regex" : "posting:account"
      |                }
      |              },
      |              {
      |                "TxnFilterPostingAmountEqual" : {
      |                  "regex" : "posting:amount:equal",
      |                  "amount" : 1
      |                }
      |              },
      |              {
      |                "TxnFilterPostingAmountLess" : {
      |                  "regex" : "posting.amount:less",
      |                  "amount" : 2
      |                }
      |              },
      |              {
      |                "TxnFilterPostingAmountGreater" : {
      |                  "regex" : "posting.amount:greater",
      |                  "amount" : 123456789123456789.012345678901234567890123456789
      |                }
      |              },
      |              {
      |                "TxnFilterPostingCommodity" : {
      |                  "regex" : "posting.commodity"
      |                }
      |              },
      |              {
      |                "TxnFilterPostingComment" : {
      |                  "regex" : "posting.comment"
      |                }
      |              }
      |            ]
      |          }
      |        },
      |        {
      |          "TxnFilterNOT" : {
      |            "txnFilter" : {
      |              "TxnFilterTxnDescription" : {
      |                "regex" : "not-me-not"
      |              }
      |            }
      |          }
      |        }
      |      ]
      |    }
      |  }
      |}
      |""".stripMargin

  val filterTextStr =
    """Filter:
      |  AND
      |    AND
      |      Txn TS: begin 2018-01-01T10:11:22.345+02:00
      |      Txn TS: end   2018-12-01T14:11:22.678+02:00
      |      Txn Code: "txn.code"
      |      Txn Description: "txn.desc"
      |      Txn UUID: 29c548db-deb7-44bd-a6a2-e5e4258d256a
      |      Txn Comments: "txn.comments"
      |    OR
      |      Posting Account: "posting:account"
      |      Posting Amount
      |        account: "posting:amount:equal"
      |        amount == 1
      |      Posting Amount
      |        account: "posting.amount:less"
      |        amount < 2
      |      Posting Amount
      |        account: "posting.amount:greater"
      |        amount > 123456789123456789.012345678901234567890123456789
      |      Posting Commodity: "posting.commodity"
      |      Posting Comment: "posting.comment"
      |    NOT
      |      Txn Description: "not-me-not"
      |""".stripMargin

  val tt = new TacklerTxns(Settings())

  val uuidTxn01 = "22e17bf5-3da5-404d-aaff-e3cc668191ee"
  val uuidTxn02 = "a88f4981-ebe7-4287-a59c-d444e3bd579a"
  val uuidTxn03 = "16cf7363-45d2-480c-ac49-c710f4ea5f0d"
  val uuidTxn04 = "205d4a48-471c-4015-856c-1c827f8befdd"
  val uuidTxn05 = "c6e6cb30-858b-4d9f-8b87-3fae2372136a"

  val txnStr =
    s"""2018-01-01 abc txn01
       | ;:uuid: ${uuidTxn01}
       | ; xyz
       | e:b  1.000000001
       | a
       |
       |2018-02-01 (abc) txn02
       | ;:uuid: ${uuidTxn02}
       | e:b  1.0000000011 ; abc hamburger
       | a
       |
       |2018-03-01 (xyz) txn03
       | ;:uuid: ${uuidTxn03}
       | ; xyz
       | ; abc
       | ; klm
       | e:b  2 EUR
       | a
       |
       |2018-04-01
       | ;:uuid: ${uuidTxn04}
       | e:b:abc  3 ; xyz
       | a
       |
       |2018-05-01
       | ;:uuid: ${uuidTxn05}
       | e:abc:foo  4 EUR
       | a
       |
       |""".stripMargin

  val txnsAll = tt.string2Txns(txnStr)

  describe("Decoding JSON to Txn Filter") {
    /**
     * test: 4cce4204-16b1-40a4-b1ea-ce11272d5824
     */
    it("decode from JSON and then encode to text") {
      val txnFilterRoot = decode[TxnFilterDefinition](filterJsonStr)

      assert(filterTextStr === txnFilterRoot.right.get.text(""))
    }

    /**
     * test: 283d64f6-4508-48ac-89a3-e70e25784330
     */
    it("decode working filter from JSON") {
      val filterStr =
        """
          |{
          |  "txnFilter" : {
          |    "TxnFilterAND" : {
          |      "txnFilters" : [
          |        {
          |          "TxnFilterPostingAccount" : {
          |            "regex" : ".*:abc"
          |          }
          |        },
          |        {
          |          "TxnFilterPostingAmountGreater" : {
          |            "regex" : ".*:abc",
          |            "amount" : 2.99
          |          }
          |        }
          |      ]
          |    }
          |  }
          |}
        """.stripMargin

      val txnFilterRoot = decode[TxnFilterDefinition](filterStr)

      val txnData = txnsAll.filter(txnFilterRoot.right.get)

      assert(txnData.txns.size === 1)
      assert(checkUUID(txnData, uuidTxn04))
    }

    /**
     * test: 2671b0ff-8b8d-42c8-95ae-e2dcf4d15ab0
     */
    it("reject JSON AND filter with only one filter") {
      val filterStr =
        """
          |{
          |  "txnFilter" : {
          |    "TxnFilterAND" : {
          |      "txnFilters" : [
          |        {
          |          "TxnFilterPostingAccount" : {
          |            "regex" : ".*:abc"
          |          }
          |        }
          |      ]
          |    }
          |  }
          |}
        """.stripMargin

      assertThrows[IllegalArgumentException] {
        val _ = decode[TxnFilterDefinition](filterStr)
      }
    }

    /**
     * test: 00754b91-91e4-4ace-b4e4-0f43ff599939
     */
    it("reject JSON OR filter with only one filter") {
      val filterStr =
        """
          |{
          |  "txnFilter" : {
          |    "TxnFilterOR" : {
          |      "txnFilters" : [
          |        {
          |          "TxnFilterPostingAccount" : {
          |            "regex" : ".*:abc"
          |          }
          |        }
          |      ]
          |    }
          |  }
          |}
        """.stripMargin

      assertThrows[IllegalArgumentException] {
        val _ = decode[TxnFilterDefinition](filterStr)
      }
    }
  }

  describe("Encode Filter and it's metadata") {
    val txnData = TxnData(None, Seq.empty)

    val txnFilter = TxnFilterDefinition(
      TxnFilterAND(List[TxnFilter](
        TxnFilterAND(List[TxnFilter](
          TxnFilterTxnTSBegin(ZonedDateTime.parse("2018-01-01T10:11:22.345+02:00")),
          TxnFilterTxnTSEnd(ZonedDateTime.parse("2018-12-01T14:11:22.678+02:00")),
          TxnFilterTxnCode("txn.code"),
          TxnFilterTxnDescription("txn.desc"),
          TxnFilterTxnUUID(UUID.fromString("29c548db-deb7-44bd-a6a2-e5e4258d256a")),
          TxnFilterTxnComments("txn.comments"),
        )),
        TxnFilterOR(List[TxnFilter](
          TxnFilterPostingAccount("posting:account"),
          TxnFilterPostingAmountEqual("posting:amount:equal", 1),
          TxnFilterPostingAmountLess("posting.amount:less", 2),
          TxnFilterPostingAmountGreater("posting.amount:greater",
            BigDecimal("123456789123456789.012345678901234567890123456789")),
          TxnFilterPostingCommodity("posting.commodity"),
          TxnFilterPostingComment("posting.comment"),
        )),
        TxnFilterNOT(
          TxnFilterTxnDescription("not-me-not")
        )
      ))
    )

    /**
     * test: 3624a7b3-3668-45ee-9580-aa64fb955a33
     */
    it("encode filter to JSON") {
      assert(filterJsonStr === txnFilter.asJson + "\n")
    }

    /**
     * test: f3213817-fe0c-4bec-b6be-b3396bad8114
     */
    it("encode filter to TEXT") {
      assert(filterTextStr === txnFilter.text(""))
    }

    /**
     * test: 2b56249e-4dff-445f-b30c-427c7c29e8e1
     */
    it("encode metadata as JSON") {
      val metadataJson =
        """{
          |  "metadataItems" : [
          |    {
          |      "TxnFilterMetadata" : {
          |        "txnFilterDef" : {
          |          "txnFilter" : {
          |            "TxnFilterAND" : {
          |              "txnFilters" : [
          |                {
          |                  "TxnFilterAND" : {
          |                    "txnFilters" : [
          |                      {
          |                        "TxnFilterTxnTSBegin" : {
          |                          "begin" : "2018-01-01T10:11:22.345+02:00"
          |                        }
          |                      },
          |                      {
          |                        "TxnFilterTxnTSEnd" : {
          |                          "end" : "2018-12-01T14:11:22.678+02:00"
          |                        }
          |                      },
          |                      {
          |                        "TxnFilterTxnCode" : {
          |                          "regex" : "txn.code"
          |                        }
          |                      },
          |                      {
          |                        "TxnFilterTxnDescription" : {
          |                          "regex" : "txn.desc"
          |                        }
          |                      },
          |                      {
          |                        "TxnFilterTxnUUID" : {
          |                          "uuid" : "29c548db-deb7-44bd-a6a2-e5e4258d256a"
          |                        }
          |                      },
          |                      {
          |                        "TxnFilterTxnComments" : {
          |                          "regex" : "txn.comments"
          |                        }
          |                      }
          |                    ]
          |                  }
          |                },
          |                {
          |                  "TxnFilterOR" : {
          |                    "txnFilters" : [
          |                      {
          |                        "TxnFilterPostingAccount" : {
          |                          "regex" : "posting:account"
          |                        }
          |                      },
          |                      {
          |                        "TxnFilterPostingAmountEqual" : {
          |                          "regex" : "posting:amount:equal",
          |                          "amount" : 1
          |                        }
          |                      },
          |                      {
          |                        "TxnFilterPostingAmountLess" : {
          |                          "regex" : "posting.amount:less",
          |                          "amount" : 2
          |                        }
          |                      },
          |                      {
          |                        "TxnFilterPostingAmountGreater" : {
          |                          "regex" : "posting.amount:greater",
          |                          "amount" : 123456789123456789.012345678901234567890123456789
          |                        }
          |                      },
          |                      {
          |                        "TxnFilterPostingCommodity" : {
          |                          "regex" : "posting.commodity"
          |                        }
          |                      },
          |                      {
          |                        "TxnFilterPostingComment" : {
          |                          "regex" : "posting.comment"
          |                        }
          |                      }
          |                    ]
          |                  }
          |                },
          |                {
          |                  "TxnFilterNOT" : {
          |                    "txnFilter" : {
          |                      "TxnFilterTxnDescription" : {
          |                        "regex" : "not-me-not"
          |                      }
          |                    }
          |                  }
          |                }
          |              ]
          |            }
          |          }
          |        }
          |      }
          |    }
          |  ]
          |}
          |""".stripMargin

      assert(metadataJson === txnData.filter(txnFilter).metadata.map(m => m.asJson).getOrElse("") + "\n")
    }

    /**
     * test: f26027f1-b9d5-4f87-a173-9ffac1b1b862
     */
    it("encode metadata as TEXT") {
      assert(filterTextStr === txnData.filter(txnFilter).metadata.map(m => m.text()).getOrElse(""))
    }
  }

}
