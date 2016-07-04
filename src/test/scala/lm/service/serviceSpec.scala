package lm.service

import org.specs2.matcher._
import org.specs2.mutable._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import JsonResponseExceptionHelper._
import TimeoutExceptionJsonHelper._

class ServiceSpec extends Specification {
	implicit val formats = DefaultFormats
	"trait JsonResponseExceptionHelper" >> {
		"convert expected json to ServiceFailResponseException" >> {
			val json = ("errors" -> List("hallo","hi")) ~ ("code" -> 418)
			val ex = json.toServiceErrorException 
			ex must haveClass[ServiceFailResponseException]
			ex.msgs must_== List("hallo","hi")
			ex.code must_== 418
		}
		"convert unexpected json to ServiceBadResponseException" >> {
			val json = ("hoho" -> "1234") ~ ("errors" -> "hai")
			val ex = json.toServiceErrorException("unexpected json")
			ex must haveClass[ServiceBadResponseException]
			ex.msgs.size must be_>= (1)
			ex.msgs.head.toLowerCase must contain ("unexpected json")
			ex.code must_== 502
		}
	}

	"implicit class TimeoutExceptionToJson" >> {
		"can convert to ErrorResponse json" >> {
			val tox = new java.util.concurrent.TimeoutException()
			val json = tox.toJson
			(json \ "errors") must beLike {
				case JArray(elms) => {
					elms.size must_== 1
					elms.head.extract[String].toLowerCase must contain("timeout")
				}
			}
			(json \ "code").extract[Int] must_== 504
		}
	}

	"class ServiceErrorException" >> {
		val msgs = Seq("one","two")
		val code = 404
		val e = ServiceErrorException( msgs, code )
		val expectedJson = ("errors" -> msgs) ~ ("code" -> code)
		"can transform to json" >> {
			e.toJson must_== expectedJson
		}
		"can transform to json with extra fields" >> {
			val j2 = e.toJson(("param" -> 123) ~ ("errors" -> List("extra")))
			(j2 \ "errors") must beLike {
				case JArray(elms) => {
					elms.size must_== msgs.size + 1
					elms must contain (JString("extra"))
				}
			}
			(j2 \ "param").extract[Int] must_== 123
		}
	}
}