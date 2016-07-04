package lm

import net.liftweb.common._
import net.liftweb.http.{ LiftRules, Bootable }

import lm.jwt.{ Uid, JwtAuthentication }
import net.liftweb.http.Req
import net.liftweb.http.GetRequest
import net.liftweb.http.auth.AuthRole
import net.liftweb.http.auth.userRoles

class Boot extends Bootable with Loggable {

  override def boot(): Unit = {

    /*
     * Not necessary since we will log them ourselves
     */
    LiftRules.logServiceRequestTiming = false

    /**
     * Any api "modules" would be registered at boot
     */
    API.register("test", Test)

    /*
     * This whole app is nothing but a stateless dispatch
     * to an API which is registered here.
     */
    LiftRules.statelessDispatch.append(API)

    Metrics.report()

    // example authorization
    LiftRules.httpAuthProtectedResource.append({
      case Req(List(prefix, "auth"), _, GetRequest) => Full(AuthRole("auth"))
    })

    // example authentication
    LiftRules.authentication = lm.jwt.JwtAuthentication("Cus") {
      case (jwt, req) => {
        (for {
          uid <- jwt.getClaim[Uid]
        } yield {
          val check = (uid.value == 1)
          if (check)
            userRoles(AuthRole("auth"))
          check
        }).getOrElse(false)
      }
      case _ => false
    }

  }

}
