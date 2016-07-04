package lm

import net.liftweb.common._
import net.liftweb.http.{ InternalServerErrorResponse, ResponseWithReason, Req, LiftResponse }
import net.liftweb.http.rest.RestHelper
import net.liftweb.util.LiftFlowOfControlException

/**
 * The API is the main entry point for registering handlers
 */
object API extends RestHelper {

  /**
   * A helper method that aids in registering handlers with the API
   * by prefix.  Handlers that are registered this way will automatically
   * have their invocations logged and metrics published on success/failure counts
   * and completion times.
   *
   * @param prefix The path component that will route to this handler
   * @param handler Usually a RestHelper that the request is passed to
   * @return This method returns the API object for call chaining
   */
  def register(prefix: String,
    handler: PartialFunction[Req, () => Box[LiftResponse]]): this.type = register(List(prefix), handler)

  def register(handler: PartialFunction[Req, () => Box[LiftResponse]]): this.type = register(Nil, handler)

  def register(prefixes: List[String],
    handler: PartialFunction[Req, () => Box[LiftResponse]]): this.type = {

    serve {

      prefixes prefix new PartialFunction[Req, () => Box[LiftResponse]] {

        lazy val logger = Logger(if (prefixes.isEmpty) "api" else s"api.${prefixes.mkString(".")}")

        override def isDefinedAt(req: Req): Boolean = handler.isDefinedAt(req)

        override def apply(req: Req): () => Box[LiftResponse] = {
          val context = Metrics.timer(
            s"Request Time path=${req.path.partPath.mkString("/")}*").time()
          val start = System.currentTimeMillis()
          var success = true
          try {
            handler.apply(req)
          } catch {
            case e: Exception if !e.isInstanceOf[LiftFlowOfControlException] =>
              logger.error(s"Failed ${req.requestType.method} to ${req.uri}", e)
              success = false
              () =>
                new Full(ResponseWithReason(
                  new InternalServerErrorResponse(), "Internal error occurred"))
          } finally {
            context.stop()
            Metrics.meter(s"${if (success) "Success" else "Failure"} Count method=${req.requestType.method} path=${req.path.partPath.mkString("/")}*").mark()
            logger.info(s"${if (success) "Successful" else "Failed"} ${req.requestType.method} to ${req.uri} in ${System.currentTimeMillis() - start} millis")
          }
        }

      }
    }

    this

  }

}
