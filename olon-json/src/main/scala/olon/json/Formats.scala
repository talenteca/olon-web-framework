package olon
package json

import java.util.Date
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent.{Map => ConcurrentScalaMap}
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag

/** Formats to use when converting JSON. Formats are usually configured by using
  * an implicit parameter: <pre> implicit val formats = olon.json.DefaultFormats
  * </pre>
  */
trait Formats { self: Formats =>
  val dateFormat: DateFormat
  val typeHints: TypeHints = NoTypeHints
  // SCALA3 using `?` instead of `_`
  val customSerializers: List[Serializer[?]] = Nil
  // SCALA3 using `?` instead of `_`
  val fieldSerializers: List[(Class[?], FieldSerializer[?])] = Nil

  /** Support for the tuple decomposition/extraction that represents tuples as
    * JSON arrays. This provides better support for heterogenous arrays in JSON,
    * but enable it at your own risk as it does change the behavior of
    * serialization/deserialization and comes with some caveats (such as Scala
    * primitives not being recognized reliably during extraction).
    */
  val tuplesAsArrays = false

  /** The name of the field in JSON where type hints are added (jsonClass by
    * default)
    */
  val typeHintFieldName = "jsonClass"

  /** Parameter name reading strategy. By deafult 'paranamer' is used.
    */
  val parameterNameReader: ParameterNameReader = Meta.ParanamerReader

  /** Adds the specified type hints to this formats.
    */
  def +(extraHints: TypeHints): Formats = new Formats {
    val dateFormat = Formats.this.dateFormat
    override val typeHintFieldName = self.typeHintFieldName
    override val parameterNameReader = self.parameterNameReader
    override val typeHints = self.typeHints + extraHints
    override val customSerializers = self.customSerializers
    override val fieldSerializers = self.fieldSerializers
  }

  /** Adds the specified custom serializer to this formats.
    */
  // SCALA3 using `?` instead of `_`
  def +(newSerializer: Serializer[?]): Formats = new Formats {
    val dateFormat = Formats.this.dateFormat
    override val typeHintFieldName = self.typeHintFieldName
    override val parameterNameReader = self.parameterNameReader
    override val typeHints = self.typeHints
    override val customSerializers = newSerializer :: self.customSerializers
    override val fieldSerializers = self.fieldSerializers
  }

  /** Adds the specified custom serializers to this formats.
    */
  // SCALA3 using `?` instead of `_`
  def ++(newSerializers: Iterable[Serializer[?]]): Formats =
    newSerializers.foldLeft(this)(_ + _)

  /** Adds a field serializer for a given type to this formats.
    */
  // SCALA3 Using `ClassTag` instead of `Manifest`
  def +[A](
      newSerializer: FieldSerializer[A]
  )(implicit mf: ClassTag[A]): Formats = new Formats {
    val dateFormat = Formats.this.dateFormat
    override val typeHintFieldName = self.typeHintFieldName
    override val parameterNameReader = self.parameterNameReader
    override val typeHints = self.typeHints
    override val customSerializers = self.customSerializers
    // The type inferencer infers an existential type below if we use
    // value :: list instead of list.::(value), and we get a feature
    // warning.
    // SCALA3 using `?` instead of `_`
    override val fieldSerializers: List[(Class[?], FieldSerializer[?])] =
      self.fieldSerializers.::((mf.runtimeClass: Class[?], newSerializer))
  }

  // SCALA3 using `?` instead of `_`
  private[json] def fieldSerializer(
      clazz: Class[?]
  ): Option[FieldSerializer[?]] = {
    import ClassDelta._

    // SCALA3 using `?` instead of `_`
    val ord =
      Ordering[Int].on[(Class[?], FieldSerializer[?])](x => delta(x._1, clazz))
    fieldSerializers filter (_._1.isAssignableFrom(clazz)) match {
      case Nil => None
      case xs  => Some((xs min ord)._2)
    }
  }

