package olon
package http
package rest

import olon._

import scala.xml.Elem
import scala.xml.Text

import json._
import common._
import util._

/** Mix this trait into a class to provide a list of REST helper methods
  */
trait RestHelper extends LiftRules.DispatchPF {
  import JsonAST._

  /** Will the request accept a JSON response? Yes if the Accept header contains
    * "text/json", "application/json" or the Accept header is missing or
    * contains "star/star" and the suffix is "json". Override this method to
    * provide your own logic. If there's no suffix, no accepts (or it's
    * "star/star") then look to either the Content-Type header or the
    * defaultGetAsJson flag
    */
  protected def jsonResponse_?(in: Req): Boolean = {
    (in.acceptsJson_? && !in.acceptsStarStar) ||
    ((in.weightedAccept.isEmpty ||
      in.acceptsStarStar) &&
      (in.path.suffix.equalsIgnoreCase("json") ||
        in.json_? ||
        (in.path.suffix.length == 0 && defaultGetAsJson))) ||
    suplimentalJsonResponse_?(in)
  }

  /** If the headers and the suffix say nothing about the response type, should
    * we default to JSON. By default, yes, override to change the behavior.
    */
  protected def defaultGetAsJson: Boolean = true

  /** If the headers and the suffix say nothing about the response type, should
    * we default to XML. By default, no, override to change the behavior.
    */
  protected def defaultGetAsXml: Boolean = false

  /** Take any value and convert it into a JValue. Full box if it works, empty
    * if it does
    */
  protected def anyToJValue(in: Any): Box[JValue] = Helpers.tryo {
    implicit def formats = DefaultFormats
    Extraction.decompose(in)
  }

  /** If there are additional custom rules (e.g., looking at query parameters)
    * you can override this method which is consulted if the other rules in
    * jsonResponse_? fail
    */
  protected def suplimentalJsonResponse_?(in: Req): Boolean = false

  /** Will the request accept an XML response? Yes if the Accept header contains
    * "application/xml" or "text/xml" or the Accept header is missing or
    * contains "star/star" and the suffix is "xml". Override this method to
    * provide your own logic. If there's no suffix, no accepts (or it's
    * "star/star") then look to either the Content-Type header or the
    * defaultGetAsXml flag.
    */
  protected def xmlResponse_?(in: Req): Boolean = {
    (in.acceptsXml_? && !in.acceptsStarStar) ||
    ((in.weightedAccept.isEmpty ||
      in.acceptsStarStar) &&
      (in.path.suffix.equalsIgnoreCase("xml") ||
        in.xml_? ||
        (in.path.suffix.length == 0 && defaultGetAsXml))) ||
    suplimentalXmlResponse_?(in)
  }

  /** If there are additional custom rules (e.g., looking at query parameters)
    * you can override this method which is consulted if the other rules in
    * xmlResponse_? fail
    */
  protected def suplimentalXmlResponse_?(in: Req): Boolean = false

  /** A trait that defines the TestReq extractor. Is the request something that
    * expects JSON, XML in the response. Subclass this trait to change the
    * behavior
    */
  protected trait TestReq {

    /** Test to see if the request is expecting JSON, XML in the response. The
      * path and a Tuple2 representing RequestType and the Req instance are
      * extracted.
      */
    def unapply(r: Req): Option[(List[String], (RequestType, Req))] =
      if (testResponse_?(r))
        Some(r.path.partPath -> (r.requestType -> r))
      else None

    def testResponse_?(r: Req): Boolean
  }

  protected trait XmlTest {
    def testResponse_?(r: Req): Boolean = xmlResponse_?(r)
  }

  protected trait JsonTest {
    def testResponse_?(r: Req): Boolean = jsonResponse_?(r)
  }

  /** The stable identifier for JsonReq. You can use it as an extractor.
    */
  protected lazy val JsonReq = new TestReq with JsonTest

  /** The stable identifier for XmlReq. You can use it as an extractor.
    */
  protected lazy val XmlReq = new TestReq with XmlTest

  /** A trait that defines the TestGet extractor. Is the request a GET and
    * something that expects JSON or XML in the response. Subclass this trait to
    * change the behavior
    */
  protected trait TestGet {

    /** Test to see if the request is a GET and expecting JSON in the response.
      * The path and the Req instance are extracted.
      */
    def unapply(r: Req): Option[(List[String], Req)] =
      if (r.get_? && testResponse_?(r))
        Some(r.path.partPath -> r)
      else None

