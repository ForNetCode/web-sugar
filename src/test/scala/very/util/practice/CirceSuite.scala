package very.util.practice;

import io.circe.*
import io.circe.derivation.{Configuration, ConfiguredCodec}
import io.circe.generic.auto.*
import sttp.apispec.ExampleSingleValue
//import  io.circe.syntax.*
import io.circe.parser.*

given Configuration = Configuration.default.withDefaults

case class A(a:String = "default") derives ConfiguredCodec

case class ShortExample[T](a:T)

class CirceSuite extends munit.FunSuite {
  // https://github.com/circe/circe/issues/2136
  test("default value") {
    assertEquals(decode[A]("{}"), Right(A()))
  }

  test("short value decode") {
    import io.circe.syntax.*
    val a:Short = 1
    println(ShortExample(List(a)).asJson)
  }
}
