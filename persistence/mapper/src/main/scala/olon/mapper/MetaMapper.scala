package olon
package mapper

import java.lang.reflect.Method
import java.sql.{ResultSet, Types, PreparedStatement}
import java.util.{Date, Locale}

import scala.language.existentials

import scala.collection.mutable.{ListBuffer, HashMap}
import scala.collection.immutable.{SortedMap, TreeMap}
import scala.xml._

import common._
import json._
import util.Helpers._
import util.{SourceFieldMetadata, NamedPF, FieldError, Helpers,CssSel,PassThru}
import http.{LiftRules, S, SHtml, RequestMemoize, Factory}
import http.js._

trait BaseMetaMapper {
  type RealType <: Mapper[RealType]

  def beforeSchemifier: Unit
  def afterSchemifier: Unit

  def dbTableName: String
  def _dbTableNameLC: String
  def mappedFields: Seq[BaseMappedField];
  def dbAddTable: Box[() => Unit]

  def dbIndexes: List[BaseIndex[RealType]]
}

/**
 * Rules and functions shared by all Mappers
 */
object MapperRules extends Factory {
  /**
   * This function converts a header name into the appropriate
   * XHTML format for displaying across the headers of a
   * formatted block.  The default is &lt;th&gt; for use
   * in XHTML tables.  If you change this function, the change
   * will be used for all MetaMappers, unless they've been
   * explicitly changed.
   */
  var displayNameToHeaderElement: String => NodeSeq = in => <th>{in}</th>

  /**
   * This function converts an element into the appropriate
   * XHTML format for displaying across a line
   * formatted block.  The default is &lt;td&gt; for use
   * in XHTML tables.  If you change this function, the change
   * will be used for all MetaMappers, unless they've been
   * explicitly changed.
   */
  var displayFieldAsLineElement: NodeSeq => NodeSeq = in => <td>{in}</td>

  /**
   * This function is the global (for all MetaMappers that have
   * not changed their formatFormElement function) that
   * converts a name and form for a given field in the
   * model to XHTML for presentation in the browser.  By
   * default, a table row ( &lt;tr&gt; ) is presented, but
   * you can change the function to display something else.
   */
  var formatFormElement: (NodeSeq, NodeSeq) => NodeSeq =
  (name, form) =>
  <xml:group><tr>
      <td>{name}</td>
      <td>{form}</td>
             </tr></xml:group>

  /**
  * What are the rules and mechanisms for putting quotes around table names?
  */
  val quoteTableName: FactoryMaker[String => String] =
  new FactoryMaker[String => String]((s: String) => if (s.indexOf(' ') >= 0) "\""+s+"\"" else s) {}

  /**
  * What are the rules and mechanisms for putting quotes around column names?
  */
  val quoteColumnName: FactoryMaker[String => String] =
  new FactoryMaker[String => String]((s: String) => if (s.indexOf(' ') >= 0) "\""+s+"\"" else s) {}

 /**
  * Function that determines if foreign key constraints are
  * created by Schemifier for the specified connection.
  *
  * Note: The driver choosen must also support foreign keys for
  * creation to happen
  */
  var createForeignKeys_? : ConnectionIdentifier => Boolean = c => false


  /**
   * This function is used to calculate the displayName of a field. Can be
   * used to easily localize fields based on the locale in the
   * current request
   */
  val displayNameCalculator: FactoryMaker[(BaseMapper, Locale, String) => String] =
  new FactoryMaker[(BaseMapper, Locale, String) => String]((m: BaseMapper,l: Locale,name: String) => name) {}

  /**
   * Calculate the name of a column based on the name
   * of the MappedField. Must be set in Boot before any code
   * that touches the MetaMapper.
   *
   * To get snake_case, use this:
   *
   *  MapperRules.columnName =  (_,name) => StringHelpers.snakify(name)
   */
  var columnName: (ConnectionIdentifier,String) => String = (_,name) => name.toLowerCase

  /**
   * Calculate the name of a table based on the name
   * of the Mapper. Must be set in Boot before any code
   * that tocuhes the MetaMapper.
   *
   * To get snake_case, use this
   *
   *  MapperRules.tableName =  (_,name) => StringHelpers.snakify(name)
   */
  var tableName: (ConnectionIdentifier,String) => String = (_,name) => name.toLowerCase
}

trait MetaMapper[A<:Mapper[A]] extends BaseMetaMapper with Mapper[A] {
  self: A =>

  private val logger = Logger(classOf[MetaMapper[A]])

  case class FieldHolder(name: String, method: Method, field: MappedField[_, A])

  type RealType = A

  def beforeValidation: List[A => Unit] = Nil
  def beforeValidationOnCreate: List[A => Unit] = Nil
  def beforeValidationOnUpdate: List[A => Unit] = Nil
  def afterValidation: List[A => Unit] = Nil
  def afterValidationOnCreate: List[A => Unit] = Nil
  def afterValidationOnUpdate: List[A => Unit] = Nil

  def beforeSave: List[A => Unit] = Nil
  def beforeCreate: List[(A) => Unit] = Nil
  def beforeUpdate: List[(A) => Unit] = Nil

  def afterSave: List[(A) => Unit] = Nil
  def afterCreate: List[(A) => Unit] = Nil
  def afterUpdate: List[(A) => Unit] = Nil

  def beforeDelete: List[(A) => Unit] = Nil
  def afterDelete: List[(A) => Unit] = Nil

  /**
   * If there are model-specific validations to perform, override this
   * method and return an additional list of validations to perform
   */
  def validation: List[A => List[FieldError]] = Nil

  private def clearPostCommit(in: A): Unit = {
    in.addedPostCommit = false
  }

  private def clearPCFunc: A => Unit = clearPostCommit _

  def afterCommit: List[A => Unit] = Nil

  def dbDefaultConnectionIdentifier: ConnectionIdentifier = DefaultConnectionIdentifier

  def findAll(): List[A] =
  findMapDb(dbDefaultConnectionIdentifier, Nil :_*)(v => Full(v))

  def findAllDb(dbId:ConnectionIdentifier): List[A] =
  findMapDb(dbId, Nil :_*)(v => Full(v))

  def countByInsecureSql(query: String, checkedBy: IHaveValidatedThisSQL): scala.Long =
  countByInsecureSqlDb(dbDefaultConnectionIdentifier, query, checkedBy)

  def countByInsecureSqlDb(dbId: ConnectionIdentifier, query: String, checkedBy: IHaveValidatedThisSQL): scala.Long =
  DB.use(dbId)(DB.prepareStatement(query, _)(DB.exec(_)(rs => if (rs.next) rs.getLong(1) else 0L)))

  def findAllByInsecureSql(query: String, checkedBy: IHaveValidatedThisSQL): List[A] = findAllByInsecureSqlDb(dbDefaultConnectionIdentifier, query, checkedBy)

  /**
   * Execute a PreparedStatement and return a List of Mapper instances. {@code f} is
   * where the user will do the work of creating the PreparedStatement and
   * preparing it for execution.
   *
   * @param f A function that takes a SuperConnection and returns a PreparedStatement.
   * @return A List of Mapper instances.
   */
  def findAllByPreparedStatement(f: SuperConnection => PreparedStatement): List[A] = {
    DB.use(dbDefaultConnectionIdentifier) {
      conn =>
      findAllByPreparedStatement(dbDefaultConnectionIdentifier, f(conn))
    }
  }

  def findAllByPreparedStatement(dbId: ConnectionIdentifier, stmt: PreparedStatement): List[A] = findAllByPreparedStatementDb(dbId, stmt)(a => Full(a))

  def findAllByPreparedStatementDb[T](dbId: ConnectionIdentifier, stmt: PreparedStatement)(f: A => Box[T]): List[T] = {
    DB.exec(stmt) {
      rs => createInstances(dbId, rs, Empty, Empty, f)
    }
  }

  def findAllByInsecureSqlDb(dbId: ConnectionIdentifier, query: String, checkedBy: IHaveValidatedThisSQL): List[A] =
  findMapByInsecureSqlDb(dbId, query, checkedBy)(a => Full(a))


  def findMapByInsecureSql[T](query: String, checkedBy: IHaveValidatedThisSQL)
  (f: A => Box[T]): List[T] =
  findMapByInsecureSqlDb(dbDefaultConnectionIdentifier, query, checkedBy)(f)

  def findMapByInsecureSqlDb[T](dbId: ConnectionIdentifier, query: String, checkedBy: IHaveValidatedThisSQL)(f: A => Box[T]): List[T] = {
    DB.use(dbId) {
      conn =>
      DB.prepareStatement(query, conn) {
        st =>
        DB.exec(st) {
          rs =>
          createInstances(dbId, rs, Empty, Empty, f)
        }
      }
    }
  }

  def dbAddTable: Box[() => Unit] = Empty

  def count: Long = countDb(dbDefaultConnectionIdentifier, Nil :_*)

  def count(by: QueryParam[A]*): Long = countDb(dbDefaultConnectionIdentifier, by:_*)

  def countDb(dbId: ConnectionIdentifier, by: QueryParam[A]*): Long = {
    DB.use(dbId) {
      conn =>
      val bl = by.toList ::: addlQueryParams.get
      val (query, start, max) = addEndStuffs(addFields("SELECT COUNT(*) FROM "+MapperRules.quoteTableName.vend(_dbTableNameLC)+"  ", false, bl, conn), bl, conn)

      DB.prepareStatement(query, conn) {
        st =>
        setStatementFields(st, bl, 1, conn)
        DB.exec(st) {
          rs =>
          if (rs.next) rs.getLong(1)
          else 0
        }
      }
    }
  }

  //type KeyDude = T forSome {type T}
  type OtherMapper = KeyedMapper[_, _] // T forSome {type T <: KeyedMapper[KeyDude, T]}
  type OtherMetaMapper = KeyedMetaMapper[_, _] // T forSome {type T <: KeyedMetaMapper[KeyDude, OtherMapper]}
  //type OtherMapper = KeyedMapper[_, (T forSome {type T})]
  //type OtherMetaMapper = KeyedMetaMapper[_, OtherMapper]

  def findAllFields(fields: Seq[SelectableField],
                    by: QueryParam[A]*): List[A] =
  findMapFieldDb(dbDefaultConnectionIdentifier,
                 fields, by :_*)(v => Full(v))

  def findAllFieldsDb(dbId: ConnectionIdentifier,
                      fields: Seq[SelectableField],
                      by: QueryParam[A]*):
  List[A] = findMapFieldDb(dbId, fields, by :_*)(v => Full(v))