    def testResponse_?(r: Req): Boolean
  }

  /** The stable identifier for JsonGet. You can use it as an extractor.
    */
  protected lazy val JsonGet = new TestGet with JsonTest

  /** The stable identifier for XmlGet. You can use it as an extractor.
    */
  protected lazy val XmlGet = new TestGet with XmlTest

  /** A trait that defines the TestDelete extractor. Is the request a DELETE and
    * something that expects JSON or XML in the response. Subclass this trait to
    * change the behavior
    */
  protected trait TestDelete {

    /** Test to see if the request is a DELETE and expecting JSON or XML in the
      * response. The path and the Req instance are extracted.
      */
    def unapply(r: Req): Option[(List[String], Req)] =
      if (r.requestType.delete_? && testResponse_?(r))
        Some(r.path.partPath -> r)
      else None

    def testResponse_?(r: Req): Boolean
  }

  /** The stable identifier for JsonDelete. You can use it as an extractor.
    */
  protected lazy val JsonDelete = new TestDelete with JsonTest

  /** The stable identifier for XmlDelete. You can use it as an extractor.
    */
  protected lazy val XmlDelete = new TestDelete with XmlTest

  /** A trait that defines the TestPost extractor. Is the request a POST, has
    * JSON or XML data in the post body and something that expects JSON or XML
    * in the response. Subclass this trait to change the behavior
    */
  protected trait TestPost[T] {

    /** Test to see if the request is a POST, has JSON data in the body and
      * expecting JSON in the response. The path, JSON Data and the Req instance
      * are extracted.
      */
    def unapply(r: Req): Option[(List[String], (T, Req))] =
      if (r.post_? && testResponse_?(r))
        body(r).toOption.map(t => (r.path.partPath -> (t -> r)))
      else None

    def testResponse_?(r: Req): Boolean

    def body(r: Req): Box[T]
  }

  /** A trait that defines the TestPatch extractor. Is the request a PATCH, has
    * JSON or XML data in the post body and something that expects JSON or XML
    * in the response. Subclass this trait to change the behavior
    */
  protected trait TestPatch[T] {

    /** Test to see if the request is a PATCH, has JSON data in the body and
      * expecting JSON in the response. The path, JSON Data and the Req instance
      * are extracted.
      */
    def unapply(r: Req): Option[(List[String], (T, Req))] =
      if (r.patch_? && testResponse_?(r))
        body(r).toOption.map(t => (r.path.partPath -> (t -> r)))
      else None

    def testResponse_?(r: Req): Boolean

    def body(r: Req): Box[T]
  }

  /** The stable identifier for JsonPatch. You can use it as an extractor.
    */
  protected lazy val JsonPatch = new TestPatch[JValue]
    with JsonTest
    with JsonBody

  /** The stable identifier for XmlPatch. You can use it as an extractor.
    */
  protected lazy val XmlPatch = new TestPatch[Elem] with XmlTest with XmlBody

  /** a trait that extracts the JSON body from a request It is composed with a
    * TestXXX to get the correct thing for the extractor
    */
  protected trait JsonBody {
    def body(r: Req): Box[JValue] = r.json
  }

  /** a trait that extracts the XML body from a request It is composed with a
    * TestXXX to get the correct thing for the extractor
    */
  protected trait XmlBody {
    def body(r: Req): Box[Elem] = r.xml
  }

  /** An extractor that tests the request to see if it's a GET and if it is, the
    * path and the request are extracted. It can be used as:<br/> <pre>case
    * "api" :: id :: _ Get req => ...</pre><br/> or<br/> <pre>case Get("api" ::
    * id :: _, req) => ...</pre><br/> *
    */
  protected object Get {
    def unapply(r: Req): Option[(List[String], Req)] =
      if (r.get_?) Some(r.path.partPath -> r) else None

  }

  /** An extractor that tests the request to see if it's a POST and if it is,
    * the path and the request are extracted. It can be used as:<br/> <pre>case
    * "api" :: id :: _ Post req => ...</pre><br/> or<br/> <pre>case Post("api"
    * :: id :: _, req) => ...</pre><br/>
    */
  protected object Post {
    def unapply(r: Req): Option[(List[String], Req)] =
      if (r.post_?) Some(r.path.partPath -> r) else None

  }

  /** An extractor that tests the request to see if it's a PUT and if it is, the
    * path and the request are extracted. It can be used as:<br/> <pre>case
    * "api" :: id :: _ Put req => ...</pre><br/> or<br/> <pre>case Put("api" ::
    * id :: _, req) => ...</pre><br/>
    */
  protected object Put {
    def unapply(r: Req): Option[(List[String], Req)] =
      if (r.put_?) Some(r.path.partPath -> r) else None

  }

