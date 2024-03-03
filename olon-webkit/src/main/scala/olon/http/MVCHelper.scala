package olon
package http

import olon.common._
import olon.util._

import scala.language.reflectiveCalls
import scala.xml.Node
import scala.xml.NodeSeq

/** Mix this trait into a class to provide support for MVC style coding. Each
  * controller line is defined as: <pre name="code" class="scala"> serve { case
  * "user" :: User(user) :: _ => "#name" #> user.firstName } </pre>
  *
  * The above code matches /user/4, loads the user with primary key 4 from the
  * database, then applies the transform to the /user.html template replacing
  * the node with the id "name" with the firstName of the user
  */
trait MVCHelper extends LiftRules.DispatchPF {

  /** The partial function to match a request to a response
    */
  protected type MVCMatch = PartialFunction[List[String], MVCResponse]

  /** Serve an MVC page based on matching the path
    */
  protected def serve(pf: MVCMatch): Unit = { _dispatch ::= pf }

  @volatile private var _dispatch: List[MVCMatch] = Nil

  private lazy val nonDevDispatch = _dispatch.reverse

  private object curRequest extends RequestVar[Req](null) {
    override def __nameSalt = Helpers.nextFuncName
  }

  private object curSession
      extends RequestVar[LiftSession](
        S.session.openOr(LiftRules.statelessSession.vend.apply(curRequest.is))
      ) {
    override def __nameSalt = Helpers.nextFuncName
  }

  private def dispatch: List[MVCMatch] =
    if (Props.devMode) _dispatch.reverse else nonDevDispatch

  /** Is the Rest helper defined for a given request
    */
  def isDefinedAt(in: Req) = {
    S.session match {
      case Full(_) => dispatch.find(_.isDefinedAt(in.path.partPath)).isDefined

      case _ =>
        curRequest.set(in)
        S.init(Box !! in, curSession.is) {
          dispatch.find(_.isDefinedAt(in.path.partPath)).isDefined
        }
    }
  }

  /** Apply the Rest helper
    */
  def apply(in: Req): () => Box[LiftResponse] = {
    val path = in.path.partPath
    S.session match {
      case Full(_) =>
        val resp = dispatch.find(_.isDefinedAt(path)).get.apply(path).toResponse

        () => resp

      case _ =>
        S.init(Box !! in, curSession.is) {
          val resp =
            dispatch.find(_.isDefinedAt(path)).get.apply(path).toResponse

          () => resp
        }
    }
  }

  /** A trait that holds a response for the MVCHelper. Conversions exist from
    * Unit (just serve the template), CssBindFunc (do the substitution on the
    * template), NodeSeq (run the template), LiftResponse (send the response
    * back), or Box or Option of any of the above.
    */
  protected sealed trait MVCResponse {
    def toResponse: Box[LiftResponse]
  }

  private def templateForPath(req: Req): Box[NodeSeq] = {

    def tryIt(path: List[String]): Box[NodeSeq] = path match {
      case Nil => Empty
      case _ =>
        Templates(path) match {
          case ret @ Full(_) => ret
          case _             => tryIt(path.dropRight(1))
        }
    }

    tryIt(req.path.partPath)
  }

  object MVCResponse extends Loggable {
    implicit def unitToResponse(unit: Unit): MVCResponse = {
      logger.trace("Converting to response " + unit)
      new MVCResponse {
        val toResponse: Box[LiftResponse] =
          for {
            session <- S.session
            req <- S.request
            template <- templateForPath(req)
            resp <- session.processTemplate(Full(template), req, req.path, 200)
          } yield resp
      }
    }

    implicit def bindToResponse(bind: CssBindFunc): MVCResponse =
      new MVCResponse {
        val toResponse: Box[LiftResponse] =
          for {
            session <- S.session
            req <- S.request
            template <- templateForPath(req)
            resp <- session.processTemplate(
              Full(bind(template)),
              req,
              req.path,
              200
            )
          } yield resp
      }

    implicit def nsToResponse(nodes: Seq[Node]): MVCResponse = {
      new MVCResponse {
        val toResponse: Box[LiftResponse] =
          for {
            session <- S.session
            req <- S.request
            resp <- session.processTemplate(Full(nodes), req, req.path, 200)
          } yield resp
      }
    }

    implicit def respToResponse(resp: LiftResponse): MVCResponse =
      new MVCResponse {
        val toResponse: Box[LiftResponse] = Full(resp)
      }

    implicit def boxThinginy[T](
        box: Box[T]
    )(implicit f: T => MVCResponse): MVCResponse = new MVCResponse {
      val toResponse: Box[LiftResponse] = boxToResp(box)(f)
    }

    implicit def optionThinginy[T](
        box: Option[T]
    )(implicit f: T => MVCResponse): MVCResponse = new MVCResponse {
      val toResponse: Box[LiftResponse] = boxToResp(box)(f)
    }
  }

  /** Turn a Box[T] into the return type expected by DispatchPF. Note that this
    * method will return messages from Failure() and return codes and messages
    * from ParamFailure[Int[(msg, _, _, code)
    */
  protected implicit def boxToResp[T](
      in: Box[T]
  )(implicit c: T => MVCResponse): Box[LiftResponse] =
    in match {
      case Full(v)     => c(v).toResponse
      case e: EmptyBox => emptyToResp(e)
    }

  /** Convert an Empty into an appropriate LiftResponse. In the case of Failure,
    * you may want to display a particular error message to the user.
    */
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

  /** Validate what, if it passes validation, then redirect to the new URL, else
    * display the messages using S.error and redisplay the current page.
    */
  // SCALA3 Moving `validate` and `save` as argument since a custom type on the
  // fly is breaking the compiler
  protected def saveRedir(
      validate: () => List[FieldError],
      save: () => Boolean,
      where: String
  ) = () => {
    validate() match {
      case Nil =>
        save()
        S.redirectTo(where)
      case xs =>
        S.error(xs)
    }
  }
}