  private def dealWithPrecache(ret: List[A], by: Seq[QueryParam[A]]): List[A] = {

    val precache: List[PreCache[A, _, _]] = by.toList.flatMap{case j: PreCache[A, _, _] => List[PreCache[A, _, _]](j) case _ => Nil}
    for (j <- precache) {
      type FT = j.field.FieldType
      type MT = T forSome {type T <: KeyedMapper[FT, T]}

      val ol: List[MT] = if (!j.deterministic) {
        def filter(in: Seq[FT]): Seq[FT] =
        in.flatMap{
          case null => Nil
          case x: Number if x.longValue == 0L => Nil
          case x => List(x)
        }

        val lst: Set[FT] = Set(filter(ret.map(v => v.getSingleton.getActualField(v, j.field).get.asInstanceOf[FT])) :_*)

        j.field.dbKeyToTable.
        asInstanceOf[MetaMapper[A]].
        findAll(ByList(j.field.dbKeyToTable.primaryKeyField.
                       asInstanceOf[MappedField[FT, A]], lst.toList)).asInstanceOf[List[MT]]
      } else {
        j.field.dbKeyToTable.
        asInstanceOf[MetaMapper[A]].
        findAll(new InThing[A]{
            type JoinType = FT
            type InnerType = A

            val outerField: MappedField[JoinType, A] =
            j.field.dbKeyToTable.primaryKeyField.asInstanceOf[MappedField[JoinType, A]]
            val innerField: MappedField[JoinType, A] = j.field.asInstanceOf[MappedField[JoinType, A]]
            val innerMeta: MetaMapper[A] = j.field.fieldOwner.getSingleton

          def notIn = false

            val queryParams: List[QueryParam[A]] = by.toList
          }.asInstanceOf[QueryParam[A]] ).asInstanceOf[List[MT]]
      }

      val map: Map[FT, MT] =
      Map(ol.map(v => (v.primaryKeyField.get, v)) :_*)

      for (i <- ret) {
        val field: MappedForeignKey[FT, A, _] =
        getActualField(i, j.field).asInstanceOf[MappedForeignKey[FT, A, _]]

        map.get(field.get) match {
          case v => field._primeObj(Box(v))
        }
        //field.primeObj(Box(map.get(field.get).map(_.asInstanceOf[QQ])))
      }
    }

    ret
  }

  def findAll(by: QueryParam[A]*): List[A] =
  dealWithPrecache(findMapDb(dbDefaultConnectionIdentifier, by :_*)
                   (v => Full(v)), by)


  def findAllDb(dbId: ConnectionIdentifier,by: QueryParam[A]*): List[A] =
  dealWithPrecache(findMapDb(dbId, by :_*)(v => Full(v)), by)

  def bulkDelete_!!(by: QueryParam[A]*): Boolean = bulkDelete_!!(dbDefaultConnectionIdentifier, by :_*)
  def bulkDelete_!!(dbId: ConnectionIdentifier, by: QueryParam[A]*): Boolean = {
    DB.use(dbId) {
      conn =>
      val bl = by.toList ::: addlQueryParams.get
      val (query, start, max) = addEndStuffs(addFields("DELETE FROM "+MapperRules.quoteTableName.vend(_dbTableNameLC)+" ", false, bl, conn), bl, conn)

      DB.prepareStatement(query, conn) {
        st =>
        setStatementFields(st, bl, 1, conn)
        st.executeUpdate
        true
      }
    }
  }

  private def distinct(in: Seq[QueryParam[A]]): String =
    in.find {case Distinct() => true case _ => false}.isDefined match {
      case false => ""
      case true => " DISTINCT "
    }

  def findMap[T](by: QueryParam[A]*)(f: A => Box[T]) =
  findMapDb(dbDefaultConnectionIdentifier, by :_*)(f)

  def findMapDb[T](dbId: ConnectionIdentifier,
                   by: QueryParam[A]*)(f: A => Box[T]): List[T] =
  findMapFieldDb(dbId, mappedFields, by :_*)(f)

  /**
   * Given fields, a connection and the query parameters, build a query and return the query String,
   * and Start or MaxRows values (depending on whether the driver supports LIMIT and OFFSET)
   * and the complete List of QueryParams based on any synthetic query parameters calculated during the
   * query creation.
   *
   * @param fields -- a Seq of the fields to be selected
   * @param conn -- the SuperConnection to be used for calculating the query
   * @param by -- the varg of QueryParams
   *
   * @return a Tuple of the Query String, Start (offset), MaxRows (limit), and the list of all query parameters
   * including and synthetic query parameters
   */
  def buildSelectString(fields: Seq[SelectableField], conn: SuperConnection, by: QueryParam[A]*):
  (String, Box[Long], Box[Long], List[QueryParam[A]]) = {
    val bl = by.toList ::: addlQueryParams.get
    val selectStatement = "SELECT "+
    distinct(by)+
    fields.map(_.dbSelectString).
    mkString(", ")+
    " FROM "+MapperRules.quoteTableName.vend(_dbTableNameLC)+"  "

    val (str, start, max) = addEndStuffs(addFields(selectStatement, false, bl, conn), bl, conn)
    (str, start, max, bl)
  }

  def findMapFieldDb[T](dbId: ConnectionIdentifier, fields: Seq[SelectableField],
                        by: QueryParam[A]*)(f: A => Box[T]): List[T] = {
    DB.use(dbId) {
      conn =>

      val (query, start, max, bl) = buildSelectString(fields, conn, by :_*)
      DB.prepareStatement(query, conn) {
        st =>
        setStatementFields(st, bl, 1, conn)
        DB.exec(st)(createInstances(dbId, _, start, max, f))
      }
    }
  }

  def create: A = createInstance

  object addlQueryParams extends olon.http.RequestVar[List[QueryParam[A]]](Nil) {
    override val __nameSalt = randomString(10)
  }

  private[mapper] def addFields(what: String, whereAdded: Boolean,
                                by: List[QueryParam[A]], conn: SuperConnection): String = {

    var wav = whereAdded

    def whereOrAnd = if (wav) " AND " else {wav = true; " WHERE "}

    class DBFuncWrapper(dbFunc: Box[String]) {
      def apply(field: String) = dbFunc match {
        case Full(f) => f+"("+field+")"
        case _ => field
      }
    }

    implicit def dbfToFunc(in: Box[String]): DBFuncWrapper = new DBFuncWrapper(in)

    by match {
      case Nil => what
      case x :: xs => {
          var updatedWhat = what
          x match {
            case Cmp(field, opr, Full(_), _, dbFunc) =>
              (1 to field.dbColumnCount).foreach {
                cn =>
                updatedWhat = updatedWhat + whereOrAnd + dbFunc(MapperRules.quoteColumnName.vend(field.dbColumnNames(field.name)(cn - 1)))+" "+opr+" ? "
              }

            case Cmp(field, opr, _, Full(otherField), dbFunc) =>
              (1 to field.dbColumnCount).foreach {
                cn =>
                updatedWhat = updatedWhat + whereOrAnd + dbFunc(MapperRules.quoteColumnName.vend(field.dbColumnNames(field.name)(cn - 1)))+" "+opr+" "+
                MapperRules.quoteColumnName.vend(otherField.dbColumnNames(otherField.name)(cn - 1))
              }

            case Cmp(field, opr, Empty, Empty, dbFunc) =>
              (1 to field.dbColumnCount).foreach (cn => updatedWhat = updatedWhat + whereOrAnd + dbFunc(MapperRules.quoteColumnName.vend(field.dbColumnNames(field.name)(cn - 1)))+" "+opr+" ")

              // For vals, add "AND $fieldname = ? [OR $fieldname = ?]*" to the query. The number
              // of fields you add onto the query is equal to vals.length
            case ByList(field, orgVals) =>
              val vals = Set(orgVals :_*).toList // faster than list.removeDuplicates

              if (vals.isEmpty) updatedWhat = updatedWhat + whereOrAnd + " 0 = 1 "
              else updatedWhat = updatedWhat +
              vals.map(v => MapperRules.quoteColumnName.vend(field._dbColumnNameLC)+ " = ?").mkString(whereOrAnd+" (", " OR ", ")")

            case in: InRaw[A, _] =>
              updatedWhat = updatedWhat + whereOrAnd + (in.rawSql match {
                  case null | "" => " 0 = 1 "
                  case sql => " "+MapperRules.quoteColumnName.vend(in.field._dbColumnNameLC)+" IN ( "+sql+" ) "
                })

            case (in: InThing[A]) =>
              updatedWhat = updatedWhat + whereOrAnd +
              MapperRules.quoteColumnName.vend(in.outerField._dbColumnNameLC)+in.inKeyword+
              "("+in.innerMeta.addEndStuffs(in.innerMeta.addFields("SELECT "+
                                                                       in.distinct+
                                                                       MapperRules.quoteColumnName.vend(in.innerField._dbColumnNameLC)+
                                                                       " FROM "+
                                                                       MapperRules.quoteTableName.vend(in.innerMeta._dbTableNameLC)+" ",false,
                                                                       in.queryParams, conn), in.queryParams, conn)._1+" ) "

              // Executes a subquery with {@code query}
            case BySql(query, _,  _*) =>
              updatedWhat = updatedWhat + whereOrAnd + " ( "+ query +" ) "
            case _ =>
          }
          addFields(updatedWhat, wav, xs, conn)
        }
    }
  }


  private[mapper] def setStatementFields(st: PreparedStatement, by: List[QueryParam[A]], curPos: Int, conn: SuperConnection): Int = {
    by match {
      case Nil => curPos
      case Cmp(field, _, Full(value), _, _) :: xs =>
        setPreparedStatementValue(conn, st, curPos, field, field.targetSQLType, field.convertToJDBCFriendly(value), objectSetterFor(field))
        setStatementFields(st, xs, curPos + 1, conn)

      case ByList(field, orgVals) :: xs => {
        val vals = Set(orgVals :_*).toList
        var newPos = curPos
        vals.foreach(v => {
          setPreparedStatementValue(conn, st, newPos, field, field.targetSQLType, field.convertToJDBCFriendly(v), objectSetterFor(field))
          newPos = newPos + 1
        })

        setStatementFields(st, xs, newPos, conn)
      }

      case (in: InThing[A]) :: xs =>
        val newPos = in.innerMeta.setStatementFields(st, in.queryParams,
                                                     curPos, conn)
        setStatementFields(st, xs, newPos, conn)

      case BySql(query, who, params @ _*) :: xs => {
          params.toList match {
            case Nil => setStatementFields(st, xs, curPos, conn)
            case List(i: Int) =>
              st.setInt(curPos, i)
              setStatementFields(st, xs, curPos + 1, conn)
            case List(lo: Long) =>
              st.setLong(curPos, lo)
              setStatementFields(st, xs, curPos + 1, conn)
            case List(s: String) =>
              st.setString(curPos, s)
              setStatementFields(st, xs, curPos + 1, conn)
              // Allow specialization of time-related values based on the input parameter
            case List(t: java.sql.Timestamp) =>
              st.setTimestamp(curPos, t)
              setStatementFields(st, xs, curPos + 1, conn)
            case List(d: java.sql.Date) =>
              st.setDate(curPos, d)
              setStatementFields(st, xs, curPos + 1, conn)
            case List(t: java.sql.Time) =>
              st.setTime(curPos, t)
              setStatementFields(st, xs, curPos + 1, conn)
              // java.util.Date goes last, since it's a superclass of java.sql.{Date,Time,Timestamp}
            case List(d: Date) =>
              st.setTimestamp(curPos, new java.sql.Timestamp(d.getTime))
              setStatementFields(st, xs, curPos + 1, conn)
            case List(field: BaseMappedField) =>
              setPreparedStatementValue(conn, st, curPos, field, field.targetSQLType, field.jdbcFriendly, objectSetterFor(field))
              setStatementFields(st, xs, curPos + 1, conn)
            case p :: ps =>
              setStatementFields(st, BySql[A](query, who, p) :: BySql[A](query, who, ps: _*) :: xs, curPos, conn)
          }
        }
      case _ :: xs => {
          setStatementFields(st, xs, curPos, conn)
        }
    }
  }

