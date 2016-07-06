package lm


import java.sql.Connection
import javax.naming.InitialContext
import net.liftweb.common._
import net.liftweb.http.{ LiftRules, Bootable }

import com.dotography.balancetracker.api.BalanceTrackerApi
import javax.sql.DataSource
import lm.jwt.{ Uid, JwtAuthentication }
import net.liftweb.http.Req
import net.liftweb.db.ConnectionManager
import net.liftweb.http.GetRequest
import net.liftweb.http.auth.AuthRole
import net.liftweb.http.auth.userRoles
import net.liftweb.mapper.DB
import net.liftweb.util.ConnectionIdentifier
import net.liftweb.util.DefaultConnectionIdentifier

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
    API.register("balancetracker", BalanceTrackerApi)

    /*
     * This whole app is nothing but a stateless dispatch
     * to an API which is registered here.
     */
    LiftRules.statelessDispatch.append(API)
    LiftRules.addToPackages("com.dotography.balancetracker")

//    Metrics.report()
    

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

    DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)
  }
  
  object DBVendor extends ConnectionManager {
    val ic : InitialContext = new InitialContext()
    val ds : DataSource = ic.lookup("java:comp/env/jdbc/MySQLDS").asInstanceOf[DataSource]
    
    def newConnection(name : ConnectionIdentifier) = {
      try {
        Full(ds.getConnection)
      } catch {
        case e : Exception => Empty
      }
    }
    
    def releaseConnection(con : Connection) {
      con.close
    }
  }

}
