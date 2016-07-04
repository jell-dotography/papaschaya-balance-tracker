package lm.jwt

import io.igl.jwt._
import scala.util.Try
import net.liftweb.util.Helpers._
import net.liftweb.util.Props

/*
	IMPORTANT cannot decode utf-8 claims correctly

*/

object JwtCodec {
  private def secret = Props.get("jwt.secret").openOrThrowException("No secret set in Props")
  private def now: Long = System.currentTimeMillis / 1000
  private val tokenLifeSecond: Long = Props.get("jwt.tokenLifeSecond").flatMap { str: String => tryo(str.toLong) }.openOr(3600L)
  private val alg = Algorithm.HS256

  def encode(cs: ClaimSet): String = new DecodedJwt(
    Seq(
      Alg(alg),
      Typ("JWT")),
    cs.toSeq).encodedAndSigned(secret)

  def decode(jwt: String): Try[Jwt] = DecodedJwt.validateEncodedJwt(
    jwt,
    secret,
    alg,
    Set(Typ),
    ClaimSet.expected)

  def decodeAndGetClaimSet(jwt: String): Try[ClaimSet] = decode(jwt) map { decoded =>
    ClaimSet(decoded) match {
      case Some(cs) => cs
      case None => throw new IllegalArgumentException("The jwt does not contain all the required claims.")
    }
  }

  def apply(name: String, uid: Long): String = encode(ClaimSet(name, uid, now + tokenLifeSecond))
  def apply(cs: ClaimSet): String = encode(cs)
}