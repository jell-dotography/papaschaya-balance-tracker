package lm.jwt

import org.specs2.matcher._
import org.specs2.mutable._
import org.springframework.mock.web.MockHttpServletRequest

import net.liftweb.http.Req
import net.liftweb.http.auth.HttpAuthentication
import net.liftweb.http.provider.servlet.HTTPRequestServlet
import net.liftweb.json._

class JwtAuthenticationSpec
    extends Specification {

  // Define JWT Authentication
  private val jwtAuth = JwtAuthentication("test") {
    case _ => true
  }

  // Test authentication response
  private def requireResponse(errorMessage: String, code: Int = 401): Matcher[HttpAuthentication] = { authentication: HttpAuthentication =>
    val response = authentication.unauthorizedResponse.toResponse
    val j = parse(new String(response.data, "UTF-8"))
    j \ "errors" must haveClass[JArray]
    (j \ "errors").children must haveLength(1)
    (j \ "errors").children must contain(JString(errorMessage))
    j \ "code" must haveClass[JInt]
    j \ "code" must_== JInt(code)

    response.code must_== code
  }

  "Req without token return false" >> {
    jwtAuth.verified_?(MockRequest("test/auth")) must_== false
    jwtAuth must requireResponse("Invalid token.", 401)
  }

  "Req with token return true" >> {
    val token: String = JwtCodec.apply("my name", 1) // Gen new token
    val mReq = new MockHttpServletRequest("Get", "test/path")
    mReq.addHeader("Authorization", token)
    jwtAuth.verified_?(MockRequest(mReq)) must_== true
    jwtAuth must requireResponse("Invalid token.", 401)
  }

  "Req with token invalid return false" >> {
    val token: String = "this is token invalid." // Token invalid
    val mReq = new MockHttpServletRequest("Get", "test/path")
    mReq.addHeader("Authorization", token)
    jwtAuth.verified_?(MockRequest(mReq)) must_== false
    jwtAuth must requireResponse("Invalid token.", 401)
  }

  "Req with token exp return false" >> {
    val token: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoibmFtZSIsInVpZCI6MSwiZXhwIjoxNDYxMzIyNjU0fQ.tZ8H9ehlazPzOXIt5sFVaGiDTuz7NGRqsSJS7-Iksdo" // Token expired.
    val mReq = new MockHttpServletRequest("Get", "test/path")
    mReq.addHeader("Authorization", token)
    jwtAuth.verified_?(MockRequest(mReq)) must_== false
    jwtAuth must requireResponse("Invalid token.", 401)
  }
}

object MockRequest {
  def apply(path: String, method: String = "GET"): Req = {
    val mMock = new MockHttpServletRequest(method, path)
    this(mMock)
  }
  def apply(mMock: MockHttpServletRequest): Req = {
    val request = new HTTPRequestServlet(mMock, null)
    Req(request, Nil, System.nanoTime)
  }
}