  /** An extractor that tests the request to see if it's a PATCH and if it is,
    * the path and the request are extracted. It can be used as:<br/> <pre>case
    * "api" :: id :: _ Patch req => ...</pre><br/> or<br/> <pre>case Patch("api"
    * :: id :: _, req) => ...</pre><br/>
    */
  protected object Patch {
    def unapply(r: Req): Option[(List[String], Req)] =
      if (r.patch_?) Some(r.path.partPath -> r) else None

  }

  /** An extractor that tests the request to see if it's a DELETE and if it is,
    * the path and the request are extracted. It can be used as:<br/> <pre>case
    * "api" :: id :: _ Delete req => ...</pre><br/> or<br/> <pre>case
    * Delete("api" :: id :: _, req) => ...</pre><br/>
    */
  protected object Delete {
    def unapply(r: Req): Option[(List[String], Req)] =
      if (r.requestType.delete_?) Some(r.path.partPath -> r) else None

  }

  /** An extractor that tests the request to see if it's an OPTIONS and if it
    * is, the path and the request are extracted. It can be used as:<br/>
    * <pre>case "api" :: id :: _ Options req => ...</pre><br/> or<br/> <pre>case
    * Options("api" :: id :: _, req) => ...</pre><br/> *
    */
  protected object Options {
    def unapply(r: Req): Option[(List[String], Req)] =
      if (r.requestType.options_?) Some(r.path.partPath -> r) else None

  }

  /** A function that chooses JSON or XML based on the request.. Use with
    * serveType
    */
  implicit def jxSel(req: Req): BoxOrRaw[JsonXmlSelect] =
    if (jsonResponse_?(req)) JsonSelect
    else if (xmlResponse_?(req)) XmlSelect
    else None

  /** Serve a given request by determining the request type, computing the
    * response and then converting the response to the given type (e.g., JSON or
    * XML).<br/><br/>
    * @param selection
    *   -- a function that determines the response type based on the Req.
    * @param pf
    *   -- a PartialFunction that converts the request to a response type (e.g.,
    *   a case class that contains the response).
    * @param cvt
    *   -- a function that converts from the response type to a the appropriate
    *   LiftResponse based on the selected response type.
    */
  protected def serveType[T, SelectType](
      selection: Req => BoxOrRaw[SelectType]
  )(
      pf: PartialFunction[Req, BoxOrRaw[T]]
  )(implicit cvt: PartialFunction[(SelectType, T, Req), LiftResponse]): Unit = {
    serve(new PartialFunction[Req, () => Box[LiftResponse]] {
      def isDefinedAt(r: Req): Boolean =
        selection(r).isDefined && pf.isDefinedAt(r)

      def apply(r: Req): () => Box[LiftResponse] =
        () => {
          pf(r).box match {
            case Full(resp) =>
              val selType = selection(r).openOrThrowException(
                "Full because pass isDefinedAt"
              )
              if (cvt.isDefinedAt((selType, resp, r)))
                Full(cvt((selType, resp, r)))
              else
                emptyToResp(
                  ParamFailure(
                    "Unabled to convert the message",
                    Empty,
                    Empty,
                    500
                  )
                )

            case e: EmptyBox => emptyToResp(e)
          }
        }
    })
  }

  /** Serve a request returning either JSON or XML.
    *
    * @param pf
    *   -- a Partial Function that converts the request into an intermediate
    *   response.
    * @param cvt
    *   -- convert the intermediate response to a LiftResponse based on the
    *   request being for XML or JSON. If T is JsonXmlAble, there are built-in
    *   converters. Further, you can return auto(thing) and that will invoke
    *   built-in converters as well. The built-in converters use Lift JSON's
    *   Extraction.decompose to convert the object into JSON and then
    *   Xml.toXml() to convert to XML.
    */
  protected def serveJx[T](pf: PartialFunction[Req, BoxOrRaw[T]])(implicit
      cvt: JxCvtPF[T]
  ): Unit =
    serveType(jxSel)(pf)(cvt)

  protected type JxCvtPF[T] =
    PartialFunction[(JsonXmlSelect, T, Req), LiftResponse]

