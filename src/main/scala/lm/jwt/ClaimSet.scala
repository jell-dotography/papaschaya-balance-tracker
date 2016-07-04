package lm.jwt

import io.igl.jwt._
import play.api.libs.json._

//TO MANAGE: what fields are expected in the JWT claimset
case class ClaimSet(name: Name, uid: Uid, exp: Exp) {
  def toSeq = Seq(name, uid, exp)
}

object ClaimSet {
  def apply(n: String, u: Long, e: Long): ClaimSet = ClaimSet(Name(n), Uid(u), Exp(e))
  def apply(jwt: Jwt): Option[ClaimSet] = for {
    n <- jwt.getClaim[Name]
    u <- jwt.getClaim[Uid]
    e <- jwt.getClaim[Exp]
  } yield apply(n, u, e)
  // def unapply(cs: ClaimSet): Option[ (String, String, Long) ] = Some((cs.name.value, cs.uid.value, cs.exp.value))
  def expected: Set[ClaimField] = Set(Name, Uid, Exp)
}

case class Name(value: String) extends ClaimValue {
  override val field: ClaimField = Name
  override val jsValue: JsValue = JsString(value)
}

object Name extends ClaimField {
  override def attemptApply(value: JsValue): Option[ClaimValue] =
    value.asOpt[String].map(apply)
  override val name: String = "name"
}

case class Uid(value: Long) extends ClaimValue {
  override val field: ClaimField = Uid
  override val jsValue: JsValue = JsNumber(value)
}

object Uid extends ClaimField {
  override def attemptApply(value: JsValue): Option[ClaimValue] =
    value.asOpt[Long].map(apply)
  override val name: String = "uid"
}

