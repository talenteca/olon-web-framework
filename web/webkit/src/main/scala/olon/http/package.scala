package olon

import scala.xml.Comment
import scala.xml.NodeSeq

import builtin.comet.AsyncRenderComet
import http.js.JsCmds.Replace
import util._

package object http {

  /** Provides support for binding anything that has a `CanResolveAsync`
    * implementation. Out of the box, that's just Scala `Future`s and
    * `LAFuture`s, but it could just as easily be, for example, Twitter
    * `Future`s if you're using Finagle; all you have to do is add a
    * `CanResolveAsync` implicit for it.
    */
  implicit def asyncResolvableTransform[ResolvableType, ResolvedType](implicit
      asyncResolveProvider: CanResolveAsync[ResolvableType, ResolvedType],
      innerTransform: CanBind[ResolvedType]
  ) = {
    new CanBind[ResolvableType] {
      def apply(resolvable: => ResolvableType)(ns: NodeSeq): Seq[NodeSeq] = {
        val placeholderId = Helpers.nextFuncName
        AsyncRenderComet.setupAsync

        val concreteResolvable: ResolvableType = resolvable

        S.session.map { session =>
          // Capture context now.
          val deferredRender =
            session.buildDeferredFunction((resolved: ResolvedType) => {
              AsyncRenderComet.completeAsyncRender(
                Replace(placeholderId, innerTransform(resolved)(ns).flatten)
              )
            })

          // Actually complete the render once the future is fulfilled.
          asyncResolveProvider.resolveAsync(
            concreteResolvable,
            resolvedResult => deferredRender(resolvedResult)
          )

          <div id={placeholderId}><img src={
            s"${LiftRules.assetRootPath}images/ajax-loader.gif"
          } alt="Loading" /></div>
        } openOr {
          Comment(
            "FIX" + "ME: Asynchronous rendering failed for unknown reason."
          )
        }
      }
    }
  }
}
