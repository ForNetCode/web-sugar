package very.util.persistence.scalikejdbc.mapper

import java.util.Locale.ENGLISH

import scala.util.Random

case class GeneratorConfig(
                            srcDir: String = "src/main/scala",
                            testDir: String = "src/test/scala",
                            packageName: String = "models",
                            lineBreak: LineBreak = LineBreak("\n"),
                            encoding: String = "UTF-8",
                            dateTimeClass: DateTimeClass = DateTimeClass.ZonedDateTime,
                            tableNameToClassName: String => String = GeneratorConfig.toCamelCase,
                            columnNameToFieldName: String => String = GeneratorConfig.columnNameToFieldNameBasic andThen GeneratorConfig
                              .addSuffixIfConflict("Column"),
                            returnCollectionType: ReturnCollectionType = ReturnCollectionType.List,
                            tableNamesToSkip: collection.Seq[String] = List(),
                            tableNameToBaseTypes: String => Seq[String] = _ => Nil,
                            classNameToAliasName: String => String = GeneratorConfig.aliasGenerate,
                            daoExtendImport: Option[String] = None
                          )

object GeneratorConfig {
  private def toProperCase(s: String): String = {
    if (s == null || s.trim.isEmpty) ""
    else s.substring(0, 1).toUpperCase(ENGLISH) + s.substring(1).toLowerCase(ENGLISH)
  }

  val aliasGenerate: String => String = className => {
    className.filter(_.isUpper) + Random.nextInt(20)
  }

  private val toCamelCase: String => String = _.split("_").foldLeft("") { (camelCaseString, part) =>
    camelCaseString + toProperCase(part)
  }

  val reservedWords: Set[String] = Set(
    "abstract",
    "case",
    "catch",
    "class",
    "def",
    "do",
    "else",
    "extends",
    "false",
    "final",
    "finally",
    "for",
    "forSome",
    "if",
    "implicit",
    "import",
    "lazy",
    "match",
    "new",
    "null",
    "macro",
    "object",
    "override",
    "package",
    "private",
    "protected",
    "return",
    "sealed",
    "super",
    "then",
    "this",
    "throw",
    "trait",
    "try",
    "true",
    "type",
    "val",
    "var",
    "while",
    "with",
    "yield"
  )

  val quoteReservedWord: String => String = { name =>
    if (reservedWords(name)) "`" + name + "`"
    else name
  }

  val conflictMethods: Set[String] = Set(
    "toString",
    "hashCode",
    "wait",
    "getClass",
    "notify",
    "notifyAll",
    "productArity",
    "productElementName",
    "productElementNames",
    "productIterator",
    "productPrefix",
    "copy"
  )

  def addSuffixIfConflict(suffix: String): String => String = { name =>
    if (conflictMethods(name)) name + suffix
    else name
  }

  val lowerCamelCase: String => String =
    GeneratorConfig.toCamelCase.andThen { camelCase =>
      s"${camelCase.head.toLower}${camelCase.tail}"
    }

  val columnNameToFieldNameBasic: String => String = {
    GeneratorConfig.lowerCamelCase andThen GeneratorConfig.quoteReservedWord
  }

  private val tableNameToSyntaxNameDefault: String => String = { tableName =>
    val name = "[A-Z]".r.findAllIn(toCamelCase(tableName)).mkString.toLowerCase(ENGLISH)
    if (name == "rs" || name.isEmpty) "r" else name
  }
}