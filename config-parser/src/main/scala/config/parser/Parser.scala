package config.parser

import java.io.{FilenameFilter, BufferedInputStream, File, FileInputStream}
import javax.xml.stream.XMLStreamConstants.{END_ELEMENT, START_ELEMENT}
import javax.xml.stream.{XMLInputFactory, XMLStreamReader}

object Parser {

  case class FileSearcher(configFile: File, root: File) {
    
    def find(dirtyName: String): File = {
      val needed = if (dirtyName.lift(0) == Some('/')) dirtyName.tail else dirtyName

      def tryFindRecursiveUp(): Option[File] = {
        val filter = new FilenameFilter {
          def accept(dir: File, name: String) = name == needed
        }

        def tryFind(dir: File): Option[File] = {
          if (dir == root) None
          else {
            val maybeFile = dir.listFiles(filter).headOption
            if (maybeFile.isDefined) maybeFile else tryFind(dir.getParentFile)
          }
        }

        tryFind(configFile.getParentFile)
      }

      def tryFindByFullPath(): Option[File] = {
        val file = new File(root, needed)
        if (file.exists() && file.isFile) Some(file) else None
      }
      
      tryFindRecursiveUp().orElse(tryFindByFullPath()).getOrElse {
        throw new FileLinkingError(configFile,root,needed)
      }
    }
  }

  def parse(file: File, root: File): Config = {
    require(root.exists(), "Root directory not exists")
    Validator.validate(file)

    val r = XMLInputFactory.newInstance().createXMLStreamReader(new BufferedInputStream(new FileInputStream(file)))
    try {
      val parser = InternalParser(r, FileSearcher(file,root))
      parser.parse()
      parser.cfg
    } finally r.close()
  }

  case class InternalParser(r: XMLStreamReader,fs:FileSearcher) {

    implicit class StringToFileConverter(file: String) {
      def asFile(): File = fs.find(file.trim)
    }

    var cfg = Config()

    def parseRequestHeader(h: Header): Unit =
      h.headers = h.headers :+(r.getAttributeValue(0), r.getAttributeValue(1))

    def parseResponseHeader(h: Response): Unit =
      h.response_headers = h.response_headers :+(r.getAttributeValue(0), r.getAttributeValue(1))

    def parseParam(h: Body): Unit =
      h.params = h.params :+(r.getAttributeValue(0), r.getAttributeValue(1))

    def parseFile(h: Body): Unit = {
      h.files = h.files :+(r.getAttributeValue(0), r.getAttributeValue(1).asFile(), Option(r.getAttributeValue(2)))
      h.multipart = true
    }

    def parseBody(h: Body) = parseBlock("body", {
      case "param" => parseParam(h)
      case "file" => parseFile(h)
    })

    def parseResponse(h: Response): Unit = {
      parseAttributes {
        case ("code", v) => h.code = v.toInt
        case ("file", v) => h.file = Some(v.trim.asFile())
      }
      parseBlock("response", {
        case "header" => parseResponseHeader(h)
      })
    }

    def parseMethod[E <: Method with Header with Response](e: E, name: String): Block = {
      parseAttributes {
        case ("url", v) => e.url = v.trim
        case ("multipart", v) => e.asInstanceOf[Body].multipart = v.toBoolean
      }
      parseBlock(name, {
        case "header" => parseRequestHeader(e)
        case "body" => parseBody(e.asInstanceOf[Body])
        case "response" => parseResponse(e)
      })
      e
    }

    def parseDump(): Block = {
      val e = DUMP()
      parseAttributes {
        case ("file", v) => e.file = Some(v.asFile())
      }
      parseBlock("dump", {
        case "sql" => e.sql = e.sql :+ r.getElementText
      })
      e
    }

    def parseRole(): Block = {
      val e = ROLE()
      parseAttributes {
        case ("credentials", v) => e.credentials = Some(v.asFile())
      }

      parseBlock("role", {
        case part@("get" | "post" | "put" | "delete" | "patch" | "dump") =>
          e.block = e.block :+ requests.orElse(dump)(part)
      })
      e
    }

    val requests: PartialFunction[String, Block] = {
      case "get" => parseMethod(GET(), "get")
      case "post" => parseMethod(POST(), "post")
      case "put" => parseMethod(PUT(), "put")
      case "delete" => parseMethod(DELETE(), "delete")
      case "patch" => parseMethod(PATCH(), "patch")
    }
    val dump: PartialFunction[String, Block] = {
      case "dump" => parseDump()
    }
    val role: PartialFunction[String, Block] = {
      case "role" => parseRole()
    }

    def parse(): Unit = {
      parseBlock("config", {
        case "config" =>
          parseAttributes {
            case ("active", v) => cfg = cfg.copy(active = v.toBoolean)
            case ("patch", v) => cfg = cfg.copy(patch = Some(v.trim.asFile()))
          }
        case "description" => cfg = cfg.copy(description = r.getElementText)
        case part@("get" | "post" | "put" | "delete" | "patch" | "dump" | "role") =>
          cfg = cfg.copy(block = cfg.block :+ requests.orElse(dump).orElse(role)(part))
      })
    }

    def parseAttributes(f: ((String, String)) => Unit): Unit = {
      for (idx <- 0 until r.getAttributeCount)
        f((r.getAttributeName(idx).toString, r.getAttributeValue(idx)))
    }

    def parseBlock(name: String, f: PartialFunction[String,Unit]): Unit = {
      var continue = true
      while (r.hasNext && continue) {
        r.next() match {
          case START_ELEMENT => f(r.getName.getLocalPart)
          case END_ELEMENT => if (r.getName.getLocalPart == name) continue = false
          case _ => // ignore
        }
      }
    }
  }

  case class FileLinkingError(start:File,root:File,fileKey:String) extends RuntimeException(
    s"""
      Unable to find file by key [$fileKey].
      Initial scan folder [$start] with strategy recursive walk to root folder
      or search by absolute path strategy [$root] and [$fileKey]
    """
  )
}