  // def find(by: QueryParam): Box[A] = find(List(by))

  private def _addOrdering(in: String, params: List[QueryParam[A]]): String = {
    params.flatMap{
      case OrderBy(field, order, nullOrder) => List(MapperRules.quoteColumnName.vend(field._dbColumnNameLC)+" "+order.sql+" "+(nullOrder.map(_.getSql).openOr("")))
      case OrderBySql(sql, _) => List(sql)
      case _ => Nil
    } match {
      case Nil => in
      case xs => in + " ORDER BY "+xs.mkString(" , ")
    }
  }

  protected def addEndStuffs(in: String, params: List[QueryParam[A]], conn: SuperConnection): (String, Box[Long], Box[Long]) = {
    val tmp = _addOrdering(in, params)
    val max = params.foldRight(Empty.asInstanceOf[Box[Long]]){(a,b) => a match {case MaxRows(n) => Full(n); case _ => b}}
    val start = params.foldRight(Empty.asInstanceOf[Box[Long]]){(a,b) => a match {case StartAt(n) => Full(n); case _ => b}}

    if (conn.brokenLimit_?) (tmp, start, max) else {
      val ret = (max, start) match {
        case (Full(max), Full(start)) => tmp + " LIMIT "+max+" OFFSET "+start
        case (Full(max), _) => tmp + " LIMIT "+max
        case (_, Full(start)) => tmp + " LIMIT "+conn.driverType.maxSelectLimit+" OFFSET "+start
        case _ => tmp
      }
      (ret, Empty, Empty)
    }
  }

  def delete_!(toDelete : A): Boolean =
  toDelete match {
    case x: MetaMapper[_] => throw new MapperException("Cannot delete the MetaMapper singleton")

    case _ =>
      thePrimaryKeyField.map(im =>
        DB.use(toDelete.connectionIdentifier) {
          conn =>
          _beforeDelete(toDelete)
          val ret = DB.prepareStatement("DELETE FROM "+MapperRules.quoteTableName.vend(_dbTableNameLC) +" WHERE "+im+" = ?", conn) {
            st =>
            val indVal = indexedField(toDelete)
            indVal.map{indVal =>
              setPreparedStatementValue(conn, st, 1, indVal, im, objectSetterFor(indVal))
              st.executeUpdate == 1
            } openOr false
          }
          _afterDelete(toDelete)
          ret
        }
      ).openOr(false)
  }



  type AnyBound = T forSome {type T}

  private[mapper] def ??(meth: Method, inst: A) = meth.invoke(inst).asInstanceOf[MappedField[AnyBound, A]]

  def dirty_?(toTest: A): Boolean = mappedFieldList.exists(
    mft =>
    ??(mft.method, toTest).dirty_?
  )

  def indexedField(toSave: A): Box[MappedField[Any, A]] =
  thePrimaryKeyField.map(im => ??(mappedColumns(im.toLowerCase), toSave))

  def saved_?(toSave: A): Boolean =
  toSave match {
    case x: MetaMapper[_] => throw new MapperException("Cannot test the MetaMapper singleton for saved status")

    case _ => toSave.persisted_?
  }

  /**
   * This method will update the instance from JSON.  It allows for
   * attacks from untrusted JSON as it bypasses normal security.  By
   * default, the method is protected.  You can write a proxy method
   * to expose the functionality.
   */
  protected def updateFromJSON_!(toUpdate: A, json: JsonAST.JObject): A = {
    import JsonAST._

    toUpdate.runSafe {

      for {
        field <- json.obj
        meth <- _mappedFields.get(field.name)
      } {
        val f = ??(meth, toUpdate)
        f.setFromAny(field.value)
      }
    }

    toUpdate
  }

  /**
   * This method will encode the instance as JSON.  It may reveal
   * data in fields that might otherwise be proprietary.  It should
   * be used with caution and only exposed as a public method
   * after a security review.
   */
  protected def encodeAsJSON_! (toEncode: A): JsonAST.JObject = {
    toEncode.runSafe {
      JsonAST.JObject(JsonAST.JField("$persisted",
				     JsonAST.JBool(toEncode.persisted_?)) ::
		      this.mappedFieldList.
		      flatMap(fh => ??(fh.method, toEncode).asJsonField))
    }
  }

  /**
   * Decode the fields from a JSON Object.  Should the fields be marked as dirty?
   */
  protected def decodeFromJSON_!(json: JsonAST.JObject, markFieldsAsDirty: Boolean): A = {
    val ret: A = createInstance
    import JsonAST._

    ret.runSafe {
      json.findField {
        case JField("$persisted", JBool(per)) =>
          ret.persisted_? = per
          true
        case _ => false
      }

      for {
        field <- json.obj
        meth <- _mappedFields.get(field.name)
      } {
        val f = ??(meth, ret)
        f.setFromAny(field.value)
        if (!markFieldsAsDirty) f.resetDirty
      }
    }

    ret
  }


  def whatToSet(toSave : A) : String = {
    mappedColumns.filter{c => ??(c._2, toSave).dirty_?}.map{c => c._1 + " = ?"}.toList.mkString("", ",", "")
  }

  /**
   * Run the list of field validations, etc.  This is the raw validation,
   * without the notifications.  This method can be over-ridden.
   */
  protected def runValidationList(toValidate: A): List[FieldError] =
  mappedFieldList.flatMap(f => ??(f.method, toValidate).validate) :::
  validation.flatMap{
    case pf: PartialFunction[A, List[FieldError]] =>
      if (pf.isDefinedAt(toValidate)) pf(toValidate)
      else Nil

    case f => f(toValidate)
  }

  final def validate(toValidate: A): List[FieldError] = {
    logger.debug("Validating dbName=%s, entity=%s".format(dbName, toValidate))
    val saved_? = this.saved_?(toValidate)
    _beforeValidation(toValidate)
    if (saved_?) _beforeValidationOnUpdate(toValidate) else _beforeValidationOnCreate(toValidate)

    val ret: List[FieldError] = runValidationList(toValidate)

    _afterValidation(toValidate)
    if (saved_?) _afterValidationOnUpdate(toValidate) else _afterValidationOnCreate(toValidate)

    logger.debug("Validated dbName=%s, entity=%s, result=%s".format(dbName, toValidate, ret))

    ret
  }

  val elemName = getClass.getSuperclass.getName.split("\\.").toList.last

  def toXml(what: A): Elem =
  Elem(null,elemName,
       mappedFieldList.foldRight[MetaData](Null) {(p, md) => val fld = ??(p.method, what)
                                                  new UnprefixedAttribute(p.name, Text(fld.toString), md)}
       ,TopScope, true)

  /**
   * Returns true if none of the fields are dirty
   */
  def clean_?(toCheck: A): Boolean = mappedColumns.foldLeft(true)((bool, ptr) => bool && !(??(ptr._2, toCheck).dirty_?))

  /**
   * Sets a prepared statement value based on the given MappedField's value
   * and column name. This delegates to the BaseMappedField overload of
   * setPreparedStatementValue by retrieving the necessary values.
   *
   * @param conn The connection for this prepared statement
   * @param st The prepared statement
   * @param index The index for this prepared statement value
   * @param field The field corresponding to this prepared statement value
   * @param columnName The column name to use to retrieve the type and value
   * @param setObj A function that we can delegate to for setObject calls
   */
  private def setPreparedStatementValue(conn: SuperConnection,
                                        st: PreparedStatement,
                                        index: Int,
                                        field: MappedField[_, A],
                                        columnName : String,
                                        setObj : (PreparedStatement, Int, AnyRef, Int) => Unit): Unit = {
    setPreparedStatementValue(conn, st, index, field,
                              field.targetSQLType(columnName),
                              field.jdbcFriendly(columnName),
                              setObj)
  }

  /**
   * Sets a prepared statement value based on the given BaseMappedField's type and value. This
   * allows us to do special handling based on the type in a central location.
   *
   * @param conn The connection for this prepared statement
   * @param st The prepared statement
   * @param index The index for this prepared statement value
   * @param field The field corresponding to this prepared statement value
   * @param columnType The JDBC SQL Type for this value
   * @param value The value itself
   * @param setObj A function that we can delegate to for setObject calls
   */
  private def setPreparedStatementValue(conn: SuperConnection,
                                        st: PreparedStatement,
                                        index: Int,
                                        field: BaseMappedField,
                                        columnType : Int,
                                        value : Object,
                                        setObj : (PreparedStatement, Int, AnyRef, Int) => Unit): Unit = {
    // Remap the type if the driver wants
    val mappedColumnType = conn.driverType.columnTypeMap(columnType)

    // We generally use setObject for everything, but we've found some broken JDBC drivers
    // which has prompted us to use type-specific handling for certain types
    mappedColumnType match {
      case Types.VARCHAR => 
        // Set a string with a simple guard for null values
        st.setString(index, if (value ne null) value.toString else value.asInstanceOf[String])

      // Sybase SQL Anywhere and DB2 choke on using setObject for boolean data
      case Types.BOOLEAN => value match {
        case intData : java.lang.Integer => st.setBoolean(index, intData.intValue != 0)
        case b : java.lang.Boolean => st.setBoolean(index, b.booleanValue)
        // If we can't figure it out, maybe the driver can
        case other => setObj(st, index, other, mappedColumnType) 
      }

      // In all other cases, delegate to the driver
      case _ => setObj(st, index, value, mappedColumnType)
    }
  }

  /**
   * This is a utility method to simplify using setObject. It's intended use is to
   * generate a setObject proxy so that the intermediate code doesn't need to be aware
   * of drivers that ignore column types.
   */
  private def objectSetterFor(field : BaseMappedField) = {
    (st : PreparedStatement, index : Int, value : AnyRef, columnType : Int) => {
      if (field.dbIgnoreSQLType_?) {
        st.setObject(index, value)
      } else {
        st.setObject(index, value, columnType)
      }
    }
  }

