package lm.service

import provider._
import delegator._

import net.liftweb.common._
import net.liftweb.json._
import net.liftweb.json.JsonParser.ParseException
import net.liftweb.http.{ LiftResponse, InMemoryResponse }

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration.DurationLong

import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import akka.pattern.after


trait AkkaHttpCaller extends HttpServiceDelegate with JsonResponseExceptionHelper {
  type Provider = AkkaHttpServiceProvider.type
  val serviceProvider = AkkaHttpServiceProvider

  //akka-http specific functions

  implicit val formats = DefaultFormats
  def parseResponse(response: Response)(implicit ec: ExecutionContext): Future[JValue] = serviceProvider.parseResponse(response)
  def callAndParse(request: Request)(implicit ec: ExecutionContext): Future[JValue] = for {
    response <- call(request)
    parse <- parseResponse(response)
  } yield parse

  def parseAndProcessJsonResponse[TargetType](response: Future[Response], transform: JValue => Option[TargetType])(implicit ec: ExecutionContext): Future[TargetType] = {
    val parsedResponse: Future[JValue] = response.flatMap { parseResponse _ }

    def responseAsException: Future[TargetType] = parsedResponse.map { jval: JValue => throw jval.toServiceErrorException }
    def processedResponse: Future[Option[TargetType]] = parsedResponse map { transform }
    processedResponse flatMap {
      case Some(t) => Future.successful(t)
      case _ => responseAsException
    }
  }

  def callAndTransformToLiftResponse(request: Request)(implicit ec: ExecutionContext): Future[LiftResponse] = {
    call(request).flatMap { _.toLiftResponseFuture() }
  }

  //helper functions
  private def httpEntityJson(json: JValue = JNothing): HttpEntity.Strict =
    HttpEntity(ContentTypes.`application/json`, compactRender(json))

  private def makeUriWithQuery(uri: String, params: Map[String, String] = Map()) =
    Uri(uri).withQuery(Uri.Query(params))

  def postJsonRequest(uri: String, json: JValue = JNothing): Request =
    HttpRequest(HttpMethods.POST, uri = Uri(uri), entity = httpEntityJson(json))

  def postJson(uri: String, json: JValue = JNothing)(implicit ec: ExecutionContext): Future[Response] = call(postJsonRequest(uri, json))

  def putJsonRequest(uri: String, json: JValue = JNothing): Request =
    HttpRequest(HttpMethods.PUT, uri = Uri(uri), entity = httpEntityJson(json))

  def putJson(uri: String, json: JValue = JNothing)(implicit ec: ExecutionContext): Future[Response] = call(putJsonRequest(uri, json))

  def getRequest(uri: String): Request = getRequest(uri, Map())
  def getRequest(uri: String, params: Map[String, String]): Request =
    HttpRequest(HttpMethods.GET, uri = makeUriWithQuery(uri, params))

  def get(uri: String, params: Map[String, String] = Map())(implicit ec: ExecutionContext): Future[Response] = call(getRequest(uri, params))

  def deleteRequest(uri: String, params: Map[String, String] = Map()): Request =
    HttpRequest(HttpMethods.DELETE, uri = makeUriWithQuery(uri, params))

  def delete(uri: String)(implicit ec: ExecutionContext): Future[Response] = call(deleteRequest(uri, Map()))

  def responseIsFailure(response: Response): Boolean = response.status.isFailure
  def responseIsSuccess(response: Response): Boolean = response.status.isSuccess
  def responseStatusInt(response: Response): Int = response.status.intValue

  implicit class ToLiftResponse(response: Response) {
    def toLiftResponseFuture
      (headers: List[(String, String)] = ("Content-Type", "application/json; charset=utf-8") :: Nil)
      (implicit ec: ExecutionContext): Future[LiftResponse] = {
      def statusCode = response.status.intValue
      def unmarshalled = serviceProvider.unmarshalToByteArray(response)
      unmarshalled map { bs: Array[Byte] => InMemoryResponse(bs, headers, Nil, statusCode) }
    }
  }
}

object AkkaHttpServiceProvider extends AkkaHttpServiceProvider
trait AkkaHttpServiceProvider extends HttpServiceProvider {

  type Request = HttpRequest
  type Response = HttpResponse

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  private val http = Http()

  def call(request: HttpRequest)(implicit ec: ExecutionContext): Future[HttpResponse] = {
    Logger("service").info(s"Service request (${request.method.name}) ${request.uri.path}")
    http.singleRequest(request)
  }
  def call(request: HttpRequest, timeoutMs: Int)(implicit ec: ExecutionContext): Future[HttpResponse] =
    Future.firstCompletedOf(
      call(request) ::
      after(timeoutMs.millis, system.scheduler)(Future.failed(new java.util.concurrent.TimeoutException)) ::
      Nil
    )

  def parseResponse(response: Response)(implicit ec: ExecutionContext): Future[JValue] = {
    for {
      unmarshed <- unmarshalToString(response)
    } yield parse(unmarshed)
  }.recover {
    case parseExc: ParseException => throw ServiceBadResponseException("Bad Response: not parsable")
  }
  def unmarshalToString(r: Response)(implicit ec: ExecutionContext): Future[String] = Unmarshal(r.entity).to[String]
  def unmarshalToByteArray(r: Response)(implicit ec: ExecutionContext): Future[Array[Byte]] = Unmarshal(r.entity).to[Array[Byte]]
}