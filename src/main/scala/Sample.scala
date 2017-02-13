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

  val pubclient = api.Client.public()

  val productsHandler = (v: Option[Any]) => {
    v.foreach( (el: Any ) => {
      el.asInstanceOf[List[Map[Any,Any]]].foreach {
        (m: Map[Any, Any]) => {
          m("id") match {
            case "BTC-USD" => {
              val id = m("id").asInstanceOf[String]
              pubclient.productOrderBook(id,"2").onComplete {
                  case Success(value) => { done += 1; println(value + "\n^productOrderBook\n") }
                  case Failure(e) => e.printStackTrace
              }
              pubclient.productTicker(id).onComplete {
                  case Success(value) => { done += 1; println(value + "\n^productTicker\n") }
                  case Failure(e) => e.printStackTrace
              }
              pubclient.productTrades(id).onComplete {
                  case Success(value) => { done += 1; println(value + "\n^productTrades\n") }
                  case Failure(e) => e.printStackTrace
              }
              pubclient.productStats(id).onComplete {
                  case Success(value) => { done += 1; println(value + "\n^productStats\n") }
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
    case Success(value) => productsHandler(value)
    case Failure(e) => e.printStackTrace
  }

  pubclient.currencies().onComplete {
    case Success(value) => { done += 1; println(value + "\n^currencies\n") }
    case Failure(e) => e.printStackTrace
  }

  pubclient.time().onComplete {
    case Success(value) => { done += 1; println(value + "\n^time\n") }
    case Failure(e) => e.printStackTrace
  }

  val authclient = api.Client.authenticated(
    sys.env("APIKEY"),
    sys.env("APISECRET"),
    sys.env("APIPASSPHRASE")
  )

  var done = 0
  def patience = Future { while(done < 6) { api.Client.sleep(1000) } }
  Await.ready(patience, Duration.Inf)

  //TODO need a better way to "join" futures
  // api.Client.sleep(1000*2)
}