  /** Serve a request returning either JSON or XML.
    *
    * @param pf
    *   -- a Partial Function that converts the request into Any (note that the
    *   response must be convertable into JSON vis Lift JSON
    *   Extraction.decompose
    */
  protected def serveJxa(pf: PartialFunction[Req, BoxOrRaw[Any]]): Unit =
    serveType(jxSel)(pf)(
      new PartialFunction[(JsonXmlSelect, Any, Req), LiftResponse] {
        def isDefinedAt(p: (JsonXmlSelect, Any, Req)) =
          convertAutoJsonXmlAble.isDefinedAt(
            (p._1, AutoJsonXmlAble(p._2), p._3)
          )

        def apply(p: (JsonXmlSelect, Any, Req)) =
          convertAutoJsonXmlAble.apply((p._1, AutoJsonXmlAble(p._2), p._3))
      }
    )

  /** Return the implicit Formats instance for JSON conversion
    */
  protected implicit def formats: Formats = olon.json.DefaultFormats

  /** The default way to convert a JsonXmlAble into JSON or XML
    */
  protected implicit lazy val convertJsonXmlAble
      : PartialFunction[(JsonXmlSelect, JsonXmlAble, Req), LiftResponse] = {
    case (JsonSelect, obj, _) => Extraction.decompose(obj)

    case (XmlSelect, obj, _) =>
      Xml.toXml(Extraction.decompose(obj)).toList match {
        case x :: _ => x
        case _      => Text("")
      }
  }

  /** The class that wraps anything for auto conversion to JSON or XML
    */
  protected case class AutoJsonXmlAble(obj: Any)

  /** wrap anything for autoconversion to JSON or XML
    */
  protected def auto(in: Any): Box[AutoJsonXmlAble] =
    Full(new AutoJsonXmlAble(in))

  /** Wrap a Box of anything for autoconversion to JSON or XML
    */
  protected def auto(in: Box[Any]): Box[AutoJsonXmlAble] =
    in.map(obj => new AutoJsonXmlAble(obj))

  /** An implicit conversion that converts AutoJsonXmlAble into JSON or XML
    */
  protected implicit lazy val convertAutoJsonXmlAble
      : PartialFunction[(JsonXmlSelect, AutoJsonXmlAble, Req), LiftResponse] = {
    case (JsonSelect, AutoJsonXmlAble(obj), _) =>
      Extraction.decompose(obj)
    case (XmlSelect, AutoJsonXmlAble(obj), _) =>
      Xml.toXml(Extraction.decompose(obj)).toList match {
        case x :: _ => x
        case _      => Text("")
      }
  }

  /** The stable identifier for JsonPost. You can use it as an extractor.
    */
  protected lazy val JsonPost = new TestPost[JValue] with JsonTest with JsonBody

  /** The stable identifier for XmlPost. You can use it as an extractor.
    */
  protected lazy val XmlPost = new TestPost[Elem] with XmlTest with XmlBody

  /** A trait that defines the TestPut extractor. Is the request a PUT, has JSON
    * or XML data in the put body and something that expects JSON or XML in the
    * response. Subclass this trait to change the behavior
    */
  protected trait TestPut[T] {

    /** Test to see if the request is a PUT, has JSON or XML data in the body
      * and expecting JSON or XML in the response. The path, Data and the Req
      * instance are extracted.
      */
    def unapply(r: Req): Option[(List[String], (T, Req))] =
      if (r.put_? && testResponse_?(r))
        body(r).toOption.map(b => (r.path.partPath -> (b -> r)))
      else None

    def testResponse_?(r: Req): Boolean

    def body(r: Req): Box[T]
  }

  /** The stable identifier for JsonPut. You can use it as an extractor.
    */
  protected lazy val JsonPut = new TestPut[JValue] with JsonTest with JsonBody

  /** The stable identifier for XmlPut. You can use it as an extractor.
    */
  protected lazy val XmlPut = new TestPut[Elem] with XmlTest with XmlBody

  /** Extract a Pair using the same syntax that you use to make a Pair
    */
  protected object -> {
    def unapply[A, B](s: (A, B)): Option[(A, B)] = Some(s._1 -> s._2)
  }

  @volatile private var _dispatch: List[LiftRules.DispatchPF] = Nil

  private lazy val nonDevDispatch = _dispatch.reverse

  private def dispatch: List[LiftRules.DispatchPF] =
    if (Props.devMode) _dispatch.reverse else nonDevDispatch

  /** Is the Rest helper defined for a given request
    */
  def isDefinedAt(in: Req) = dispatch.find(_.isDefinedAt(in)).isDefined