  def save(toSave: A): Boolean = {
    toSave match {
      case x: MetaMapper[_] => throw new MapperException("Cannot save the MetaMapper singleton")

      case _ =>
        logger.debug("Saving dbName=%s, entity=%s".format(dbName, toSave))
        /**
         * @return true if there was exactly one row in the result set, false if not.
         */
        def runAppliers(rs: ResultSet) : Boolean = {
          try {
            if (rs.next) {
              val meta = rs.getMetaData
              toSave.runSafe {
                for {
                  indexMap <- thePrimaryKeyField
                  auto <- primaryKeyAutogenerated if auto
                } {
                  findApplier(indexMap, rs.getObject(1)) match {
                    case Full(ap) => ap.apply(toSave, rs.getObject(1))
                    case _ =>
                  }
                }
              }
              !rs.next
            } else false
          } finally {
            rs.close
          }
        }

        /**
         * Checks whether the result set has exactly one row.
         */
        def hasOneRow(rs: ResultSet) : Boolean = {
          try {
            val firstRow = rs.next
            (firstRow && !rs.next)
          } finally {
            rs.close
          }
        }

        if (saved_?(toSave) && clean_?(toSave)) true else {
          val ret = DB.use(toSave.connectionIdentifier) {
            conn =>
            _beforeSave(toSave)
            val ret = if (saved_?(toSave)) {
              _beforeUpdate(toSave)
              val ret: Boolean = if (!dirty_?(toSave)) true else {
                val ret: Boolean = DB.prepareStatement("UPDATE "+MapperRules.quoteTableName.vend(_dbTableNameLC)+" SET "+whatToSet(toSave)+" WHERE "+thePrimaryKeyField.openOrThrowException("Cross your fingers") +" = ?", conn) {
                  st =>
                  var colNum = 1

                  // Here we apply each column's value to the prepared statement
                  for (col <- mappedColumns) {
                    val colVal = ??(col._2, toSave)
                    if (!columnPrimaryKey_?(col._1) && colVal.dirty_?) {
                      setPreparedStatementValue(conn, st, colNum, colVal, col._1, objectSetterFor(colVal))
                      colNum = colNum + 1
                    }
                  }

                  for {
                    indVal <- indexedField(toSave)
                    indexColumnName <- thePrimaryKeyField
                  } {
                    setPreparedStatementValue(conn, st, colNum, indVal, indexColumnName, objectSetterFor(indVal))
                  }

                  st.executeUpdate
                  true
                }
                ret
              }
              _afterUpdate(toSave)
              ret
            } else {
              _beforeCreate(toSave)

              val query = "INSERT INTO "+MapperRules.quoteTableName.vend(_dbTableNameLC)+" ("+columnNamesForInsert+") VALUES ("+columnQueriesForInsert+")"

              def prepStat(st : PreparedStatement): Unit = {
                var colNum = 1

                for (col <- mappedColumns) {
                  if (!columnPrimaryKey_?(col._1)) {
                    val colVal = col._2.invoke(toSave).asInstanceOf[MappedField[AnyRef, A]]
                    setPreparedStatementValue(conn, st, colNum, colVal, col._1, objectSetterFor(colVal))
                    colNum = colNum + 1
                  }
                }
              }

              // Figure out which columns are auto-generated
              val generatedColumns = (mappedColumnInfo.filter(_._2.dbAutogenerated_?).map(_._1)).toList

              val ret = conn.driverType.performInsert(conn, query, prepStat, MapperRules.quoteTableName.vend(_dbTableNameLC), generatedColumns) {
                case Right(count) => count == 1
                case Left(rs) => runAppliers(rs)
              }

              _afterCreate(toSave)
              toSave.persisted_? = true
              ret
            }
            _afterSave(toSave)
            ret
          }

          // clear dirty and get rid of history
          for (col <- mappedColumns) {
            val colVal = ??(col._2, toSave)
            if (!columnPrimaryKey_?(col._1) && colVal.dirty_?) {
              colVal.resetDirty
              colVal.doneWithSave
            }
          }

          ret
        }
    }
  }

  /**
   * This method returns true if the named column is the primary key and
   * it is autogenerated
   */
  def columnPrimaryKey_?(name: String) = mappedColumnInfo.get(name).map(c => (c.dbPrimaryKey_? && c.dbAutogenerated_?)) getOrElse false

  def createInstances(dbId: ConnectionIdentifier, rs: ResultSet, start: Box[Long], omax: Box[Long]) : List[A] = createInstances(dbId, rs, start, omax, v => Full(v))


  def createInstances[T](dbId: ConnectionIdentifier, rs: ResultSet, start: Box[Long], omax: Box[Long], f: A => Box[T]) : List[T] = {
    var ret = new ListBuffer[T]
    val bm = buildMapper(rs)
    var pos = (start openOr 0L) * -1L
    val max = omax openOr java.lang.Long.MAX_VALUE

    while (pos < max && rs.next()) {
      if (pos >= 0L) {
        f(createInstance(dbId, rs, bm)).foreach(v => ret += v)
      }
      pos = pos + 1L
    }

    ret.toList
  }

  def appendFieldToStrings(in: A): String = mappedFieldList.map(p => ??(p.method, in).asString).mkString(",")

  private val columnNameToMappee = new HashMap[String, Box[(ResultSet, Int, A) => Unit]]

  def buildMapper(rs: ResultSet): List[Box[(ResultSet,Int,A) => Unit]] = columnNameToMappee.synchronized {
    val meta = rs.getMetaData
    val colCnt = meta.getColumnCount
    for {
      pos <- (1 to colCnt).toList
      colName = meta.getColumnName(pos).toLowerCase
    } yield
      columnNameToMappee.get(colName) match {
        case None =>
          val colType = meta.getColumnType(pos)

          Box(mappedColumns.get(colName)).flatMap{
            fieldInfo =>
            val setTo = {
              val tField = fieldInfo.invoke(this).asInstanceOf[MappedField[AnyRef, A]]

              Some(colType match {
                  case Types.INTEGER | Types.BIGINT => {
                      val bsl = tField.buildSetLongValue(fieldInfo, colName)
                      (rs: ResultSet, pos: Int, objInst: A) => bsl(objInst, rs.getLong(pos), rs.wasNull)}
                  case Types.VARCHAR => {
                      val bsl = tField.buildSetStringValue(fieldInfo, colName)
                      (rs: ResultSet, pos: Int, objInst: A) => bsl(objInst, rs.getString(pos))}
                  case Types.DATE | Types.TIME | Types.TIMESTAMP =>
                    val bsl = tField.buildSetDateValue(fieldInfo, colName)
                    (rs: ResultSet, pos: Int, objInst: A) => bsl(objInst, rs.getTimestamp(pos))
                  case Types.BOOLEAN | Types.BIT =>{
                      val bsl = tField.buildSetBooleanValue(fieldInfo, colName)
                      (rs: ResultSet, pos: Int, objInst: A) => bsl(objInst, rs.getBoolean(pos), rs.wasNull)}
                  case _ => {
                      (rs: ResultSet, pos: Int, objInst: A) => {
                        val res = rs.getObject(pos)
                        findApplier(colName, res).foreach(f => f(objInst, res))
                      }
                    }
                })
            }

            columnNameToMappee(colName) = Box(setTo)
            setTo
          }

        case Some(of) => of
      }
  }

  def createInstance(dbId: ConnectionIdentifier, rs : ResultSet, mapFuncs: List[Box[(ResultSet,Int,A) => Unit]]) : A = {
    val ret: A = createInstance.connectionIdentifier(dbId)

    ret.persisted_? = true

    for {
      (fb, pos) <- mapFuncs.zipWithIndex
      f <- fb
    } f(rs, pos + 1, ret)

    ret
  }

  protected def  findApplier(name: String, inst: AnyRef): Box[((A, AnyRef) => Unit)] = synchronized {
    val clz = inst match {
      case null => null
      case _ => inst.getClass.asInstanceOf[Class[(C forSome {type C})]]
    }
    val look = (name.toLowerCase, if (clz ne null) Full(clz) else Empty)
    Box(mappedAppliers.get(look) orElse {
        val newFunc = createApplier(name, inst)
        mappedAppliers(look) = newFunc
        Some(newFunc)
      })
  }


  private def createApplier(name : String, inst : AnyRef /*, clz : Class*/) : (A, AnyRef) => Unit = {
    val accessor = mappedColumns.get(name) orElse mappedColumns.get(name.toLowerCase)
    if ((accessor eq null) || accessor == None) {
      null
    } else {
      (accessor.get.invoke(this).asInstanceOf[MappedField[AnyRef, A]]).buildSetActualValue(accessor.get, inst, name)
    }
  }

  /**
   * A set of CssSels that can be used to bind this MetaMapper's fields.
   *
   * Elements with a class matching the field name are mapped to the NodeSeq
   * produced by the fieldHtml function that is passed in.
   *
   * So, with a MetaMapper that has three fields, name, date, and description,
   * the resulting CSS selector transforms are:
   *
   * {{{
   * Seq(
   *   ".name" #> fieldHtml(-name field-),
   *   ".date" #> fieldHtml(-date field-),
   *   ".description" #> fieldHtml(-description field-)
   * )
   * }}}
   *
   * Above, -name field-, -date field-, and -description field- refer to the
   * actual MappedField objects for those fields.
   */
  def fieldMapperTransforms(fieldHtml: (BaseOwnedMappedField[A]=>NodeSeq), mappedObject: A): Seq[CssSel] = {
    mappedFieldList.map { field =>
      s".${field.name}" #> fieldHtml(??(field.method, mappedObject))
    }
  }

  private[mapper] def checkFieldNames(in: A): Unit = {
    mappedFieldList.foreach(f =>
      ??(f.method, in) match {
        case field if (field.i_name_! eq null) => field.setName_!(f.name)
        case _ =>
      })
  }

  /**
   * Get a field by the field name
   * @param fieldName -- the name of the field to get
   * @param actual -- the instance to get the field on
   *
   * @return Box[The Field] (Empty if the field is not found)
   */
  def fieldByName[T](fieldName: String, actual: A): Box[MappedField[T, A]] =
  Box(_mappedFields.get(fieldName)).
  map(meth => ??(meth, actual).asInstanceOf[MappedField[T,A]])

  /**
   * A partial function that takes an instance of A and a field name and returns the mapped field
   */
  lazy val fieldMatcher: PartialFunction[(A, String), MappedField[Any, A]] = {
    case (actual, fieldName) if _mappedFields.contains(fieldName) => fieldByName[Any](fieldName, actual).openOrThrowException("we know this is defined")
  }

