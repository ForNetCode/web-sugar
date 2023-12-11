package very.util.practice;

import io.circe.*
import io.circe.derivation.{Configuration, ConfiguredCodec}
import io.circe.generic.auto.*
//import  io.circe.syntax.*
import io.circe.parser.*

given Configuration = Configuration.default.withDefaults

case class A(a:String = "default") derives ConfiguredCodec

class CirceSuite extends munit.FunSuite {
  // https://github.com/circe/circe/issues/2136
  test("default value") {
    assertEquals(decode[A]("{}"), Right(A()))
  }
}
