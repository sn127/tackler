package fi.sn127.tackler.report

import fi.sn127.tackler.core.Settings

trait ReportConfiguration {
  val minScale: Int
  val maxScale: Int
}

class ReportSettings(setttings: Settings)
  extends ReportConfiguration {

  override val minScale: Int = setttings.Reporting.minScale
  override val maxScale: Int = setttings.Reporting.maxScale
}
