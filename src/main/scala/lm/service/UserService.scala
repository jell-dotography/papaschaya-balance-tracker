package lm.service

import net.liftweb.json.JValue
import net.liftweb.json.JsonDSL._
import net.liftweb.util.Props
import net.liftweb.util.Helpers._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object UserService extends AkkaHttpCaller {
	type UserType = User
	type IdType = Long

	private val serviceUri = Props.get("service.user.uri") openOrThrowException("UserService base URL Not Set. Please set in props file.")
	override def defaultTimeoutMs = Props.get("service.user.timeout").flatMap { str => tryo(str.toInt) } openOr super.defaultTimeoutMs
  
  def authenticate(email: String, pass: String): Future[UserType] = {
  	val uri = serviceUri + "/user/authenticate"
  	val jsonBody:JValue = 
  		("email" -> email) ~
      ("password" -> pass)

    parseAndProcessJsonResponse(
    	postJson(uri, jsonBody),
    	{j: JValue => (j \ "user").extractOpt[UserType]}
    ) 
  }

  def getPermission(uid: IdType): Future[List[PermissionWrapper]] = {
  	val uri = serviceUri + s"/user/$uid/permissions"
  	parseAndProcessJsonResponse(
  		get(uri),
  		{ j: JValue => (j \ "permissions").extractOpt[List[PermissionWrapper]] }
  	)
  }
}

final case class User(
  id: Long,
  firstName: String,
  lastName: String,
  email: String,
  locale: String,
  timezone: String,
  superUser: Boolean,
  validated: Boolean)

final case class PermissionWrapper(
  permission: Permission)
final case class Permission(
  id: Long,
  name: String,
  description: Option[String])