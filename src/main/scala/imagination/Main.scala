package imagination

import java.io.File
import java.util.regex.Pattern

import _root_.config.parser.{Config, ConfigCollector}
import com.typesafe.config
import com.typesafe.config.ConfigFactory
import scopt.OptionParser

object Main {

  def main(args: Array[String]) {
    val parser = new OptionParser[ConfigFileLocation]("REST API TESTING TOOL") {
      help("help")

      opt[File]('c', "config") required() action { (x, c) =>
        c.copy(file = x)
      } text "config file path is required"

      checkConfig { e =>
        if (e.file.exists())
          success
        else
          failure(s"File not found by path ${e.file.getAbsolutePath}")
      }
    }

    parser.parse(args, ConfigFileLocation()) match {
      case Some(config) =>
        withCriticalState {
          val c = buildConfig(config.file)
          val rests = ConfigCollector(c.dir, c.pattern).getConfigs

          val failedCount = rests.count(_._2.isLeft)
          if (failedCount > 0) {
            println(s"# Failed Tests Count: " + failedCount)
            println()

            for (each <- rests if each._2.isLeft; ((f, Left(t))) = each) {
              println(s"# File  : " + f.getAbsolutePath)
              println(s"# Failed: " + t.getLocalizedMessage)
            }

            if (c.stopIfHasBrokenTests) throw new RuntimeException("Broken tests was found")
          }

          for (each <- rests
               if each._2.isRight; ((f, Right(t))) = each)
            executeTest(f, t)
        }
      case None =>
    }

    def executeTest(file: File, config: Config) = {

    }
  }

  def withCriticalState[T](f: => T) {
    try f catch {
      case e: Throwable =>
        println(e.getMessage)
        System.exit(1)
    }
  }

  def getDir(s: String, create: Boolean = false) = {
    val f = new File(s)
    if (create) f.mkdirs()
    if (!f.exists()) throw new RuntimeException(s"Unable to create/find folder by path [$f]")
    f
  }

  def buildConfig(file: File): Args = {
    val r = ArgsReader(ConfigFactory.parseFile(file))

    Args(
      r.dir,
      r.errorDir,
      r.host,
      r.port,
      r.pattern,
      r.debug,
      r.stopIfHasBrokenTests,
      r.stopOnComparisonError
    )
  }

  case class ArgsReader(cfg: config.Config) {
    val dir = getDir(cfg.getString("tests.dir"))

    def pattern = Pattern.compile(cfg.getString("tests.pathPattern"), Pattern.DOTALL)
    def debug = get(cfg.getBoolean("debug"), false)
    def errorDir = get(getDir(cfg.getString("tests.errorDir"), create = true), new File(dir, "errors"))
    def host = get(cfg.getString("server.host"), "localhost")
    def port = get(cfg.getInt("server.port"), 80)
    def stopIfHasBrokenTests = get(cfg.getBoolean("tests.stopIfHasBrokenTests"), false)
    def stopOnComparisonError = get(cfg.getBoolean("tests.stopOnComparisonError"), true)

    def get[T](f: => T, _default: T): T = try f catch {
      case e: Throwable => _default
    }
  }
}

case class ConfigFileLocation(file: File = new File("."))

case class Args(dir: File,
                errorDir: File,
                host: String,
                port: Int,
                pattern: Pattern,
                debug: Boolean,
                stopIfHasBrokenTests: Boolean,
                stopOnComparisonError: Boolean)

// TODO db credentials auth mode