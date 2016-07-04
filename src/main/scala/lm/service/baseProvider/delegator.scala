package lm.service.delegator


import lm.service.provider.HttpServiceProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait HttpServiceDelegate extends HttpServiceProvider {
  
  type Provider <: HttpServiceProvider
  type Request = serviceProvider.type#Request
  type Response = serviceProvider.type#Response
  val serviceProvider: Provider
  
  protected def defaultTimeoutMs: Int = 7000

  def call(request:Request)(implicit ec: ExecutionContext): Future[Response] = serviceProvider.call(request, defaultTimeoutMs)
  def call(req: Request, timeoutMs: Int)(implicit ec: ExecutionContext): Future[Response] = serviceProvider.call(req, timeoutMs)
}