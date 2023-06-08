package very.util.persistence.quill

import io.getquill.context.jdbc.JdbcContextTypes
import io.getquill.PostgresDialect

import java.time.OffsetDateTime

trait OffsetDateTimeSupport {
  this: JdbcContextTypes[PostgresDialect, _] =>

  given encodeOffsetDateTime: Encoder[OffsetDateTime] =
    encoder(
      java.sql.Types.TIMESTAMP_WITH_TIMEZONE,
      (index, value, row) => row.setObject(index, value)
    )

  given decodeOffsetDateTime: Decoder[OffsetDateTime] =
    decoder((index, row, _) => row.getObject(index, classOf[OffsetDateTime]))

}
