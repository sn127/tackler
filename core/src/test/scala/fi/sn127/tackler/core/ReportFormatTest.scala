package fi.sn127.tackler.core

import org.scalatest.FlatSpec

class ReportFormatTest extends FlatSpec {

  behavior of "ReportFormatTest"

  it should "apply" in {
    assertThrows[ReportException]{
      ReportFormat("no-such-format")
    }
  }
}
