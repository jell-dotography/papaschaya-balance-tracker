package lm.jwt

import org.specs2.matcher._
import org.specs2.mutable._

import net.liftweb.util.Props
import net.liftweb.json._
import scala.util.{ Try, Success, Failure }
import java.util.Base64
import java.nio.charset.StandardCharsets
import io.igl.jwt._

class JwtCodecSpec extends Specification {
	
	"JwtCodec" >> {
		//val name = "asdกขคงbnm"  current library DOES NOT fully support utf-8
		val name = "qwerty123456*^"
		val uid = 44L
		def now:Long = System.currentTimeMillis / 1000
		val cs = ClaimSet( name, uid , now + 180)
		def getClaimJsonFromJwtString(str: String): Option[JValue] = {
			val claimStr = str.split('.').lift(1)
			claimStr map {s => parse(new String(Base64.getDecoder.decode(s), "UTF-8"))}
		}
		def secret = Props.get("jwt.secret").openOr("No secret set in Props")

		"encode ClaimSet to JWT String" >> {
			val str = JwtCodec(cs)
			val jval: Option[JValue] = getClaimJsonFromJwtString(str)
			jval must beLike {
				case Some(j) => {
					(j \ "name").values must_== name
					(j \ "uid").values must_== uid
				}
			}
		}
		"decode valid JWT String to JWT" >> {
			val str = JwtCodec(cs)
			val jwt = JwtCodec.decode(str)
			jwt must beLike {
				case Success(j) => {
					j.getClaim[Name] must_== Some(Name(name))
					j.getClaim[Uid] must_== Some(Uid(uid))
				}
			}
		}
		
		"decode corrupt JWT String to Failure" >> {
			val str = Base64.getEncoder.encodeToString("someRandomString".getBytes)
			JwtCodec.decode(str) must beLike {
				case Failure(e: Exception) => e must haveClass[IllegalArgumentException]
			}
		}


		"decode JWT with wrong claims to Failure" >> {
			val jwtStr = new DecodedJwt(
				Seq(
					Alg(Algorithm.HS256), 
					Typ("JWT")), 
				Seq(
					Name(name))
				).encodedAndSigned(secret)

			JwtCodec.decode(jwtStr) must beLike {
				case Failure(e: Exception) => e must haveClass[IllegalArgumentException]
			}
		}
		"decodeAndGetClaimSet from valid JWT Success as ClaimSet result" >> {
			val str = JwtCodec(cs)
			val claimset = JwtCodec.decodeAndGetClaimSet(str)
			claimset must beLike {
				case Success(c: ClaimSet) => {
					c.name.value must_== name
					c.uid.value must_== uid
				}
			}
		}
	}
}