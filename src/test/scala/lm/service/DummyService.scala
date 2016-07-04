package lm.service

import provider.HttpServiceProvider
import delegator.HttpServiceDelegate

import scala.concurrent.{ Future, ExecutionContext }

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object DummyServiceCaller extends DummyServiceCaller
trait DummyServiceCaller extends HttpServiceDelegate {
	val serviceProvider = DummyServiceProvider
	type Provider = DummyServiceProvider.type
}

class  DummyServiceProviderException(val msg: String) extends Exception(msg)
object DummyServiceProviderException {
	def apply(msg: String) = new DummyServiceProviderException(msg)
	def unapply(d: DummyServiceProviderException): Option[String] = Some(d.msg)
}

object DummyServiceProvider extends DummyServiceProvider
trait  DummyServiceProvider extends HttpServiceProvider {
  type Request = String
  type Response = String

  def call(req: Request)(implicit ec: ExecutionContext): Future[Response] = req.headOption match {
    case None => Future.failed {
      new DummyServiceProviderException("failed with empty request")
    }
    case Some('!') => Future.failed {
      new DummyServiceProviderException("failed with request: " + req)
    }
    case _ => Future.successful {
      scala.concurrent.blocking(Thread.sleep(50L))
      "dummy call success!: " + req
    }
  }
  def call(request: Request, timeoutMs: Int)(implicit ec: ExecutionContext): Future[Response] = call(request)
}