  def createInstance: A = rootClass.newInstance.asInstanceOf[A]

  def fieldOrder: List[BaseOwnedMappedField[A]] = Nil

  protected val rootClass = this.getClass.getSuperclass

  private val mappedAppliers = new HashMap[(String, Box[Class[(C forSome {type C})]]), (A, AnyRef) => Unit];

  private val _mappedFields  = new HashMap[String, Method];

  private[mapper] var mappedFieldList: List[FieldHolder] = Nil; // new Array[Triple[String, Method, MappedField[Any,Any]]]();

  private var mappedCallbacks: List[(String, Method)] = Nil

  private var mappedColumns: SortedMap[String, Method] = TreeMap()

  private var mappedColumnInfo: SortedMap[String, MappedField[AnyRef, A]] = TreeMap()


  /**
   * The primary key column.  This used to be indexMap
   */
  private var thePrimaryKeyField: Box[String] = Empty

  /**
   * If the primary key field is autogenerated, this will be Full(true)
   */
  private var primaryKeyAutogenerated: Box[Boolean] = Empty

  this.runSafe {
    logger.debug("Initializing MetaMapper for %s".format(internalTableName_$_$))
    val tArray = new ListBuffer[FieldHolder]
    def isLifecycle(m: Method) = classOf[LifecycleCallbacks].isAssignableFrom(m.getReturnType)

    val mapperAccessMethods = new FieldFinder[MappedField[_,_]](this, logger).accessorMethods

    mappedCallbacks = mapperAccessMethods.filter(isLifecycle).map(v => (v.getName, v))

    for (v <- mapperAccessMethods) {
      v.invoke(this) match {
        case untypedMf: MappedField[_, _] if !untypedMf.ignoreField_? =>
          val mf = untypedMf.asInstanceOf[MappedField[AnyRef,A]]

          mf.setName_!(v.getName)
          tArray += FieldHolder(mf.name, v, mf)
          for (colName <- mf.dbColumnNames(v.getName).map(MapperRules.quoteColumnName.vend).map(_.toLowerCase)) {
            mappedColumnInfo += colName -> mf
            mappedColumns += colName -> v
          }
          if (mf.dbPrimaryKey_?) {
            thePrimaryKeyField = Full(MapperRules.quoteColumnName.vend(mf._dbColumnNameLC))
            primaryKeyAutogenerated = Full(mf.dbAutogenerated_?)
          }

        case _ =>
      }
    }

    def findPos(in: AnyRef): Box[Int] = {
      tArray.toList.zipWithIndex.filter(mft => in eq mft._1.field) match {
        case Nil => Empty
        case x :: xs => Full(x._2)
      }
    }

    val resArray = new ListBuffer[FieldHolder];

    fieldOrder.foreach(f => findPos(f).foreach(pos => resArray += tArray.remove(pos)))

    tArray.foreach(mft => resArray += mft)

    mappedFieldList = resArray.toList
    mappedFieldList.foreach(ae => _mappedFields(ae.name) = ae.method)

    logger.trace("Mapped fields for %s: %s".format(dbName, mappedFieldList.map(_.name).mkString(",")))
  }

  val columnNamesForInsert = (mappedColumnInfo.filter(c => !(c._2.dbPrimaryKey_? && c._2.dbAutogenerated_?)).map(_._1)).toList.mkString(",")

  val columnQueriesForInsert = {
    (mappedColumnInfo.filter(c => !(c._2.dbPrimaryKey_? && c._2.dbAutogenerated_?)).map(p => "?")).toList.mkString(",")
  }

  private def fixTableName(name: String) = {
    val tableName = MapperRules.tableName(connectionIdentifier,clean(name))

    if (DB.reservedWords.contains(tableName.toLowerCase))
      tableName+"_t"
    else
      tableName
  }

  private def internalTableName_$_$ = getClass.getSuperclass.getName.split("\\.").toList.last;

  /**
   * This function converts a header name into the appropriate
   * XHTML format for displaying across the headers of a
   * formatted block.  The default is &lt;th&gt; for use
   * in XHTML tables.  If you change this function, the change
   * will be used for this MetaMapper unless you override the
   * htmlHeades method
   */
  var displayNameToHeaderElement: String => NodeSeq = MapperRules.displayNameToHeaderElement

  def htmlHeaders: NodeSeq =
  mappedFieldList.filter(_.field.dbDisplay_?).
  flatMap(mft => displayNameToHeaderElement(mft.field.displayName))

  /**
  * The mapped fields
  */
  lazy val mappedFields: Seq[BaseMappedField] = mappedFieldList.map(f => f.field)

  /**
   * the mapped fields as MappedField rather than BaseMappedField
   */
  lazy val mappedFieldsForModel: List[MappedField[_, A]] = mappedFieldList.map(_.field)

  /**
   * This function converts an element into the appropriate
   * XHTML format for displaying across a line
   * formatted block.  The default is &lt;td&gt; for use
   * in XHTML tables.  If you change this function, the change
   * will be used for this MetaMapper unless you override the
   * doHtmlLine method.
   */
  var displayFieldAsLineElement: NodeSeq => NodeSeq =
  MapperRules.displayFieldAsLineElement


  def doHtmlLine(toLine: A): NodeSeq =
  mappedFieldList.filter(_.field.dbDisplay_?).
  flatMap(mft => displayFieldAsLineElement(??(mft.method, toLine).asHtml))

  def asJs(actual: A): JsExp = {
    JE.JsObj(("$lift_class", JE.Str(dbTableName)) :: mappedFieldList.
             map(f => ??(f.method, actual)).filter(_.renderJs_?).flatMap(_.asJs).toList :::
             actual.suplementalJs(Empty) :_*)
  }

  /**
   * Get a list of all the fields
   * @return a list of all the fields
   */
  lazy val doAllFieldNames: Seq[(String, SourceFieldMetadata)] =
  mappedFieldList.map(fh => fh.name.toLowerCase -> fh.field.sourceInfoMetadata())

  /**
   * Get a list of all the fields as a map
   * @return a list of all the fields
   */
  lazy val fieldNamesAsMap: Map[String, SourceFieldMetadata] = Map(doAllFieldNames :_*)

  def asHtml(toLine: A): NodeSeq =
  Text(internalTableName_$_$) :: Text("={ ") ::
    (for {
      mft <- mappedFieldList if mft.field.dbDisplay_?
      field = ??(mft.method, toLine)
    } yield {
     <span>{field.displayName}={field.asHtml}&nbsp;</span>
    }) ::: List(Text(" }"))


  /**
   * This function converts a name and form for a given field in the
   * model to XHTML for presentation in the browser.  By
   * default, a table row ( &lt;tr&gt; ) is presented, but
   * you can change the function to display something else.
   */
  var formatFormElement: (NodeSeq, NodeSeq) => NodeSeq =
  MapperRules.formatFormElement

  def formatFormLine(displayName: NodeSeq, form: NodeSeq): NodeSeq =
  formatFormElement(displayName, form)

  def toForm(toMap: A): NodeSeq =
  mappedFieldList.map(e => ??(e.method, toMap)).
  filter(f => f.dbDisplay_? && f.dbIncludeInForm_?).flatMap (
    field =>
    field.toForm.toList.
    flatMap(form => formatFormLine(Text(field.displayName), form))
  )

  /**
   * Present the model as a HTML using the same formatting as toForm
   *
   * @param toMap the instance to generate the HTML for
   *
   * @return the html view of the model
   */
  def toHtml(toMap: A): NodeSeq =
  mappedFieldList.map(e => ??(e.method, toMap)).
  filter(f => f.dbDisplay_?).flatMap (
    field =>
    formatFormLine(Text(field.displayName), field.asHtml)
  )

  /**
   * Get the fields (in order) for displaying a form
   */
  def formFields(toMap: A): List[MappedField[_, A]] =
  mappedFieldList.map(e => ??(e.method, toMap)).filter(f => f.dbDisplay_? &&
                                                       f.dbIncludeInForm_?)


  /**
   * map the fields titles and forms to generate a list
   * @param func called with displayHtml, fieldId, form
   */
  def mapFieldTitleForm[T](toMap: A,
                           func: (NodeSeq, Box[NodeSeq], NodeSeq) => T): List[T] =
  formFields(toMap).flatMap(field => field.toForm.
                            map(fo => func(field.displayHtml, field.fieldId, fo)))


  /**
   * flat map the fields titles and forms to generate a list
   * @param func called with displayHtml, fieldId, form
   */
  def flatMapFieldTitleForm[T](toMap: A,
                               func: (NodeSeq, Box[NodeSeq], NodeSeq) => Seq[T]): List[T] =
  formFields(toMap).flatMap(field => field.toForm.toList.
                            flatMap(fo => func(field.displayHtml,
                                               field.fieldId, fo)))

/**
   * flat map the fields titles and forms to generate a list
   * @param func called with displayHtml, fieldId, form
   */
  def flatMapFieldTitleForm2[T](toMap: A,
                               func: (NodeSeq, MappedField[_, A], NodeSeq) => Seq[T]): List[T] =
  formFields(toMap).flatMap(field => field.toForm.toList.
                            flatMap(fo => func(field.displayHtml,
                                               field, fo)))


  /**
   * Given the prototype field (the field on the Singleton), get the field from the instance
   * @param actual -- the Mapper instance
   * @param protoField -- the field from the MetaMapper (Singleton)
   *
   * @return the field from the actual object
   */
  def getActualField[T](actual: A, protoField: MappedField[T, A]): MappedField[T, A] =
  ??(_mappedFields(protoField.name), actual).asInstanceOf[MappedField[T,A]]


  /**
   * Given the prototype field (the field on the Singleton), get the field from the instance
   * @param actual -- the Mapper instance
   * @param protoField -- the field from the MetaMapper (Singleton)
   *
   * @return the field from the actual object
   */
  def getActualBaseField(actual: A, protoField: BaseOwnedMappedField[A]): BaseOwnedMappedField[A] =
  ??(_mappedFields(protoField.name), actual) // .asInstanceOf[MappedField[T,A]]

  /**
   * The name of the database table.  Override this method if you
   * want to change the table to something other than the name of the Mapper class
   */
  def dbTableName = internal_dbTableName

  /**
   * The name of the mapped object
   */
  override def dbName: String = internalTableName_$_$

  /**
   * The table name, to lower case... ensures that it works on all DBs
   */
  final def _dbTableNameLC = {
    val name = dbTableName

    val conn = DB.currentConnection
    if (conn.isDefined) {
      val rc = conn.openOrThrowException("We just checked that this is a Full Box")
      if (rc.metaData.storesMixedCaseIdentifiers) name
      else name.toLowerCase
    } else name
  }  // dbTableName.toLowerCase

  private[mapper] lazy val internal_dbTableName = fixTableName(internalTableName_$_$)

