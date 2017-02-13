/*
 * This sample app demonstrates the GDAX library. All calls are completely
 * async, based on scala Futures.
 *
 * To run from the command-line:
 *
 * APIKEY=... APISECRET=... APIPASSPHRASE=... sbt run
 */
import GDAX._

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Sample extends App {
  println("This is a sample app showing basic usage.")

  // The number of tests we do below. When this reaches 0 the main thread
  // exits.
  var tests = 14

  val pubclient = api.Client.public()

  val productsHandler = (v: Option[Any]) => {
    v.foreach( (el: Any ) => {
      el.asInstanceOf[List[Map[Any,Any]]].foreach {
        (m: Map[Any, Any]) => {
          m("id") match {
            case "BTC-USD" => {
              val id = m("id").asInstanceOf[String]
              pubclient.productOrderBook(id,"2").onComplete {
                  case Success(resp) => { tests -= 1; println(resp + "\n^productOrderBook\n") }
                  case Failure(e) => e.printStackTrace
              }
              pubclient.productTicker(id).onComplete {
                  case Success(resp) => { tests -= 1; println(resp + "\n^productTicker\n") }
                  case Failure(e) => e.printStackTrace
              }
              pubclient.productTrades(id).onComplete {
                  case Success(resp) => { tests -= 1; println(resp + "\n^productTrades\n") }
                  case Failure(e) => e.printStackTrace
              }
              pubclient.productStats(id).onComplete {
                  case Success(resp) => { tests -= 1; println(resp + "\n^productStats\n") }
                  case Failure(e) => e.printStackTrace
              }
            }
            case _ => ;
          }
        }
      }
    })
  }

  pubclient.products().onComplete {
    case Success(resp) => productsHandler(resp)
    case Failure(e) => e.printStackTrace
  }

  pubclient.currencies().onComplete {
    case Success(resp) => { tests -= 1; println(resp + "\n^currencies\n") }
    case Failure(e) => e.printStackTrace
  }

  pubclient.time().onComplete {
    case Success(resp) => { tests -= 1; println(resp + "\n^time\n") }
    case Failure(e) => e.printStackTrace
  }

  // Authenticated (private) client methods. Requires API credentials
  val authclient = api.Client.authenticated(
    sys.env("APIKEY"),
    sys.env("APISECRET"),
    sys.env("APIPASSPHRASE")
  )

  authclient.accounts().onComplete {
    case Success(resp) => {
      tests -= 1
      resp.foreach( (el: Any ) => {
        el.asInstanceOf[List[Map[Any,Any]]].foreach {
          (m: Map[Any, Any]) => {
            m("currency") match {
              case "BTC" => {
                authclient.account(m("id").asInstanceOf[String]).onComplete {
                  case Success(resp) => { tests -= 1; println(resp + "\n^account\n") }
                  case Failure(e) => e.printStackTrace
                }
                authclient.accountHistory(m("id").asInstanceOf[String]).onComplete {
                  case Success(resp) => { tests -= 1; println(resp + "\n^accountHistory\n") }
                  case Failure(e) => e.printStackTrace
                }
                authclient.accountHolds(m("id").asInstanceOf[String]).onComplete {
                  case Success(resp) => { tests -= 1; println(resp + "\n^accountHolds\n") }
                  case Failure(e) => e.printStackTrace
                }

              }
              case _ => ;
            }
          }
        }
      })
    }
    case Failure(e) => e.printStackTrace
  }

  // Insufficient funds
  authclient.placeOrder("{\"size\":\"0.01\",\"price\":\"0.100\",\"side\":\"buy\",\"product_id\":\"BTC-USD\"}").onComplete {
    case Success(resp) => { tests -= 1; println(resp + "\n^order\n") }
    case Failure(e) => e.printStackTrace
  }

  authclient.getOrders().onComplete {
    case Success(resp) => { tests -= 1; println(resp + "\n^getOrders\n") }
    case Failure(e) => e.printStackTrace
  }

  authclient.getOrder("68e6a28f-ae28-4788-8d4f-5ab4e5e5ae08").onComplete {
    case Success(Some(resp)) => {
      tests -= 1
      println(resp + "\n^getOrder\n")
      assert(resp.asInstanceOf[Map[String,String]]("message") == "NotFound")
    }
    case Failure(e) => e.printStackTrace
    case _ => ;
  }

  authclient.cancelOrder("68e6a28f-ae28-4788-8d4f-5ab4e5e5ae08").onComplete {
    case Success(Some(resp)) => {
      tests -= 1
      println(resp + "\n^cancelOrder\n")
      assert(resp.asInstanceOf[Map[String,String]]("message") == "NotFound")
    }
    case Failure(e) => e.printStackTrace
    case _ => ;
  }





  // hrm, this is just a glorified main thread sleep.
  def patience = Future { while(tests > 0) { api.Client.sleep(1000) } }
  Await.ready(patience, Duration.Inf)

}
