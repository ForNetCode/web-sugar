package very.util.practice

import munit.FunSuite


object IntEnumTest {

  import enumeratum.values.*

  sealed abstract class Judgement(val value: Int) extends IntEnumEntry

  object Judgement extends IntEnum[Judgement] with IntCirceEnum[Judgement] with IntQuillEnum[Judgement]{

    case object Good extends Judgement(1)

    case object Meh extends Judgement(2)

    case object Bad extends Judgement(3)

    val values = findValues
  }
}

object StringEnumTest {

  import enumeratum.*
  import io.circe.*
  //import io.circe.generic.auto.*

  sealed trait ShirtSize extends EnumEntry

  case object ShirtSize extends Enum[ShirtSize] with CirceEnum[ShirtSize] with QuillEnum[ShirtSize] {

    case object Small extends ShirtSize

    case object Medium extends ShirtSize

    case object Large extends ShirtSize

    val values = findValues

  }
}

class EnumerationSuite extends FunSuite {


  test("circe string enum") {
    import io.circe.*
    import io.circe.syntax.*
    StringEnumTest.ShirtSize.values.foreach { size =>
      assert(size.asJson == Json.fromString(size.entryName))
    }
  }
  test("circe int enum") {
    import io.circe.*
    import io.circe.syntax.*
    IntEnumTest.Judgement.values.foreach { v =>
      assert(v.asJson == Json.fromInt(v.value))
    }

  }

}