  /** Apply the Rest helper
    */
  def apply(in: Req): () => Box[LiftResponse] =
    dispatch.find(_.isDefinedAt(in)).get.apply(in)

  /** Add request handlers
    */
  protected def serve(
      handler: PartialFunction[Req, () => Box[LiftResponse]]
  ): Unit = _dispatch ::= handler

  /** Turn T into the return type expected by DispatchPF as long as we can
    * convert T to a LiftResponse.
    */
  protected implicit def thingToResp[T](in: T)(implicit
      c: T => LiftResponse
  ): () => Box[LiftResponse] = () => Full(c(in))

  /** If we're returning a future, then automatically turn the request into an
    * Async request
    * @param in
    *   the LAFuture of the response type
    * @param c
    *   the implicit conversion from T to LiftResponse
    * @tparam T
    *   the type
    * @return
    *   Nothing
    */
  protected implicit def asyncToResponse[AsyncResolvableType, T](
      asyncContainer: AsyncResolvableType
  )(implicit
      asyncResolveProvider: CanResolveAsync[AsyncResolvableType, T],
      responseCreator: T => LiftResponse
  ): () => Box[LiftResponse] = () => {
    RestContinuation.async(reply => {
      asyncResolveProvider.resolveAsync(
        asyncContainer,
        { resolved => reply(responseCreator(resolved)) }
      )
    })
  }

  /** If we're returning a future, then automatically turn the request into an
    * Async request
    * @param in
    *   the LAFuture of the response type
    * @param c
    *   the implicit conversion from T to LiftResponse
    * @tparam T
    *   the type
    * @return
    *   Nothing
    */
  protected implicit def asyncBoxToResponse[AsyncResolvableType, T](
      asyncBoxContainer: AsyncResolvableType
  )(implicit
      asyncResolveProvider: CanResolveAsync[AsyncResolvableType, Box[T]],
      responseCreator: T => LiftResponse
  ): () => Box[LiftResponse] = () => {
    RestContinuation.async(_ => {
      asyncResolveProvider.resolveAsync(
        asyncBoxContainer,
        { resolvedBox =>
          boxToResp(resolvedBox).apply() openOr NotFoundResponse()
        }
      )
    })
  }

  /** Turn a Box[T] into the return type expected by DispatchPF. Note that this
    * method will return messages from Failure() and return codes and messages
    * from ParamFailure[Int[(msg, _, _, code)
    */
  protected implicit def boxToResp[T](
      in: Box[T]
  )(implicit c: T => LiftResponse): () => Box[LiftResponse] =
    in match {
      case Full(v)     => () => Full(c(v))
      case e: EmptyBox => () => emptyToResp(e)
    }

  protected def emptyToResp(eb: EmptyBox): Box[LiftResponse] =
    eb match {
      case ParamFailure(msg, _, _, code: Int) =>
        Full(
          InMemoryResponse(
            msg.getBytes("UTF-8"),
            ("Content-Type" ->
              "text/plain; charset=utf-8") ::
              Nil,
            Nil,
            code
          )
        )

      case Failure(msg, _, _) =>
        Full(NotFoundResponse(msg))

      case _ => Empty
    }

  /** Turn an Option[T] into the return type expected by DispatchPF.
    */
  protected implicit def optionToResp[T](
      in: Option[T]
  )(implicit c: T => LiftResponse): () => Box[LiftResponse] =
    in match {
      case Some(v) => () => Full(c(v))
      case _       => () => Empty
    }

  /** Turn a () => Box[T] into the return type expected by DispatchPF. Note that
    * this method will return messages from Failure() and return codes and
    * messages from ParamFailure[Int[(msg, _, _, code)
    */
  protected implicit def boxFuncToResp[T](
      in: () => Box[T]
  )(implicit c: T => LiftResponse): () => Box[LiftResponse] =
    () => {
      in() match {
        case ParamFailure(msg, _, _, code: Int) =>
          Full(
            InMemoryResponse(
              msg.getBytes("UTF-8"),
              ("Content-Type" ->
                "text/plain; charset=utf-8") ::
                Nil,
              Nil,
              code
            )
          )

        case Failure(msg, _, _) =>
          Full(NotFoundResponse(msg))

        case Full(v) => Full(c(v))
        case _       => Empty
      }
    }

  /** Turn an Option[T] into the return type expected by DispatchPF.
    */
  protected implicit def optionFuncToResp[T](
      in: () => Option[T]
  )(implicit c: T => LiftResponse): () => Box[LiftResponse] =
    () =>
      in() match {
        case Some(v) => Full(c(v))
        case _       => Empty
      }

