package imagination

import java.io.File
import java.util.regex.Pattern

import _root_.config.parser.ConfigCollector
import com.typesafe.config
import com.typesafe.config.ConfigFactory
import scopt.OptionParser

import scala.util.{Failure, Success, Try}

object Main {

  def main(args: Array[String]) {
    val parser = new OptionParser[ConfigFileLocation]("RestAPI testing tool") {
      help("help")

      opt[File]('c', "config") required() action { (x, c) =>
        c.copy(file = x)
      } text "config file path is required"
    }

    parser.parse(args, ConfigFileLocation()) match {
      case Some(config) =>
        buildConfig(config.file) match {
          case Success(c) =>
            val rests = ConfigCollector(c.dir, c.pattern).getConfigs

            rests.foreach {
              case Left(th) => println("Error")
              case Right(cfg) => println(cfg.description)
            }
            println("Exit")

          case Failure(e) =>
            println(e.getMessage)
            System.exit(1)
        }
      case None =>
    }
  }

  def getDir(s: String, create: Boolean = false) = {
    val f = new File(s)
    if (create) f.mkdirs()
    if (!f.exists()) throw new RuntimeException(s"Unable to create/find folder by path [$f]")
    f
  }

  def buildConfig(file: File): Try[Args] = Try {
    val cfg = ConfigFactory.parseFile(file)

    val dir = getDir(cfg.getString("tests.dir"))

    Args(
      dir,
      getErrorDir(cfg, dir),
      getHost(cfg),
      getPort(cfg),
      getPattern(cfg),
      getDebug(cfg)
    )
  }

  def getPattern(cfg: config.Config): Pattern =
    Pattern.compile(cfg.getString("tests.pathPattern"), Pattern.DOTALL)

  def getDebug(cfg: config.Config): Boolean = Try {
    cfg.getBoolean("debug")
  } match {
    case Success(debug) => debug
    case Failure(e) => false
  }

  def getErrorDir(cfg: config.Config, dir: File): File = Try {
    cfg.getString("tests.errorDir")
  } match {
    case Success(errorDir) => getDir(errorDir, create = true)
    case Failure(e) => new File(dir, "errors")
  }

  def getHost(cfg: config.Config): String = Try {
    cfg.getString("server.host")
  } match {
    case Success(host) => host
    case Failure(e) => "localhost"
  }

  def getPort(cfg: config.Config): Int = Try {
    cfg.getInt("server.port")
  } match {
    case Success(port) => port
    case Failure(e) => 80
  }
}

case class ConfigFileLocation(file: File = new File("."))

case class Args(dir: File,
                errorDir: File,
                host: String,
                port: Int,
                pattern: Pattern,
                debug: Boolean)

// TODO db credentials auth mode