package olon
package mongodb
package record
package field

import olon.common.{Box, Empty, Failure, Full}
import olon.http.js.JE.Str
import olon.json.JsonAST.{JNothing, JObject, JValue}
import olon.record.{Field, MandatoryTypedField, Record}
import scala.xml.NodeSeq

import com.mongodb.{BasicDBObject, BasicDBObjectBuilder, DBObject, DBRef}
import com.mongodb.util.JSON
import org.bson.types.ObjectId

/*
* Field for storing a DBRef
*/
@deprecated("DBref is being removed. See 'MongoRef' for an alternative", "3.4.3")
class DBRefField[OwnerType <: BsonRecord[OwnerType], RefType <: MongoRecord[RefType]](rec: OwnerType, ref: RefType)
  extends Field[DBRef, OwnerType] with MandatoryTypedField[DBRef] {

  /*
  * get the referenced object
  */
  def obj = synchronized {
    if (!_calcedObj) {
      _calcedObj = true
      this._obj = ref.meta.findAny(value.getId)
    }
    _obj
  }

  def cached_? : Boolean = synchronized { _calcedObj }

  def primeObj(obj: Box[RefType]) = synchronized {
    _obj = obj
    _calcedObj = true
  }

  private var _obj: Box[RefType] = Empty
  private var _calcedObj = false

  def asJs = Str(toString)

  def asJValue: JValue = (JNothing: JValue) // not implemented

  def setFromJValue(jvalue: JValue) = Empty // not implemented

  def asXHtml = <div></div>

  def defaultValue = new DBRef("", null)

  def setFromAny(in: Any): Box[DBRef] = in match {
    case ref: DBRef => Full(set(ref))
    case Some(ref: DBRef) => Full(set(ref))
    case Full(ref: DBRef) => Full(set(ref))
    case seq: Seq[_] if !seq.isEmpty => seq.map(setFromAny).apply(0)
    case (s: String) :: _ => setFromString(s)
    case null => Full(set(null))
    case s: String => setFromString(s)
    case None | Empty | Failure(_, _, _) => Full(set(null))
    case o => setFromString(o.toString)
  }

  // assume string is json
  def setFromString(in: String): Box[DBRef] = {
    val dbo = JSON.parse(in).asInstanceOf[BasicDBObject]
    val id = dbo.get("$id").toString
    ObjectId.isValid(id) match {
      case true => Full(set(new DBRef(dbo.get("$ref").toString, new ObjectId(id))))
      case false => Full(set(new DBRef(dbo.get("$ref").toString, id)))
    }
  }

  def toForm: Box[NodeSeq] = Empty

  def owner = rec
}

