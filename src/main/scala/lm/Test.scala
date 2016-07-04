package lm

import java.util.UUID

import net.liftweb.common.Loggable
import net.liftweb.http.rest.{ RestContinuation, RestHelper }
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import lm.jwt.JwtCodec

/**
 * A simple example handler.  When an API
 * request starts with:
 *
 * /{module_name}/echo, the rest of the path is turned into a JSON
 * array and echoed back to the client.
 * For instance /{module}/echo/this/path would
 * result in ["this", "path"] as a response.
 *
 * /{module_name}/error, an exception will be thrown to test the
 * logging and metrics collection and return on errors
 *
 * /{module_name}/randTime, the system will pause between 0 - 500ms
 * before responding with JSON true for testing metric collection on
 * response times
 *
 * /{module_name}/id, the system will report on its identity.  The
 * identity consists of a JSON object containing the following keys.
 * name: The app name
 * version: The app version
 * uuid: A unique ID that identifies this instance within the cluster
 */
object Test extends RestHelper with Loggable {

  lazy val uuid = UUID.randomUUID().toString

  serve {

    case Get("echo" :: path, _) =>
      JArray(path.map(JString(_)))

    case Get("error" :: Nil, _) =>
      throw new Exception("An error occurred!")

    case Get("randTime" :: Nil, _) =>
      Thread.sleep((Math.random() * 500).toInt)
      JBool(true)

    case Get("async" :: Nil, _) =>
      RestContinuation.async { resp =>
        try {
          respondLater.onComplete {
            case Success(v) => resp(JString(v))
            case _ => throw new Exception("Yikes!")
          }
        } catch {
          case e: Exception =>
            logger.error("Could not respond asynchronously")
        }
      }

    case Get("id" :: nil, _) =>
      JObject(Nil) ~
        ("name" -> System.getenv("LiftMicroservicesName")) ~
        ("version" -> System.getenv("LiftMicroservicesVersion")) ~
        ("group" -> System.getenv("LiftMicroservicesGroup")) ~
        ("uuid" -> JString(uuid))

    // example gen token
    case Get("token" :: Nil, _) =>
      JObject(Nil) ~ ("token" -> JwtCodec.apply("name", 1))

    //example check authorization
    case Get("auth" :: Nil, _) =>
      JObject(Nil) ~ ("message" -> "Ok.")

  }

  def respondLater: Future[String] = Future {
    Thread.sleep((Math.random() * 2000).toInt)
    "ok!"
  }

}
