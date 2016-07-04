package lm.jwt

import scala.util._
import net.liftweb.http._
import net.liftweb.http.auth.HttpAuthentication
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import io.igl.jwt._
import net.liftweb.common.Logger

case class JwtAuthentication(realmName: String)(func: PartialFunction[(Jwt, Req), Boolean]) extends HttpAuthentication {

  override def realm = realmName

  override def unauthorizedResponse: UnauthorizedResponse = new UnauthorizedResponse(realm) {
    override def toResponse = {
      val res: JValue = ("code" -> 401) ~ ("errors" -> Seq("Invalid token."))
      JsonResponse(res, 401).toResponse.asInstanceOf[InMemoryResponse]
    }

  }

  override def verified_? : PartialFunction[Req, Boolean] = {
    case (req) => {
      header(req).flatMap(auth => {
        val headerAuth = new String(auth.getBytes)
        val jwt = JwtCodec.decode(headerAuth)
        jwt match {
          case Success(x) => Some(func(x, req))
          case _ => Some(false)
        }
      }).openOr(false)
    }
  }

}