  def customSerializer(implicit format: Formats) =
    customSerializers.foldLeft(Map(): PartialFunction[Any, JValue]) {
      (acc, x) =>
        acc.orElse(x.serialize)
    }

  def customDeserializer(implicit format: Formats) = {
    customSerializers.foldLeft(
      Map(): PartialFunction[(TypeInfo, JValue), Any]
    ) { (acc, x) =>
      acc.orElse(x.deserialize)
    }
  }

}

/** Conversions between String and Date.
  */
trait DateFormat {
  def parse(s: String): Option[Date]
  def format(d: Date): String
}

trait Serializer[A] {
  def deserialize(implicit
      format: Formats
  ): PartialFunction[(TypeInfo, JValue), A]
  def serialize(implicit format: Formats): PartialFunction[Any, JValue]
}

/** Type hints can be used to alter the default conversion rules when converting
  * Scala instances into JSON and vice versa. Type hints must be used when
  * converting class which is not supported by default (for instance when class
  * is not a case class). <p> Example:<pre> class DateTime(val time: Long)
  *
  * val hints = new ShortTypeHints(classOf[DateTime] :: Nil) { override def
  * serialize: PartialFunction[Any, JObject] = { case t: DateTime =>
  * JObject(JField("t", JInt(t.time)) :: Nil) }
  *
  * override def deserialize: PartialFunction[(String, JObject), Any] = { case
  * ("DateTime", JObject(JField("t", JInt(t)) :: Nil)) => new
  * DateTime(t.longValue) } } implicit val formats =
  * DefaultFormats.withHints(hints) </pre>
  */
trait TypeHints {
  import ClassDelta._

  // SCALA3 using `?` instead of `_`
  val hints: List[Class[?]]

  /** Return hint for given type.
    */
  // SCALA3 using `?` instead of `_`
  def hintFor(clazz: Class[?]): String

  /** Return type for given hint.
    */
  // SCALA3 using `?` instead of `_`
  def classFor(hint: String): Option[Class[?]]

  // SCALA3 using `?` instead of `_`
  def containsHint_?(clazz: Class[?]) = hints exists (_.isAssignableFrom(clazz))
  def deserialize: PartialFunction[(String, JObject), Any] = Map()
  def serialize: PartialFunction[Any, JObject] = Map()

  def components: List[TypeHints] = List(this)

  /** Adds the specified type hints to this type hints.
    */
  def +(hints: TypeHints): TypeHints = CompositeTypeHints(
    components ::: hints.components
  )

  private[TypeHints] case class CompositeTypeHints(
      override val components: List[TypeHints]
  ) extends TypeHints {
    // SCALA3 using `?` instead of `_`
    val hints: List[Class[?]] = components.flatMap(_.hints)

    /** Chooses most specific class.
      */
    // SCALA3 using `?` instead of `_`
    def hintFor(clazz: Class[?]): String = components
      .filter(_.containsHint_?(clazz))
      .map(th =>
        (
          th.hintFor(clazz),
          th.classFor(th.hintFor(clazz))
            .getOrElse(sys.error("hintFor/classFor not invertible for " + th))
        )
      )
      .sortWith((x, y) => (delta(x._2, clazz) - delta(y._2, clazz)) < 0)
      .head
      ._1

    // SCALA3 using `?` instead of `_`
    def classFor(hint: String): Option[Class[?]] = {
      def hasClass(h: TypeHints) =
        scala.util.control.Exception.allCatch opt (h.classFor(
          hint
        )) map (_.isDefined) getOrElse (false)

      components find (hasClass) flatMap (_.classFor(hint))
    }

    override def deserialize: PartialFunction[(String, JObject), Any] =
      components.foldLeft[PartialFunction[(String, JObject), Any]](Map()) {
        (result, cur) => result.orElse(cur.deserialize)
      }

    override def serialize: PartialFunction[Any, JObject] =
      components.foldLeft[PartialFunction[Any, JObject]](Map()) {
        (result, cur) => result.orElse(cur.serialize)
      }
  }
}

