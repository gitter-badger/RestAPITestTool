package config.parser

import java.io.File
import java.util.regex.Pattern

case class ConfigCollector(dir: File, pattern: Pattern) {
  require(dir.exists(), "Directory for scan not found")
  require(dir.isDirectory, s"Path $dir should be directory for scan")

  def matchesPathPattern(pattern: Pattern, cfg: File): Boolean = {
    val pathFromRoot = cfg.getPath.replace(dir.getPath, "")
    pattern.matcher(pathFromRoot).matches()
  }

  def collect(): Seq[File] = {
    def collectConfigs(root: File): Stream[File] = {
      root #:: (if (root.isDirectory) root.listFiles().toStream.flatMap(collectConfigs) else Stream.empty)
    }

    collectConfigs(dir).filter(e => e.isFile && e.getName == "config.xml" && matchesPathPattern(pattern, e))
  }

  def getConfigs: Seq[Either[Throwable, Config]] = {
    collect().map(e => execute(Parser.parse(e, dir)))
  }

  def execute[T](f: => T): Either[Throwable, T] = try Right(f) catch {
    case e: Throwable => Left(e)
  }
}
