package very.util.web
import io.circe.Printer

object JsonConfig {
  val jsonPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)
}
