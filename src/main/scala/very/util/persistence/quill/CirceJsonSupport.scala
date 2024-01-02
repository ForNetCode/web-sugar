package very.util.persistence.quill

import io.getquill.context.jdbc.JdbcContextTypes
import io.getquill.PostgresDialect
import org.postgresql.util.PGobject
import io.circe.*
import io.circe.parser.*

trait DBSerializer
trait CirceJsonSupport {
  this: JdbcContextTypes[PostgresDialect, _] =>

  private val jsonPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

  protected def encodeEntityToJsonb[T](using encode: io.circe.Encoder[T]): Encoder[T] = encoder[T](
    java.sql.Types.OTHER,
    (index, value, row) => {
      val jsonObject = new PGobject()
      jsonObject.setType("jsonb")
      jsonObject.setValue(jsonPrinter.print(encode(value)))
      row.setObject(index, jsonObject)
    }
  )

  given encodeJsonb[T <: DBSerializer](using encode: io.circe.Encoder[T]): Encoder[T] = encodeEntityToJsonb

  given encodeJson: Encoder[Json] = {
    encoder(
      java.sql.Types.OTHER,
      (index, value, row) => {
        val jsonObject = new PGobject()
        jsonObject.setType("jsonb")
        jsonObject.setValue(jsonPrinter.print(value))
        row.setObject(index, jsonObject)
      }
    )
  }

  protected def decodeEntityToJsonb[T](using io.circe.Decoder[T]): Decoder[T] =
    decoder { (index, row, session) =>
      val data = row.getString(index)
      decode(data).toOption.get
    }
  given decodeJsonb[T <: DBSerializer](using io.circe.Decoder[T]): Decoder[T] =
    decodeEntityToJsonb


  given decodeJson: Decoder[Json] =
    decoder { (index, row, session) =>
      val data = row.getString(index)
      parse(data).toOption.get
    }
}
