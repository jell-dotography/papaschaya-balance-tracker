package lm.service

import net.liftweb.http.{ LiftResponse, InMemoryResponse }
import net.liftweb.json._

import org.specs2.matcher._
import org.specs2.mutable._
import org.specs2.concurrent.ExecutionEnv

import scala.util.{Try, Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.model._

class AkkaHelperFnSpec(implicit ee:ExecutionEnv) extends Specification with AkkaHttpCaller {
	
	"AkkaHttpCaller (helper functions only)".title

	//test only helper functions. 
	//http call function tests would require another server to respond. not good in this scope
	val jsonStr = """
			{
				"a": [1,2,3],
				"b": "Salam"
			}
			"""

	val entity = HttpEntity(ContentTypes.`application/json`, jsonStr)
	val response: Response = HttpResponse(entity = entity)


	"parse response to JValue" >> {
		val parsed = parseResponse(response) 
		parsed must beLike[JValue] {
			case j:JValue => {
				(j \ "a") must haveClass[JArray]
				(j \ "b") must_== JString("Salam")
			}
		}.await
	}
	"convert to LiftResponse" >> {
		val transformed = response.toLiftResponseFuture()
		transformed must beLike[LiftResponse] {
			case InMemoryResponse(data, headers, cookies, code) => {
				data.size must_!= 0
				code must_== 200
			}
		}.await
	}
	"check response is failure" >> {responseIsFailure(response) must beFalse}
	"check response is success" >> {responseIsSuccess(response) must beTrue}
	"get http response code" >> {responseStatusInt(response) must_== 200}

	"method parseAndProcessJsonResponse" >> {
		val fResponse = Future.successful(response)
		val fFailed = Future.failed{ new Exception("dummy")}
		"can parse valid response"	>> {
			val transformed = parseAndProcessJsonResponse(fResponse, {j: JValue => Some(j)})
			transformed must beLike[JValue] {
				case j:JValue => {
					(j \ "a") must haveClass[JArray]
					(j \ "b") must_== JString("Salam")
				}
			}.await
		}
		"does not alter failure response" >> {
			val failureTransformed = parseAndProcessJsonResponse(fFailed, {j: JValue => Some(j)})
			failureTransformed must throwAn[Exception].await
		}
		"transform success to a Future of another type" >> {
			val transformed = parseAndProcessJsonResponse(fResponse, {
				j: JValue => (j \ "a").extractOpt[List[Int]]
			})
			transformed must beEqualTo(List(1,2,3)).await
		}
		"transform success to Failure with ServiceErrorException if jvalue transformation is Empty" >> {
			val transformed = parseAndProcessJsonResponse(fResponse, { _ => None})
			transformed must throwAn[ServiceErrorException].await
		}
	}
}