  private def setupInstanceForPostCommit(inst: A): Unit = {
    afterCommit match {
      case Nil =>
        // If there's no post-commit functions, then don't
        // record (and retain) the instance

      case pcf =>
        if (!inst.addedPostCommit) {
          DB.appendPostTransaction(inst.connectionIdentifier, dontUse =>  (clearPCFunc :: pcf).foreach(_(inst)))
          inst.addedPostCommit = true
        }
    }
  }

  private def eachField(what: A, toRun: List[(A) => Any])(f: (LifecycleCallbacks) => Any): Unit = {
    mappedCallbacks.foreach (e =>
      e._2.invoke(what) match {
        case lccb: LifecycleCallbacks => f(lccb)
        case _ =>
      })
    toRun.foreach{tf => tf(what)}
  }
  private def _beforeValidation(what: A): Unit = {setupInstanceForPostCommit(what); eachField(what, beforeValidation) { field => field.beforeValidation}  }
  private def _beforeValidationOnCreate(what: A): Unit = {eachField(what, beforeValidationOnCreate) { field => field.beforeValidationOnCreate}  }
  private def _beforeValidationOnUpdate(what: A): Unit = {eachField(what, beforeValidationOnUpdate) { field => field.beforeValidationOnUpdate}  }
  private def _afterValidation(what: A): Unit = { eachField(what, afterValidation) { field => field.afterValidation}  }
  private def _afterValidationOnCreate(what: A): Unit = {eachField(what, afterValidationOnCreate) { field => field.afterValidationOnCreate}  }
  private def _afterValidationOnUpdate(what: A): Unit = {eachField(what, afterValidationOnUpdate) { field => field.afterValidationOnUpdate}  }

  private def _beforeSave(what: A): Unit = {setupInstanceForPostCommit(what); eachField(what, beforeSave) { field => field.beforeSave}  }
  private def _beforeCreate(what: A): Unit = { eachField(what, beforeCreate) { field => field.beforeCreate}  }
  private def _beforeUpdate(what: A): Unit = { eachField(what, beforeUpdate) { field => field.beforeUpdate}  }

  private def _afterSave(what: A): Unit = {eachField(what, afterSave) { field => field.afterSave}  }
  private def _afterCreate(what: A): Unit = {eachField(what, afterCreate) { field => field.afterCreate}  }
  private def _afterUpdate(what: A): Unit = {eachField(what, afterUpdate) { field => field.afterUpdate}  }

  private def _beforeDelete(what: A): Unit = {setupInstanceForPostCommit(what); eachField(what, beforeDelete) { field => field.beforeDelete}  }
  private def _afterDelete(what: A): Unit = {eachField(what, afterDelete) { field => field.afterDelete}  }

  def beforeSchemifier: Unit = {}
  def afterSchemifier: Unit = {}

  def dbIndexes: List[BaseIndex[A]] = Nil

  implicit def fieldToItem[T](in: MappedField[T, A]): IndexItem[A] = IndexField(in)
  implicit def boundedFieldToItem(in: (MappedField[String, A], Int)): BoundedIndexField[A] = BoundedIndexField(in._1, in._2)

  // protected def getField(inst : Mapper[A], meth : Method) = meth.invoke(inst, null).asInstanceOf[MappedField[AnyRef,A]]
}

object OprEnum extends Enumeration {
  val Eql = Value(1, "=")
  val <> = Value(2, "<>")
  val >= = Value(3, ">=")
  val != = <>
  val <= = Value(4, "<=")
  val > = Value(5, ">")
  val < = Value(6, "<")
  val IsNull = Value(7, "IS NULL")
  val IsNotNull = Value(8, "IS NOT NULL")
  val Like = Value(9, "LIKE")
  val NotLike = Value(10, "NOT LIKE")
}

sealed trait BaseIndex[A <: Mapper[A]] {
  def columns: Seq[IndexItem[A]]
}

final case class Index[A <: Mapper[A]](columns: List[IndexItem[A]]) extends BaseIndex[A] // (columns :_*)

object Index {
  def apply[A <: Mapper[A]](cols: IndexItem[A] *): Index[A] = new Index[A](cols.toList)
}

/**
 *  Represents a unique index on the given columns
 */
final case class UniqueIndex[A <: Mapper[A]](columns: List[IndexItem[A]]) extends BaseIndex[A] // (uniqueColumns : _*)

object UniqueIndex {
  def apply[A <: Mapper[A]](cols: IndexItem[A] *): UniqueIndex[A] = new UniqueIndex[A](cols.toList)
}

/**
 * Represents a generic user-specified index on the given columns. The user provides a function to generate the SQL needed to create
 * the index based on the table and columns. Validation is required since this is raw SQL being run on the database server.
 */
final case class GenericIndex[A <: Mapper[A]](createFunc: (String,List[String]) => String, validated: IHaveValidatedThisSQL, columns: List[IndexItem[A]]) extends BaseIndex[A] // (indexColumns : _*)

object GenericIndex {
  def apply[A <: Mapper[A]](createFunc: (String,List[String]) => String, validated: IHaveValidatedThisSQL, cols: IndexItem[A] *): GenericIndex[A] =
    new GenericIndex[A](createFunc, validated, cols.toList)
}

abstract class IndexItem[A <: Mapper[A]] {
  def field: BaseMappedField
  def indexDesc: String
}

case class IndexField[A <: Mapper[A], T](field: MappedField[T, A]) extends IndexItem[A] {
  def indexDesc: String = MapperRules.quoteColumnName.vend(field._dbColumnNameLC)
}
case class BoundedIndexField[A <: Mapper[A]](field: MappedField[String, A], len: Int) extends IndexItem[A] {
  def indexDesc: String = MapperRules.quoteColumnName.vend(field._dbColumnNameLC)+"("+len+")"
}

sealed trait QueryParam[O<:Mapper[O]]
final case class Cmp[O<:Mapper[O], T](field: MappedField[T,O], opr: OprEnum.Value, value: Box[T],
                                      otherField: Box[MappedField[T, O]], dbFunc: Box[String]) extends QueryParam[O]

final case class OrderBy[O<:Mapper[O], T](field: MappedField[T,O],
                                          order: AscOrDesc,
                                          nullOrder: Box[NullOrder]) extends QueryParam[O]

sealed trait NullOrder {
  def getSql: String
}
case object NullsFirst extends NullOrder {
  def getSql: String = " NULLS FIRST "
}
case object NullsLast extends NullOrder  {
  def getSql: String = " NULLS LAST "
}

object OrderBy {
  def apply[O <: Mapper[O], T](field: MappedField[T, O],
                               order: AscOrDesc): OrderBy[O, T] =
                                 new OrderBy[O, T](field, order, Empty)

  def apply[O <: Mapper[O], T](field: MappedField[T, O],
                               order: AscOrDesc,
                             no: NullOrder): OrderBy[O, T] =
                               new OrderBy[O, T](field, order, Full(no))
}


trait AscOrDesc {
  def sql: String
}

case object Ascending extends AscOrDesc {
  def sql: String = " ASC "
}

case object Descending extends AscOrDesc {
  def sql: String = " DESC "
}

final case class Distinct[O <: Mapper[O]]() extends QueryParam[O]

final case class OrderBySql[O <: Mapper[O]](sql: String,
                                            checkedBy: IHaveValidatedThisSQL) extends QueryParam[O]

final case class ByList[O<:Mapper[O], T](field: MappedField[T,O], vals: Seq[T]) extends QueryParam[O]
/**
 * Represents a query criterion using a parameterized SQL string. Parameters are
 * substituted in order. For Date/Time types, passing a java.util.Date will result in a
 * Timestamp parameter. If you want a specific SQL Date/Time type, use the corresponding
 * java.sql.Date, java.sql.Time, or java.sql.Timestamp classes.
 */
final case class BySql[O<:Mapper[O]](query: String,
                                     checkedBy: IHaveValidatedThisSQL,
                                     params: Any*) extends QueryParam[O]
final case class MaxRows[O<:Mapper[O]](max: Long) extends QueryParam[O]
final case class StartAt[O<:Mapper[O]](start: Long) extends QueryParam[O]
final case class Ignore[O <: Mapper[O]]() extends QueryParam[O]

sealed abstract class InThing[OuterType <: Mapper[OuterType]] extends QueryParam[OuterType] {
  type JoinType
  type InnerType <: Mapper[InnerType]

  def outerField: MappedField[JoinType, OuterType]
  def innerField: MappedField[JoinType, InnerType]
  def innerMeta: MetaMapper[InnerType]
  def queryParams: List[QueryParam[InnerType]]

  def notIn: Boolean

  def inKeyword = if (notIn) " NOT IN " else " IN "

  def distinct: String =
    queryParams.find {case Distinct() => true case _ => false}.isDefined match {
      case false => ""
      case true => " DISTINCT "
    }
}

/**
 * This QueryParam can be put in a query and will cause the given foreign key field
 * to be precached.
 * @param field - the field to precache
 * @param deterministic - true if the query is deterministic.  Will be more efficient.
 * false if the query is not deterministic.  In this case, a SELECT * FROM FK_TABLE WHERE primary_key in (xxx) will
 * be generated
 */
final case class PreCache[TheType <: Mapper[TheType], FieldType, OtherType <: KeyedMapper[FieldType, OtherType]](field: MappedForeignKey[FieldType, TheType, OtherType], deterministic: Boolean)
extends QueryParam[TheType]

object PreCache {
  def apply[TheType <: Mapper[TheType], FieldType, OtherType <: KeyedMapper[FieldType, OtherType]](field: MappedForeignKey[FieldType , TheType, OtherType]) =
  new PreCache(field, true)
}

final case class InRaw[TheType <:
                       Mapper[TheType], T](field: MappedField[T, TheType],
                                           rawSql: String,
                                           checkedBy: IHaveValidatedThisSQL)
extends QueryParam[TheType]

object NotIn {
  def fk[InnerMapper <: Mapper[InnerMapper], JoinTypeA, Zoom, OuterMapper <: KeyedMapper[JoinTypeA, OuterMapper]](
    fielda: MappedForeignKey[JoinTypeA, InnerMapper, OuterMapper],
    qp: Zoom*
  )(implicit ev: Zoom => QueryParam[InnerMapper]): InThing[OuterMapper] = {
    new InThing[OuterMapper] {
      type JoinType = JoinTypeA
      type InnerType = InnerMapper

      val outerField: MappedField[JoinType, OuterMapper] = fielda.dbKeyToTable.primaryKeyField
      val innerField: MappedField[JoinType, InnerMapper] = fielda
      val innerMeta: MetaMapper[InnerMapper] = fielda.fieldOwner.getSingleton

      def notIn: Boolean = true

      val queryParams: List[QueryParam[InnerMapper]] =
      qp.map{v => val r: QueryParam[InnerMapper] = v; r}.toList
    }
  }

