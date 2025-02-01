package very.util.web

import enumeratum.values.*
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.codec.enumeratum.*
import sttp.tapir.docs.apispec.DocsExtensionAttribute.*
import sttp.tapir.json.circe.*
import sttp.tapir.{ Codec, Schema, SchemaAnnotations }

object TapirExtra {

  def stringEnumCodec[T <: StringEnumEntry](using `enum`: StringEnum[T]): PlainCodec[T] =
    Codec.string.mapEither(v => `enum`.withValueEither(v).left.map(_.notFoundValue))(_.value)

  def stringEnumSchema[T <: StringEnumEntry](using
    annotations: SchemaAnnotations[T],
    `enum`: StringEnum[T]
  ): Schema[T] =
    schemaForStringEnumEntry[T].docsExtension("x-enum-varnames", `enum`.values.map(_.toString))

  def shortEnumSchema[T <: ShortEnumEntry](using annotations: SchemaAnnotations[T], `enum`: ShortEnum[T]): Schema[T] =
    schemaForShortEnumEntry[T].docsExtension("x-enum-varnames", `enum`.values.map(_.toString)).format("int16")

  def intEnumSchema[T <: IntEnumEntry](using annotations: SchemaAnnotations[T], `enum`: IntEnum[T]): Schema[T] =
    schemaForIntEnumEntry[T].docsExtension("x-enum-varnames", `enum`.values.map(_.toString))
}
