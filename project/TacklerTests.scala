import better.files._
import com.typesafe.config.ConfigFactory
import sbt._

object TacklerTests {

  def setup(tests: String, log: Logger) = {
    val testsDir = File(tests)
    val autoCleanConfFile = testsDir / "dirsuite.conf"

    if (autoCleanConfFile.exists) {
      val cfg = ConfigFactory.parseFile(autoCleanConfFile.toJava)
      val autoClean: Boolean = cfg.getBoolean("auto-clean")
      val outGlob: String = cfg.getString("out-glob")

      if (autoClean) {
        val outFiles = testsDir.glob(outGlob)
          .filter(f => f.isRegularFile)
          .toSeq

        outFiles.foreach { output =>
          output.delete(true)
        }
        log.info("DirSuite clean-up: Removed " + outFiles.size + " files")
      } else {
        log.info("DirSuite clean-up: Disabled")
      }
    } else {
      log.warn("DirSuite: Missing configuration file: " + autoCleanConfFile)
    }
  }
}