  def apply[InnerMapper <: Mapper[InnerMapper], JoinTypeA, Zoom, OuterMapper <: Mapper[OuterMapper]](
    _outerField: MappedField[JoinTypeA, OuterMapper],
    _innerField: MappedField[JoinTypeA, InnerMapper],
    qp: Zoom*
  )(implicit ev: Zoom => QueryParam[InnerMapper]): InThing[OuterMapper] = {
    new InThing[OuterMapper] {
      type JoinType = JoinTypeA
      type InnerType = InnerMapper

      val outerField: MappedField[JoinType, OuterMapper] = _outerField
      val innerField: MappedField[JoinType, InnerMapper] = _innerField
      val innerMeta: MetaMapper[InnerMapper] = innerField.fieldOwner.getSingleton

      def notIn: Boolean = true

      val queryParams: List[QueryParam[InnerMapper]] = {
        qp.map{v => val r: QueryParam[InnerMapper] = v; r}.toList
      }
    }
  }
}

object In {
  def fk[InnerMapper <: Mapper[InnerMapper], JoinTypeA, Zoom, OuterMapper <: KeyedMapper[JoinTypeA, OuterMapper]](
    fielda: MappedForeignKey[JoinTypeA, InnerMapper, OuterMapper],
    qp: Zoom*
  )(implicit ev: Zoom => QueryParam[InnerMapper]): InThing[OuterMapper] = {
    new InThing[OuterMapper] {
      type JoinType = JoinTypeA
      type InnerType = InnerMapper

      val outerField: MappedField[JoinType, OuterMapper] = fielda.dbKeyToTable.primaryKeyField
      val innerField: MappedField[JoinType, InnerMapper] = fielda
      val innerMeta: MetaMapper[InnerMapper] = fielda.fieldOwner.getSingleton

      def notIn: Boolean = false

      val queryParams: List[QueryParam[InnerMapper]] =
      qp.map{v => val r: QueryParam[InnerMapper] = v; r}.toList
    }
  }

  def apply[InnerMapper <: Mapper[InnerMapper], JoinTypeA, Zoom, OuterMapper <: Mapper[OuterMapper]](
    _outerField: MappedField[JoinTypeA, OuterMapper],
    _innerField: MappedField[JoinTypeA, InnerMapper],
    qp: Zoom*
  )(implicit ev: Zoom => QueryParam[InnerMapper]): InThing[OuterMapper] = {
    new InThing[OuterMapper] {
      type JoinType = JoinTypeA
      type InnerType = InnerMapper

      val outerField: MappedField[JoinType, OuterMapper] = _outerField
      val innerField: MappedField[JoinType, InnerMapper] = _innerField
      val innerMeta: MetaMapper[InnerMapper] = innerField.fieldOwner.getSingleton

      def notIn: Boolean = false

      val queryParams: List[QueryParam[InnerMapper]] = {
        qp.map{v => val r: QueryParam[InnerMapper] = v; r}.toList
      }
    }
  }
}

object Like {
  def apply[O <: Mapper[O]](field: MappedField[String, O], value: String) =
  Cmp[O, String](field, OprEnum.Like, Full(value), Empty, Empty)
}

object NotLike {
  def apply[O <: Mapper[O]](field: MappedField[String, O], value: String) =
  Cmp[O, String](field, OprEnum.NotLike, Full(value), Empty, Empty)
}

object By {
  import OprEnum._

  def apply[O <: Mapper[O], T, U](field: MappedField[T, O], value: U)(implicit ev: U => T) = Cmp[O,T](field, Eql, Full(value), Empty, Empty)
  def apply[O <: Mapper[O], T](field: MappedNullableField[T, O], value: Box[T]) = value match {
    case Full(x) => Cmp[O,Box[T]](field, Eql, Full(value), Empty, Empty)
    case _ => NullRef(field)
  }
  def apply[O <: Mapper[O],T,  Q <: KeyedMapper[T, Q]](field: MappedForeignKey[T, O, Q], value: Q) =
  Cmp[O,T](field, Eql, Full(value.primaryKeyField.get), Empty, Empty)

  def apply[O <: Mapper[O],T, Q <: KeyedMapper[T, Q]](field: MappedForeignKey[T, O, Q], value: Box[Q]) =
  value match {
    case Full(v) => Cmp[O,T](field, Eql, Full(v.primaryKeyField.get), Empty, Empty)
    case _ => Cmp(field, IsNull, Empty, Empty, Empty)
  }
}

object By_>= {

  import OprEnum._

  def apply[O <: Mapper[O], T, U](field: MappedField[T, O],
                                  value: U)(implicit ev: U => T) = Cmp[O, T](field, >=, Full(value), Empty, Empty)

  def apply[O <: Mapper[O], T](field: MappedField[T, O], otherField:
  MappedField[T, O]) = Cmp[O, T](field, >=, Empty, Full(otherField),
    Empty)
}

object By_<= {

  import OprEnum._

  def apply[O <: Mapper[O], T, U](field: MappedField[T, O],
                                  value: U)(implicit ev: U => T) = Cmp[O, T](field, <=, Full(value), Empty, Empty)

  def apply[O <: Mapper[O], T](field: MappedField[T, O], otherField:
  MappedField[T, O]) = Cmp[O, T](field, <=, Empty, Full(otherField),
    Empty)
}

object NotBy {
  import OprEnum._

  def apply[O <: Mapper[O], T, U](field: MappedField[T, O], value: U)(implicit ev: U => T) = Cmp[O,T](field, <>, Full(value), Empty, Empty)

  def apply[O <: Mapper[O], T](field: MappedNullableField[T, O], value: Box[T]) = value match {
    case Full(x) => Cmp[O,Box[T]](field, <>, Full(value), Empty, Empty)
    case _ => NotNullRef(field)
  }

  def apply[O <: Mapper[O],T,  Q <: KeyedMapper[T, Q]](field: MappedForeignKey[T, O, Q], value: Q) =
  Cmp[O,T](field, <>, Full(value.primaryKeyField.get), Empty, Empty)
  def apply[O <: Mapper[O],T, Q <: KeyedMapper[T, Q]](field: MappedForeignKey[T, O, Q], value: Box[Q]) =
  value match {
    case Full(v) => Cmp[O,T](field, <>, Full(v.primaryKeyField.get), Empty, Empty)
    case _ => Cmp(field, IsNotNull, Empty, Empty, Empty)
  }
}

object ByRef {
  import OprEnum._

  def apply[O <: Mapper[O], T](field: MappedField[T, O], otherField: MappedField[T,O]) = Cmp[O,T](field, Eql, Empty, Full(otherField), Empty)
}

object NotByRef {
  import OprEnum._

  def apply[O <: Mapper[O], T](field: MappedField[T, O], otherField: MappedField[T,O]) = Cmp[O,T](field, <>, Empty, Full(otherField), Empty)
}

object By_> {
  import OprEnum._

  def apply[O <: Mapper[O], T, U](field: MappedField[T, O], value: U)(implicit ev: U => T) = Cmp[O,T](field, >, Full(value), Empty, Empty)
  def apply[O <: Mapper[O], T](field: MappedField[T, O], otherField: MappedField[T,O]) = Cmp[O,T](field, >, Empty, Full(otherField), Empty)
}

object By_< {
  import OprEnum._

  def apply[O <: Mapper[O], T, U](field: MappedField[T, O], value: U)(implicit ev: U => T) = Cmp[O,T](field, <, Full(value), Empty, Empty)
  def apply[O <: Mapper[O], T](field: MappedField[T, O], otherField: MappedField[T,O]) = Cmp[O,T](field, <, Empty, Full(otherField), Empty)
}

object NullRef {
  import OprEnum._
  def apply[O <: Mapper[O], T](field: MappedField[T, O]) = Cmp(field, IsNull, Empty, Empty, Empty)
}

object NotNullRef {
  import OprEnum._
  def apply[O <: Mapper[O], T](field: MappedField[T, O]) = Cmp(field, IsNotNull, Empty, Empty, Empty)
}

trait LongKeyedMetaMapper[A <: LongKeyedMapper[A]] extends KeyedMetaMapper[Long, A] { self: A => }


trait KeyedMetaMapper[Type, A<:KeyedMapper[Type, A]] extends MetaMapper[A] with KeyedMapper[Type, A] {
  self: A  with MetaMapper[A] with KeyedMapper[Type, A] =>

  private def testProdArity(prod: Product): Boolean = {
    var pos = 0
    while (pos < prod.productArity) {
      if (!prod.productElement(pos).isInstanceOf[QueryParam[A]]) return false
      pos = pos + 1
    }
    true
  }

  type Q = MappedForeignKey[AnyBound, A, OO] with MappedField[AnyBound, A] forSome
  {type OO <: KeyedMapper[AnyBound, OO]}

  def asSafeJs(actual: A, f: KeyObfuscator): JsExp = {
    val pk = actual.primaryKeyField
    val first = (pk.name, JE.Str(f.obscure(self, pk.get)))
    JE.JsObj(
      first ::
        ("$lift_class", JE.Str(dbTableName)) ::
        mappedFieldList
          .map(f => this.??(f.method, actual))
          .filter(f => !f.dbPrimaryKey_? && f.renderJs_?)
          .flatMap{
            case fk0: MappedForeignKey[_, _, _] with MappedField[_, _] =>
              val fk = fk0.asInstanceOf[Q]
              val key = f.obscure(fk.dbKeyToTable, fk.get)
              List(
                (fk.name, JE.Str(key)),
                (fk.name+"_obj", JE.AnonFunc("index", JE.JsRaw("return index["+key.encJs+"];").cmd))
              )
            case x => x.asJs
          }
          .toList :::
        actual.suplementalJs(Full(f)) : _*
    )
  }

  private def convertToQPList(prod: Product): Array[QueryParam[A]] = {
    var pos = 0
    val ret = new Array[QueryParam[A]](prod.productArity)
    while (pos < prod.productArity) {
      ret(pos) = prod.productElement(pos).asInstanceOf[QueryParam[A]]
      pos = pos + 1
    }
    ret
  }

  private def anyToFindString(in: Any): Box[String] =
  in match {
    case Empty | None | null | Failure(_, _, _) => Empty
    case Full(n) => anyToFindString(n)
    case Some(n) => anyToFindString(n)
    case v => Full(v.toString)
  }

  private object unapplyMemo extends RequestMemoize[Any, Box[A]] {
    override protected def __nameSalt = Helpers.randomString(20)
  }

  def unapply(key: Any): Option[A] = {
    if (S.inStatefulScope_?) unapplyMemo(key, this.find(key))
    else this.find(key)
  }

  def find(key: Any): Box[A] =
  key match {
    case qp: QueryParam[A] => find(qp)
    case prod: Product if (testProdArity(prod)) => find(convertToQPList(prod).toIndexedSeq :_*)
    case key => anyToFindString(key) flatMap (find(_))
  }

