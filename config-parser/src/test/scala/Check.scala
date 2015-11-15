import java.io.File
import java.util.regex.Pattern

import config.parser.Parser.FileLinkingError
import config.parser.Validator.XSDException
import config.parser._
import org.scalatest.FunSuite

class Check extends FunSuite {

  val resDir = new File(System.getProperty("user.dir"), "/config-parser/src/test/fs")
  require(resDir.exists())

  def withFile[T](name: String, f: (Seq[T]) => Unit) = {
    val a = Parser.parse(file(name), resDir)

    assertResult(a.active)(true)
    assertResult(a.description)("Some information about test")

    f(a.block.map(_.asInstanceOf[T]))
  }

  test("CONFIG COLLECTOR") {
    val collector = ConfigCollector(resDir,Pattern.compile(".*"))

    assert(collector.collect().length == 11)

    val seq: Seq[(File,Either[Throwable, Config])] = collector.getConfigs
    assert(seq.length == 11)
    assert(seq.map(_._2).count(_.isRight) === 9)
    assert(seq.map(_._2).count(_.isLeft) === 2)
  }

  test("FILE LINKING ERROR") {
    intercept[FileLinkingError] {
      withFile[GET]("/invalid/get/config.xml", (a) => {
        // not executed
      })
    }
  }

  test("GET") {
    withFile[GET]("/valid/get/config.xml", (a) => {
      assert(a.length == 2)

      var q = a(0)
      assert(q.url === "/file?folder_id=15")
      assert(q.headers === Seq(("1", "1")))
      assert(q.code === 200)
      assert(q.file.get.exists())
      assert(q.file.get == new File(resDir, "/valid/get/action_1.json"))
      assert(q.response_headers === Seq(("Content-Type", "application/json; charset=utf-8")))

      q = a(1)
      assert(q.url === "/user/1")
      assert(q.headers === Seq())
      assert(q.code === 200)
      assert(q.file === None)
      assert(q.response_headers === Seq())
    })
  }

  test("POST") {
    withFile[POST]("/valid/post/config.xml", (a) => {
      assert(a.length == 3)

      var q = a(0)
      assert(q.url === "/file/create")
      assert(q.multipart === true)
      assert(q.headers === Seq(("Gzip", "yes"), ("asd", "asd")))
      assert(q.params === Seq(("test", "1"), ("test1", "2"), ("test2", "3"), ("test3", "4")))

      var f = q.files(0)
      assert(f._1 === "file")
      assert(f._2.exists())
      assert(f._2 == new File(resDir, "/valid/post/data.txt"))
      assert(f._3 === None)

      f = q.files(1)
      assert(f._1 === "file2")
      assert(f._2.exists())
      assert(f._2 == new File(resDir, "/valid/post/data.txt"))
      assert(f._3 === Some("data1.txt"))

      assert(q.code === 400)
      assert(q.file.get.exists())
      assert(q.file.get == new File(resDir, "/valid/post/action_1.json"))
      assert(q.response_headers === Seq(("Content-Type", "application/json; charset=utf-8")))

      q = a(1)
      assert(q.url === "/file/create1")
      assert(q.headers === Seq(("Gzip1", "yes1"), ("asd1", "asd1")))
      assert(q.params === Seq(("test", "1")))
      assert(q.files === Seq())
      assert(q.code === 200)
      assert(q.file.get.exists())
      assert(q.file.get == new File(resDir, "/valid/post/action_3.json"))
      assert(q.response_headers === Seq())

      q = a(2)
      assert(q.url === "/file/create")
      assert(q.headers === Seq())
      assert(q.params === Seq())
      assert(q.files === Seq())
      assert(q.code === 400)
      assert(q.file.get.exists())
      assert(q.file.get == new File(resDir, "/valid/post/action_4.json"))
      assert(q.response_headers === Seq())
    })
  }

  test("PUT") {
    withFile[PUT]("/valid/put/config.xml", (a) => {
      assert(a.length == 3)

      var q = a(0)
      assert(q.url === "/file/create")
      assert(q.multipart === true)
      assert(q.headers === Seq(("Gzip", "yes"), ("asd", "asd")))
      assert(q.params === Seq(("test", "1"), ("test1", "2"), ("test2", "3"), ("test3", "4")))

      var f = q.files(0)
      assert(f._1 === "file")
      assert(f._2.exists())
      assert(f._3 === None)

      f = q.files(1)
      assert(f._1 === "file2")
      assert(f._2.exists())
      assert(f._3 === Some("data1.txt"))

      assert(q.code === 400)
      assert(q.file.get.exists())
      assert(q.response_headers === Seq(("Content-Type", "application/json; charset=utf-8")))

      q = a(1)
      assert(q.url === "/file/create1")
      assert(q.headers === Seq(("Gzip1", "yes1"), ("asd1", "asd1")))
      assert(q.params === Seq(("test", "1")))
      assert(q.files === Seq())
      assert(q.code === 200)
      assert(q.file.get.exists())
      assert(q.response_headers === Seq())

      q = a(2)
      assert(q.url === "/file/create")
      assert(q.headers === Seq())
      assert(q.params === Seq())
      assert(q.files === Seq())
      assert(q.code === 400)
      assert(q.file.get.exists())
      assert(q.response_headers === Seq())

    })
  }

