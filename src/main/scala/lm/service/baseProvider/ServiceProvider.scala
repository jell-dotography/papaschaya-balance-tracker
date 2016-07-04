package lm.service.provider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext


trait HttpServiceProvider{
  type Request
  type Response
  def call(req: Request)(implicit ec: ExecutionContext): Future[Response]
  def call(req: Request, timeoutMs: Int)(implicit ec: ExecutionContext): Future[Response]
}