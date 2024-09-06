package very.util.persistence.scalikejdbc.mapper

import scalikejdbc._

import scala.language.implicitConversions

/**
 * Active Record like template generator
 */
class CodeGenerator(table: Table, specifiedClassName: Option[String] = None)(
  implicit config: GeneratorConfig = GeneratorConfig()
) extends Generator
  with LoanPattern {
  import java.sql.{JDBCType => JavaSqlTypes}
  import java.io.{OutputStreamWriter, FileOutputStream, File}

  private val packageName = config.packageName
  private val className = specifiedClassName.getOrElse(config.tableNameToClassName(table.name))
  private val eol = config.lineBreak.value

  object TypeName {
    val Any = "Any"
    val AnyArray = "List[Any]"
    val ByteArray = "List[Byte]"
    val Long = "Long"
    val Boolean = "Boolean"
    val DateTime = "DateTime"
    val LocalDate = "LocalDate"
    val LocalTime = "LocalTime"
    val String = "String"
    val Byte = "Byte"
    val Int = "Int"
    val Short = "Short"
    val Float = "Float"
    val Double = "Double"
    val Blob = "Blob"
    val Clob = "Clob"
    val Ref = "Ref"
    val Struct = "Struct"
    val BigDecimal = "BigDecimal" // scala.math.BigDecimal
  }

  case class IndentGenerator(i: Int) {
    def indent: String = " " * i * 2
  }

  implicit def convertIntToIndentGenerator(i: Int): IndentGenerator = IndentGenerator(i)

  case class ColumnInScala(underlying: Column) {

    lazy val nameInScala: String = config.columnNameToFieldName(underlying.name)

    lazy val rawTypeInScala: String = underlying.dataType match {
      case JavaSqlTypes.ARRAY         => TypeName.AnyArray
      case JavaSqlTypes.BIGINT        => TypeName.Long
      case JavaSqlTypes.BINARY        => TypeName.ByteArray
      case JavaSqlTypes.BIT           => TypeName.Boolean
      case JavaSqlTypes.BLOB          => TypeName.Blob
      case JavaSqlTypes.BOOLEAN       => TypeName.Boolean
      case JavaSqlTypes.CHAR          => TypeName.String
      case JavaSqlTypes.CLOB          => TypeName.Clob
      case JavaSqlTypes.DATALINK      => TypeName.Any
      case JavaSqlTypes.DATE          => TypeName.LocalDate
      case JavaSqlTypes.DECIMAL       => TypeName.BigDecimal
      case JavaSqlTypes.DISTINCT      => TypeName.Any
      case JavaSqlTypes.DOUBLE        => TypeName.Double
      case JavaSqlTypes.FLOAT         => TypeName.Float
      case JavaSqlTypes.INTEGER       => TypeName.Int
      case JavaSqlTypes.JAVA_OBJECT   => TypeName.Any
      case JavaSqlTypes.LONGVARBINARY => TypeName.ByteArray
      case JavaSqlTypes.LONGVARCHAR   => TypeName.String
      case JavaSqlTypes.NULL          => TypeName.Any
      case JavaSqlTypes.NUMERIC       => TypeName.BigDecimal
      case JavaSqlTypes.OTHER         => TypeName.Any
      case JavaSqlTypes.REAL          => TypeName.Float
      case JavaSqlTypes.REF           => TypeName.Ref
      case JavaSqlTypes.SMALLINT      => TypeName.Short
      case JavaSqlTypes.STRUCT        => TypeName.Struct
      case JavaSqlTypes.TIME          => TypeName.LocalTime
      case JavaSqlTypes.TIMESTAMP     => config.dateTimeClass.simpleName
      case JavaSqlTypes.TINYINT       => TypeName.Byte
      case JavaSqlTypes.VARBINARY     => TypeName.ByteArray
      case JavaSqlTypes.VARCHAR       => TypeName.String
      case JavaSqlTypes.NVARCHAR      => TypeName.String
      case JavaSqlTypes.NCHAR         => TypeName.String
      case JavaSqlTypes.LONGNVARCHAR  => TypeName.String
      case _                          => TypeName.Any
    }

    lazy val typeInScala: String = {
      if (underlying.isNotNull) rawTypeInScala
      else "Option[" + rawTypeInScala + "]"
    }

    lazy val dummyValue: String = underlying.dataType match {
      case JavaSqlTypes.ARRAY         => "null"
      case JavaSqlTypes.BIGINT        => "1"
      case JavaSqlTypes.BINARY        => "1"
      case JavaSqlTypes.BIT           => "false"
      case JavaSqlTypes.BLOB          => "null"
      case JavaSqlTypes.BOOLEAN       => "true"
      case JavaSqlTypes.CHAR          => "'abc'"
      case JavaSqlTypes.CLOB          => "null"
      case JavaSqlTypes.DATALINK      => "null"
      case JavaSqlTypes.DATE          => "'1958-09-06'"
      case JavaSqlTypes.DECIMAL       => "1"
      case JavaSqlTypes.DISTINCT      => "null"
      case JavaSqlTypes.DOUBLE        => "0.1"
      case JavaSqlTypes.FLOAT         => "0.1"
      case JavaSqlTypes.INTEGER       => "1"
      case JavaSqlTypes.JAVA_OBJECT   => "null"
      case JavaSqlTypes.LONGVARBINARY => "null"
      case JavaSqlTypes.LONGVARCHAR   => "'abc'"
      case JavaSqlTypes.NULL          => "null"
      case JavaSqlTypes.NUMERIC       => "1"
      case JavaSqlTypes.OTHER         => "null"
      case JavaSqlTypes.REAL          => "null"
      case JavaSqlTypes.REF           => "null"
      case JavaSqlTypes.SMALLINT      => "1"
      case JavaSqlTypes.STRUCT        => "null"
      case JavaSqlTypes.TIME          => "'12:00:00'"
      case JavaSqlTypes.TIMESTAMP     => "'1958-09-06 12:00:00'"
      case JavaSqlTypes.TINYINT       => "1"
      case JavaSqlTypes.VARBINARY     => "null"
      case JavaSqlTypes.VARCHAR       => "'abc'"
      case JavaSqlTypes.NVARCHAR      => "'abc'"
      case JavaSqlTypes.NCHAR         => "'abc'"
      case JavaSqlTypes.LONGNVARCHAR  => "'abc'"
      case _                          => "null"
    }

    lazy val defaultValueInScala: String = underlying.typeInScala match {
      case TypeName.AnyArray   => "Array[Any]()"
      case TypeName.Long       => "1L"
      case TypeName.ByteArray  => "Array[Byte]()"
      case TypeName.Boolean    => "false"
      case TypeName.String     => "\"MyString\""
      case TypeName.LocalDate  => "LocalDate.now"
      case TypeName.BigDecimal => "new java.math.BigDecimal(\"1\")"
      case TypeName.Double     => "0.1D"
      case TypeName.Float      => "0.1F"
      case TypeName.Int        => "123"
      case TypeName.Short      => "123"
      case TypeName.DateTime   => "DateTime.now"
      case TypeName.Byte       => "1"
      case _                   => "null"
    }

    private[CodeGenerator] def isAny: Boolean = rawTypeInScala == TypeName.Any
  }

  /**
   * Create directory to put the source code file if it does not exist yet.
   */
  def mkdirRecursively(file: File): Unit = {
    val parent = file.getAbsoluteFile.getParentFile
    if (!parent.exists) mkdirRecursively(parent)
    if (!file.exists) file.mkdir()
  }

  implicit def convertColumnToColumnInScala(column: Column): ColumnInScala = ColumnInScala(column)

  private def outputModelFile =
    new File(config.srcDir + "/" + packageName.replace(".", "/") + "/" + className + ".scala")

  private def shouldBeSkipped: Boolean =
    config.tableNamesToSkip.contains(table.name.toLowerCase)

  /**
   * Write the source code if outputFile does not exists.
   */
  def writeModelIfNonexistentAndUnskippable(): Boolean = {
    if (outputModelFile.exists) {
      println("\"" + packageName + "." + className + "\"" + " already exists.")
      false
    } else if (shouldBeSkipped) {
      println("\"" + packageName + "." + className + "\"" + " is skipped by settings.")
      false
    } else {
      writeModel()
      true
    }
  }

  /**
   * Write the source code to outputFile.
   * It overwrites a file if it already exists.
   */
  def writeModel(): Unit = {
    mkdirRecursively(outputModelFile.getParentFile)
    using(new FileOutputStream(outputModelFile)) { fos =>
      using(new OutputStreamWriter(fos)) { writer =>
        writer.write(modelAll())
        println("\"" + packageName + "." + className + "\"" + " created.")
      }
    }
  }

  def classPart: String = {
    val constructorArgs = table.allColumns
      .map { c =>
        2.indent + c.nameInScala + ": " + c.typeInScala + (if (c.isNotNull) "" else " = None")
      }
      .mkString("," + eol)

    val baseTypes = {
      val types = config.tableNameToBaseTypes(table.name)
      if (types.isEmpty) ""
      else types.mkString("extends ", " with ", " ")
    }

    s"""case class ${className}(
       |${constructorArgs}
       |) ${baseTypes}
       |""".stripMargin + eol
  }

  def daoPart: String = {
    val constructors = table.allColumns
      .map { c =>
        3.indent + c.nameInScala + s" = rs.get(n.${c.nameInScala})"
      }
      .mkString("," + eol)

    val idTypeOpt = table.primaryKeyColumns.headOption.map(_.typeInScala)
    val hasTimestampFeature = table.allColumns.exists(_.nameInScala == "updatedAt")
    val extendBase = (hasTimestampFeature, idTypeOpt) match {
      case (true, Some(typ))  => "TimestampsWithId"
      case (false, Some(typ)) => "CRUDMapperWithId"
      case (true, _)          => "NoIdTimestampsFeature"
      case _                  => "NoIdCUDFeature"
    }

    val extendParam = idTypeOpt.map(v => s"$v,$className").getOrElse(className)

    val idOverrideMethods = idTypeOpt match {
      case None => ""
      case Some(idType) =>
        s"""
           |${1.indent}override def idToRawValue(id: ${idType}): Any = id
           |
           |${1.indent}override def rawValueToId(rawValue: Any): $idType = rawValue.toString.to$idType
           |""".stripMargin
    }
    val extendStr = config.daoExtendImport match {
      case Some(imp) => s"extends ${imp.split('.').last} with $extendBase[$extendParam]"
      case None      => s"extends $extendBase[$extendParam]"
    }
    s"""import scalikejdbc.orm.*
       |
       |trait ${className}Dao $extendStr{
       |  override val tableName:String = "${table.name}"
       |
       |  override def defaultAlias: Alias[${className}] = createAlias("${config.classNameToAliasName(className)}")
       |
       |  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[${className}]): ${className} =
       |    ${className}(
       |$constructors
       |    )
       |$idOverrideMethods
       |}
       |""".stripMargin
  }

  private val timeImport: String = {
    val timeClasses = Set(TypeName.LocalDate, TypeName.LocalTime) ++ DateTimeClass.all.map(_.simpleName)

    table.allColumns.map(_.rawTypeInScala).filter(timeClasses) match {
      case classes if classes.nonEmpty =>
        if (config.dateTimeClass == DateTimeClass.JodaDateTime) {
          val jodaImports = classes.distinct match {
            case ti :: Nil => s"import org.joda.time.$ti"
            case list      => "import org.joda.time.{" + list.distinct.mkString(", ") + "}"
          }
          jodaImports + eol +
            "import scalikejdbc.jodatime.JodaParameterBinderFactory._" + eol +
            "import scalikejdbc.jodatime.JodaTypeBinder._" + eol
        } else {
          "import java.time.{" + classes.distinct.mkString(", ") + "}" + eol
        }
      case _ => ""
    }
  }

  def modelAll(): String = {
    val javaSqlImport = table.allColumns.flatMap { c =>
      c.rawTypeInScala match {
        case TypeName.Blob   => Some("Blob")
        case TypeName.Clob   => Some("Clob")
        case TypeName.Ref    => Some("Ref")
        case TypeName.Struct => Some("Struct")
        case _               => None
      }
    } match {
      case classes if classes.nonEmpty => "import java.sql.{" + classes.distinct.mkString(", ") + "}" + eol
      case _                           => ""
    }

    "package " + config.packageName + eol +
      eol +
      "import scalikejdbc._" + eol +
      timeImport +
      javaSqlImport + eol +
      config.daoExtendImport.map(v => s"import $v" + eol).getOrElse("") +
      classPart + eol +
      eol +
      daoPart + eol
  }

}