private[json] object ClassDelta {
  // SCALA3 using `?` instead of `_`
  def delta(class1: Class[?], class2: Class[?]): Int = {
    if (class1 == class2) 0
    else if (class1.getInterfaces.contains(class2)) 0
    else if (class2.getInterfaces.contains(class1)) 0
    else if (class1.isAssignableFrom(class2)) {
      1 + delta(class1, class2.getSuperclass)
    } else if (class2.isAssignableFrom(class1)) {
      1 + delta(class1.getSuperclass, class2)
    } else
      sys.error(
        "Don't call delta unless one class is assignable from the other"
      )
  }
}

/** Do not use any type hints.
  */
case object NoTypeHints extends TypeHints {
  val hints = Nil
  // SCALA3 using `?` instead of `_`
  def hintFor(clazz: Class[?]) =
    sys.error("NoTypeHints does not provide any type hints.")
  def classFor(hint: String) = None
}

/** Use short class name as a type hint.
  */
// SCALA3 using `?` instead of `_`
case class ShortTypeHints(hints: List[Class[?]]) extends TypeHints {
  def hintFor(clazz: Class[?]) =
    clazz.getName.substring(clazz.getName.lastIndexOf(".") + 1)
  def classFor(hint: String) = hints find (hintFor(_) == hint)
}

/** Use full class name as a type hint.
  */
// SCALA3 using `?` instead of `_`
case class FullTypeHints(hints: List[Class[?]]) extends TypeHints {
  // SCALA3 using `?` instead of `_`
  private val hintsToClass: ConcurrentScalaMap[String, Class[?]] =
    new ConcurrentHashMap[String, Class[?]]().asScala ++= hints.map(clazz =>
      hintFor(clazz) -> clazz
    )

  // SCALA3 using `?` instead of `_`
  def hintFor(clazz: Class[?]) = clazz.getName

  // SCALA3 using `?` instead of `_`
  def classFor(hint: String): Option[Class[?]] = {
    hintsToClass.get(hint).orElse {
      val clazz = Thread.currentThread.getContextClassLoader.loadClass(hint)
      hintsToClass.putIfAbsent(hint, clazz).orElse(Some(clazz))
    }
  }
}

/** Default date format is UTC time.
  */
object DefaultFormats extends DefaultFormats {
  val losslessDate = new ThreadLocal(
    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  )
  val UTC = TimeZone.getTimeZone("UTC")
}

trait DefaultFormats extends Formats {
  import java.text.{ParseException, SimpleDateFormat}

  val dateFormat = new DateFormat {
    def parse(s: String) = try {
      Some(formatter.parse(s))
    } catch {
      case _: ParseException => None
    }

    def format(d: Date) = formatter.format(d)

    private def formatter = {
      val f = dateFormatter
      f.setTimeZone(DefaultFormats.UTC)
      f
    }
  }

  protected def dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

  /** Lossless date format includes milliseconds too.
    */
  def lossless = new DefaultFormats {
    override def dateFormatter = DefaultFormats.losslessDate()
  }

  /** Default formats with given <code>TypeHint</code>s.
    */
  def withHints(hints: TypeHints) = new DefaultFormats {
    override val typeHints = hints
  }
}

private[json] class ThreadLocal[A](init: => A)
    extends java.lang.ThreadLocal[A]
    with (() => A) {
  override def initialValue = init
  def apply() = get()
}

// SCALA3 Using `ClassTag` instead of `Manifest`
class CustomSerializer[A: ClassTag](
    ser: Formats => (PartialFunction[JValue, A], PartialFunction[Any, JValue])
) extends Serializer[A] {

  // SCALA3 Using `ClassTag` instead of `Manifest`
  val Class = implicitly[ClassTag[A]].runtimeClass

  def deserialize(implicit format: Formats) = {
    case (TypeInfo(Class, _), json) =>
      if (ser(format)._1.isDefinedAt(json)) ser(format)._1(json)
      else throw new MappingException("Can't convert " + json + " to " + Class)
  }

  def serialize(implicit format: Formats) = ser(format)._2
}
