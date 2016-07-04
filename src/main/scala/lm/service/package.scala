package lm.service

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import java.util.concurrent.TimeoutException

  //for extraction
  // lib ver 0.0.5c
case class ErrorResponse(errors: Seq[String], code: Int) {
  def toJson: JValue = ("errors" -> errors) ~ ("code" -> code)
}

  // lib ver 0.0.5b: field name "error" instead of "errors"
/*  case class ErrorResponse(error: Seq[String], code: Int) {
    def toErrorResponse: ErrorResponse = ErrorResponse(error, code)
  }*/

//Helper trait
object JsonResponseExceptionHelper extends JsonResponseExceptionHelper
trait JsonResponseExceptionHelper {
  implicit class ToServiceErrorException(json: JValue) {
    implicit val formats = DefaultFormats
    def toServiceErrorException: ServiceErrorException = toServiceErrorException("Unexpected Json received from service")
    def toServiceErrorException(msg: String): ServiceErrorException = {
      json.extractOpt[ErrorResponse] match {
        case Some(er: ErrorResponse) if er.errors.length > 0 => ServiceFailResponseException(er)
        case _ => ServiceBadResponseException(msg)
      }
    }
  }
}

object TimeoutExceptionJsonHelper extends TimeoutExceptionJsonHelper
trait TimeoutExceptionJsonHelper {
  implicit class TimeoutExceptionToJson(e: TimeoutException) {
    def code = 504
    def toJson: JValue = toJson("Service timeout. Please try again later")
    def toJson(msg: String): JValue = ErrorResponse(List(msg), code).toJson
  }
}
//Generic Service Error
class ServiceErrorException(val msgs: Seq[String], val code: Int) extends Exception(msgs.mkString("[ ", ", ", " ]")) {
  def toJson: JValue = ErrorResponse(msgs, code).toJson
  def toJson(j: JValue): JValue =
    toJson merge j
}
object ServiceErrorException {
  def apply(msgs: Seq[String], code: Int) = new ServiceErrorException(msgs, code)
  def apply(msg: String, code: Int) = new ServiceErrorException(Seq(msg), code)
  def apply(er: ErrorResponse) = new ServiceErrorException(er.errors, er.code)
  /*def apply(json: JValue)(implicit formats: Formats, mf: Manifest[ErrorResponse]): Option[ServiceErrorException] =
		json.extractOpt[ErrorResponse].map {er: ErrorResponse => apply(er)}*/
  def unapply(see: ServiceErrorException): Option[(Seq[String], Int)] = Some((see.msgs, see.code))
}
//For when service respond error (4xx or 5xx) response with understandable body
class ServiceFailResponseException(override val msgs: Seq[String], override val code: Int) extends ServiceErrorException(msgs, code)
object ServiceFailResponseException {
  def apply(msgs: Seq[String], code: Int) = new ServiceFailResponseException(msgs, code)
  def apply(er: ErrorResponse) = new ServiceFailResponseException(er.errors, er.code)
  def unapply(sfre: ServiceFailResponseException): Option[(Seq[String], Int)] = Some((sfre.msgs, sfre.code))
}

//For when service respond with unexpected/incomprehensible body content
class ServiceBadResponseException(override val msgs: Seq[String]) extends ServiceErrorException(msgs, 502)

object ServiceBadResponseException {
  def apply(msg: String) = new ServiceBadResponseException(Seq(msg))
  def apply(msgs: Seq[String]) = new ServiceBadResponseException(msgs)
  def unapply(sbre: ServiceBadResponseException): Option[Seq[String]] = Some(sbre.msgs)
}