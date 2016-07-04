package lm.service

import org.specs2.matcher._
import org.specs2.mutable._
import org.specs2.concurrent.ExecutionEnv

import scala.util.{Try, Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ServiceProviderSpec(implicit ee:ExecutionEnv) extends Specification {
	"dummy service caller" >> {
		"make calls" >> {
			val response = DummyServiceCaller.call("hello")
			response must contain("success").await
			response must contain("hello").await
		}
		"returns Failed future on failure" >> {
			val response = DummyServiceCaller.call("! nope")
			response must throwAn[DummyServiceProviderException].await
		}
	}
}