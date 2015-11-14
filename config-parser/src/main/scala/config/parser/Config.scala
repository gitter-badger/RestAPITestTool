package config.parser

import java.io.File

case class Config(active: Boolean = true,
                  patch: Option[File] = None,
                  description: String = "",
                  block: Seq[Block] = Seq())

trait Block

trait Method extends Block {
  var url = ""
}

trait Header {
  var headers = Seq[(String, String)]()
}

trait Body {
  var multipart = false
  var params = Seq[(String, String)]()
  var files = Seq[(String, File, Option[String])]()
}

trait Response {
  var code = -1
  var file: Option[File] = None
  var response_headers = Seq[(String, String)]()
}

trait WithoutBody extends Method with Header with Response
trait WithBody extends WithoutBody with Body

trait DumpBlock extends Block{
  var file: Option[File] = None
  var sql: Seq[String] = Seq()
}

trait RoleBlock extends Block{
  var credentials: Option[File] = None
  var block: Seq[Block] = Seq()
}

case class GET() extends WithoutBody
case class POST() extends WithBody
case class PUT() extends WithBody
case class PATCH() extends WithBody
case class DELETE() extends WithoutBody
case class DUMP() extends DumpBlock
case class ROLE() extends RoleBlock