  /** Override this method to create an AppXmlResponse with the mime type
    * application/xml rather then text/xml
    */
  protected def createXmlResponse(in: scala.xml.Node): LiftResponse =
    XmlResponse(in)

  /** Convert a Node to an XmlResponse
    */
  protected implicit def nodeToResp(in: scala.xml.Node): LiftResponse =
    createXmlResponse(in)

  /** Convert a JValue to a LiftResponse
    */
  implicit def jsonToResp(in: JsonAST.JValue): LiftResponse =
    JsonResponse(in)

  /** Convert a JsExp to a LiftResponse
    */
  implicit def jsExpToResp(in: js.JsExp): LiftResponse =
    JavaScriptResponse(in.cmd)

  /** Convert a JsCmd to a LiftResponse
    */
  implicit def jsCmdToResp(in: js.JsCmd): LiftResponse =
    JavaScriptResponse(in)

  /** @return
    *   a SuperString with more available methods such as roboSplit or commafy
    */
  protected implicit def listStringToSuper(in: List[String]): SuperListString =
    new SuperListString(in)

  /** @return
    *   a SuperString with more available methods such as roboSplit or commafy
    */
  protected implicit def stringToSuper(in: String): SuperString =
    new SuperString(in)

  /** Allows you to use >> after a path list to handle all the cases where you
    * have a prefix for a series of differ suffixes with the same path prefix.
    * For example: <code> serve("foo" / "bar" >> { case baz :: Nil Post _ => ...
    * case Nil Get _ => ... }) </code>
    */
  protected implicit def listToServeMagic(in: List[String]): ListServeMagic =
    new ListServeMagic(in)

  /** Take an original piece of JSON (most probably, JObject and replace all the
    * JFields with those in toMerge
    */
  protected def mergeJson(original: JValue, toMerge: JValue): JValue = {
    def replace(lst: List[JField], f: JField): List[JField] =
      f :: lst.filterNot(_.name == f.name)

    original match {
      case JObject(fields) =>
        toMerge match {
          case JObject(otherFields) =>
            JObject(otherFields.foldLeft(fields)(replace(_, _)))
          case _ => original
        }

      case _ => original // can't merge
    }
  }
}

/** Do some magic to prefix path patterns with a single List
  */
final class ListServeMagic(list: List[String]) {
  val listLen = list.length

  /** Used in conjunction with RestHelper.serveJx to prefix the pattern matched
    * path. For example: <code> serveJx[Foo]("foo" / "bar" prefixJx { case
    * FindBaz(baz) :: Nil Get _ => baz }) </code>
    */
  def prefixJx[T](
      pf: PartialFunction[Req, BoxOrRaw[T]]
  ): PartialFunction[Req, BoxOrRaw[T]] =
    new PartialFunction[Req, BoxOrRaw[T]] {
      def isDefinedAt(req: Req): Boolean =
        req.path.partPath.startsWith(list) && {
          pf.isDefinedAt(req.withNewPath(req.path.drop(listLen)))
        }

      def apply(req: Req): BoxOrRaw[T] =
        pf.apply(req.withNewPath(req.path.drop(listLen)))
    }

  /** Used in conjunction with RestHelper.serveJx to prefix the pattern matched
    * path. For example: <code> serve("foo" / "bar" prefix { case FindBaz(baz)
    * :: Nil GetJson _ => baz: JValue case FindBaz(baz) :: Nil GetXml _ => baz:
    * Node }) </code>
    */
  def prefix(
      pf: PartialFunction[Req, () => Box[LiftResponse]]
  ): PartialFunction[Req, () => Box[LiftResponse]] =
    new PartialFunction[Req, () => Box[LiftResponse]] {
      def isDefinedAt(req: Req): Boolean =
        req.path.partPath.startsWith(list) && {
          pf.isDefinedAt(req.withNewPath(req.path.drop(listLen)))
        }

      def apply(req: Req): () => Box[LiftResponse] =
        pf.apply(req.withNewPath(req.path.drop(listLen)))
    }

}

/** A trait that can be mixed into an class (probably a case class) so that the
  * class can be converted automatically into JSON or XML
  */
trait JsonXmlAble

/** This trait is part of the ADT that allows the choice between
  */
sealed trait JsonXmlSelect

/** The Type for JSON
  */
final case object JsonSelect extends JsonXmlSelect

/** The type for XML
  */
final case object XmlSelect extends JsonXmlSelect
