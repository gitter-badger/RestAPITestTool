package config.parser

import java.io.File
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import org.xml.sax.{ErrorHandler, SAXParseException}

import scala.collection.mutable.ArrayBuffer

object Validator {
  val schema = {
    val sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    val url = Thread.currentThread.getContextClassLoader.getResource("config.xsd")
    sf.newSchema(new File(url.getPath))
  }

  @throws[XSDException]
  def validate(file: File) {
    val errors = scala.collection.mutable.Map[String, ArrayBuffer[Problem]]()

    val validator = Validator.schema.newValidator()
    validator.setErrorHandler(new ErrorHandler {

      override def warning(e: SAXParseException): Unit = collect("WARN", e)
      override def error(e: SAXParseException): Unit = collect("ERROR", e)
      override def fatalError(e: SAXParseException): Unit = collect("FATAL", e)

      def collect(level: String, e: SAXParseException) {
        val seq = errors.getOrElse(level, ArrayBuffer()) += Problem(e.getMessage,e.getLineNumber,e.getColumnNumber)
        errors.put(level, seq)
      }
    })
    validator.validate(new StreamSource(file))

    if (errors.nonEmpty) throw new XSDException(errors.toMap)
  }

  case class Problem(message: String,line:Int,column:Int)
  case class XSDException(errors: Map[String, ArrayBuffer[Problem]]) extends RuntimeException(errors.toString())
}