  def findDb(dbId: ConnectionIdentifier, key: Any): Box[A] =
  key match {
    case qp: QueryParam[A] => findDb(dbId, List(qp.asInstanceOf[QueryParam[A]]) :_*)
    case prod: Product if (testProdArity(prod)) => findDb(dbId, convertToQPList(prod).toIndexedSeq :_*)
    case key => anyToFindString(key) flatMap (find(dbId, _))
  }

  /**
   * Find the element based on the first element of the List
   */
  def find(key: List[String]): Box[A] = key match {
    case Nil => Empty
    case x :: _ => find(x)
  }

  /**
   * Find an element by primary key or create a new one
   */
  def findOrCreate(key: Any): A = find(key) openOr create

  /**
   * Find an element by primary key or create a new one
   */
  def findOrCreate(key: List[String]): A = find(key) openOr create

  def find(key: String): Box[A] = dbStringToKey(key) flatMap (realKey => findDbByKey(selectDbForKey(realKey), realKey))

  def find(dbId: ConnectionIdentifier, key: String): Box[A] =  dbStringToKey(key) flatMap (realKey =>  findDbByKey(dbId, realKey))

  def findByKey(key: Type): Box[A] = findDbByKey(selectDbForKey(key), key)

  def dbStringToKey(in: String): Box[Type] = primaryKeyField.convertKey(in)

  private def selectDbForKey(key: Type): ConnectionIdentifier =
  if (dbSelectDBConnectionForFind.isDefinedAt(key)) dbSelectDBConnectionForFind(key)
  else dbDefaultConnectionIdentifier

  def dbSelectDBConnectionForFind: PartialFunction[Type, ConnectionIdentifier] = Map.empty

  def findDbByKey(dbId: ConnectionIdentifier, key: Type): Box[A] =
  findDbByKey(dbId, mappedFields, key)

  def findDbByKey(dbId: ConnectionIdentifier, fields: Seq[SelectableField],
                  key: Type): Box[A] =
  DB.use(dbId) { conn =>
    val field = primaryKeyField

    DB.prepareStatement("SELECT "+
                        fields.map(_.dbSelectString).
                        mkString(", ")+
                        " FROM "+MapperRules.quoteTableName.vend(_dbTableNameLC)+" WHERE "+MapperRules.quoteColumnName.vend(field._dbColumnNameLC)+" = ?", conn) {
      st =>
	if (field.dbIgnoreSQLType_?)
	  st.setObject(1, field.makeKeyJDBCFriendly(key))
	else
	  st.setObject(1, field.makeKeyJDBCFriendly(key),
		       conn.driverType.
		       columnTypeMap(field.
				     targetSQLType(field._dbColumnNameLC)))
      DB.exec(st) {
        rs =>
        val mi = buildMapper(rs)
        if (rs.next) Full(createInstance(dbId, rs, mi))
        else Empty
      }
    }
  }

  def find(by: QueryParam[A]): Box[A] = find(Seq(by): _*)

  def find(by: QueryParam[A]*): Box[A] =
  findDb(dbDefaultConnectionIdentifier, by :_*)

  def findDb(dbId: ConnectionIdentifier, by: QueryParam[A]*): Box[A] =
  findDb(dbId, mappedFields, by :_*)

  def findDb(dbId: ConnectionIdentifier, fields: Seq[SelectableField],
             by: QueryParam[A]*): Box[A] = {
    DB.use(dbId) {
      conn =>

      val (query, start, max, bl) = buildSelectString(fields, conn, by :_*)
      DB.prepareStatement(query, conn) {
        st =>
        setStatementFields(st, bl, 1, conn)
        DB.exec(st) {
          rs =>
          val mi = buildMapper(rs)
          if (rs.next) Full(createInstance(dbId, rs, mi))
          else Empty
        }

      }
    }
  }

  override def afterSchemifier: Unit = {
    if (crudSnippets_?) {
      LiftRules.snippets.append(crudSnippets)
    }
  }

  /**
   * Override this definition in your model to enable CRUD snippets
   * for that model. Set to false by default.
   *
   * Remember to override editSnippetSetup and viewSnippetSetup as well,
   * as the defaults are broken.
   *
   * @return false
   */
  def crudSnippets_? = false

  /**
   * Defines the default CRUD snippets. Override if you want to change
   * the names of the snippets. Defaults are "add", "edit", and "view".
   *
   * (No, there's no D in CRUD.)
   */
  def crudSnippets: LiftRules.SnippetPF = {
    val Name = internal_dbTableName

    NamedPF("crud "+Name) {
      case Name :: "addForm"  :: _ => addFormSnippet
      case Name :: "editForm" :: _ => editFormSnippet
      case Name :: "viewTransform" :: _ => viewTransform
    }
  }

   /**
   * Provides basic transformation of <code>html</code> to a form for the
   * given <code>obj</code>. When the form is submitted, <code>cleanup</code>
   * is run.
   */
  def formSnippet(html: NodeSeq, obj: A, cleanup: (A => Unit)): NodeSeq = {
    val name = internal_dbTableName

    def callback(): Unit = {
      cleanup(obj)
    }

    val submitTransform: (NodeSeq)=>NodeSeq =
      "type=submit" #> SHtml.onSubmitUnit(callback _)

    val otherTransforms =
      obj.fieldMapperTransforms(_.toForm openOr Text("")).reverse ++
      obj.fieldTransforms.reverse

    otherTransforms.foldRight(submitTransform)(_ andThen _) apply html
  }

  /**
   * Base add form snippet. Fetches object from
   * <code>addSnippetSetup</code> and invokes
   * <code>addSnippetCallback</code> when the form is submitted.
   */
  def addFormSnippet(html: NodeSeq): NodeSeq = {
    formSnippet(html, addSnippetSetup, addSnippetCallback _)
  }

 /**
   * Base edit form snippet. Fetches object from
   * <code>editSnippetSetup</code> and invokes
   * <code>editSnippetCallback</code> when the form is submitted.
   */
  def editFormSnippet(html: NodeSeq): NodeSeq = {
    formSnippet(html, editSnippetSetup, editSnippetCallback _)
  }

  /**
   * Basic transformation of <code>html</code> to HTML for displaying
   * the object from <code>viewSnippetSetup</code>.
   */
  def viewTransform(html: NodeSeq): NodeSeq = {
    val name = internal_dbTableName
    val obj: A = viewSnippetSetup

    val otherTransforms =
      obj.fieldMapperTransforms(_.asHtml).reverse ++
      obj.fieldTransforms.reverse

    otherTransforms.foldRight(PassThru: (NodeSeq)=>NodeSeq)(_ andThen _) apply html
  }

  /**
   * Lame attempt at automatically getting an object from the HTTP parameters.
   * BROKEN! DO NOT USE! Only here so that existing sub-classes KeyedMetaMapper
   * don't have to implement new methods when I commit the CRUD snippets code.
   */
  def objFromIndexedParam: Box[A] = {
    val found = for (
      req <- S.request.toList;
      (param, value :: _) <- req.params;
      fh <- mappedFieldList if fh.field.dbIndexed_? == true && fh.name.equals(param)
    ) yield find(value)

    found.filter(obj => obj match {
        case Full(obj) => true
        case _         => false
      }) match {
      case obj :: _ => obj
      case _        => Empty
    }
  }

  /**
   * Default setup behavior for the add snippet. Creates a new mapped object.
   *
   * @return new mapped object
   */
  def addSnippetSetup: A = {
    this.create
  }

  /**
   * Default setup behavior for the edit snippet. BROKEN! MUST OVERRIDE IF
   * USING CRUD SNIPPETS!
   *
   * @return a mapped object of this metamapper's type
   */
  def editSnippetSetup: A = {
    objFromIndexedParam.openOrThrowException("Comment says this is broken")
  }
  /**
   * Default setup behavior for the view snippet. BROKEN! MUST OVERRIDE IF
   * USING CRUD SNIPPETS!
   *
   * @return a mapped object of this metamapper's type
   */
  def viewSnippetSetup: A = {
    objFromIndexedParam.openOrThrowException("Comment says this is broken")
  }
  /**
   * Default callback behavior of the edit snippet. Called when the user
   * presses submit. Saves the passed in object.
   *
   * @param obj mapped object of this metamapper's type
   */
  def editSnippetCallback(obj: A): Unit = { obj.save }
  /**
   * Default callback behavior of the add snippet. Called when the user
   * presses submit. Saves the passed in object.
   *
   * @param obj mapped object of this metamapper's type
   */
  def addSnippetCallback(obj: A): Unit = { obj.save }
}


class KeyObfuscator {
  private var to: Map[String, Map[Any, String]] = Map.empty
  private var from: Map[String, Map[String, Any]] = Map.empty

  def obscure[KeyType, MetaType <: KeyedMapper[KeyType, MetaType]](theType:
                                                                   KeyedMetaMapper[KeyType, MetaType], key: KeyType): String = synchronized {
    val local: Map[Any, String] = to.getOrElse(theType._dbTableNameLC, Map.empty)
    local.get(key) match {
      case Some(s) => s
      case _ => val ret = "r"+randomString(15)

        val l2: Map[Any, String] = local + ( (key -> ret) )
        to = to + ( (theType._dbTableNameLC -> l2) )

        val lf: Map[String, Any] = from.getOrElse(theType._dbTableNameLC, Map.empty) + ( (ret -> key))
        // lf(ret) = key
        from = from + ( (theType._dbTableNameLC -> lf) )

        ret
    }
  }

  def obscure[KeyType, MetaType <: KeyedMapper[KeyType, MetaType]](what: KeyedMapper[KeyType, MetaType]): String =
  {
    obscure(what.getSingleton, what.primaryKeyField.get)
  }

  def apply[KeyType, MetaType <: KeyedMapper[KeyType, MetaType], Q](theType:
                                                                    KeyedMetaMapper[KeyType, MetaType], key: Q)(implicit ev: Q => KeyType): String = {
    val k: KeyType = key
    obscure(theType, k)
  }

  def apply[KeyType, MetaType <: KeyedMapper[KeyType, MetaType]](what: KeyedMapper[KeyType, MetaType]): String =
  {
    obscure(what)
  }


  def recover[KeyType, MetaType <: KeyedMapper[KeyType, MetaType]](theType:
                                                                   KeyedMetaMapper[KeyType, MetaType], id: String): Box[KeyType] = synchronized {
    for {
      map <- from.get(theType._dbTableNameLC)
      item <- map.get(id)
    } yield item.asInstanceOf[KeyType]
  }
}

case class IHaveValidatedThisSQL(who: String, date: String)

trait SelectableField {
  def dbSelectString: String
}

class MapperException(msg: String) extends Exception(msg)