  test("PATCH") {
    withFile[PATCH]("/valid/patch/config.xml", (a) => {
      assert(a.length == 3)

      var q = a(0)
      assert(q.url === "/file/create")
      assert(q.multipart === true)
      assert(q.headers === Seq(("Gzip", "yes"), ("asd", "asd")))
      assert(q.params === Seq(("test", "1"), ("test1", "2"), ("test2", "3"), ("test3", "4")))

      var f = q.files(0)
      assert(f._1 === "file")
      assert(f._2.exists())
      assert(f._3 === None)

      f = q.files(1)
      assert(f._1 === "file2")
      assert(f._2.exists())
      assert(f._3 === Some("data1.txt"))

      assert(q.code === 400)
      assert(q.file.get.exists())
      assert(q.response_headers === Seq(("Content-Type", "application/json; charset=utf-8")))

      q = a(1)
      assert(q.url === "/file/create1")
      assert(q.headers === Seq(("Gzip1", "yes1"), ("asd1", "asd1")))
      assert(q.params === Seq(("test", "1")))
      assert(q.files === Seq())
      assert(q.code === 200)
      assert(q.file.get.exists())
      assert(q.response_headers === Seq())

      q = a(2)
      assert(q.url === "/file/create")
      assert(q.headers === Seq())
      assert(q.params === Seq())
      assert(q.files === Seq())
      assert(q.code === 400)
      assert(q.file.get.exists())
      assert(q.response_headers === Seq())

    })
  }

  test("DELETE") {
    withFile[DELETE]("/valid/delete/config.xml", (a) => {
      assert(a.length == 2)

      var q = a(0)
      assert(q.url === "/file/create")
      assert(q.headers === Seq(("Gzip", "yes"), ("asd", "asd")))
      assert(q.code === 400)
      assert(q.file.get.exists())
      assert(q.response_headers === Seq(("Content-Type", "application/json; charset=utf-8")))

      q = a(1)
      assert(q.url === "/file/create")
      assert(q.headers === Seq())
      assert(q.code === 400)
      assert(q.file === None)
      assert(q.response_headers === Seq())
    })
  }

  test("DUMP") {
    withFile[DUMP]("/valid/dump/config.xml", (a) => {
      assert(a.length == 1)

      val q = a(0)
      assert(q.file.get.exists())

      assert(q.sql(0) === "SELECT * FROM t_user WHERE id = 1")
      assert(q.sql(1) === "SELECT * FROM t_comments c WHERE c.user_id=1")
    })
  }

  test("ROLE") {
    withFile[ROLE]("/valid/role/config.xml", (a) => {
      assert(a.length == 1)

      val q = a(0)
      assert(q.credentials.get.exists())

      q.block(0).asInstanceOf[DELETE]
      q.block(1).asInstanceOf[DUMP]
      q.block(2).asInstanceOf[GET]

      assert(q.block.length === 3)
    })
  }

  test("FULL") {
    val a = Parser.parse(file("/valid/full/config.xml"), resDir)

    assert(a.block.length == 16)
    val o = a.block.iterator
    o.next().asInstanceOf[POST]
    o.next().asInstanceOf[POST]
    o.next().asInstanceOf[POST]
    o.next().asInstanceOf[POST]
    o.next().asInstanceOf[GET]
    o.next().asInstanceOf[DELETE]
    o.next().asInstanceOf[GET]
    o.next().asInstanceOf[DUMP]
    o.next().asInstanceOf[DELETE]
    o.next().asInstanceOf[DUMP]
    o.next().asInstanceOf[GET]
    o.next().asInstanceOf[GET]
    o.next().asInstanceOf[ROLE]
    o.next().asInstanceOf[ROLE]
    o.next().asInstanceOf[PUT]
    o.next().asInstanceOf[PATCH]
  }

  test("RANDOM FILE LOCATION (WALK UP TEST)") {
    val a = Parser.parse(file("/valid/random_file_locations/1/2/3/config.xml"), resDir)

    assert(a.block.length == 16)
    val o = a.block.iterator
    o.next().asInstanceOf[POST]
    o.next().asInstanceOf[POST]
    o.next().asInstanceOf[POST]
    o.next().asInstanceOf[POST]
    o.next().asInstanceOf[GET]
    o.next().asInstanceOf[DELETE]
    o.next().asInstanceOf[GET]
    o.next().asInstanceOf[DUMP]
    o.next().asInstanceOf[DELETE]
    o.next().asInstanceOf[DUMP]
    o.next().asInstanceOf[GET]
    o.next().asInstanceOf[GET]
    o.next().asInstanceOf[ROLE]
    o.next().asInstanceOf[ROLE]
    o.next().asInstanceOf[PUT]
    o.next().asInstanceOf[PATCH]
  }

  test("INVALID XSD 1") {
    val e = intercept[XSDException] {
      Parser.parse(file("_ERROR_1.xml"), resDir)
    }.errors

    assertResult(29)(e.values.head.length)
  }

  test("INVALID XSD 2") {
    val e = intercept[XSDException] {
      Parser.parse(file("_ERROR_2.xml"), resDir)
    }.errors

    assertResult(11)(e.values.head.length)
  }

  def file(name: String): File = new File(resDir, name)
}