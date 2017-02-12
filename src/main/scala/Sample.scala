import GDAX._

// We need these to handle the futures that the library returns
import scala.concurrent.Future
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
                  case Success(value) => println(value + "\n^productOrderBook\n")
                  case Failure(e) => e.printStackTrace
              }
              pubclient.productTicker(id).onComplete {
                  case Success(value) => println(value + "\n^productTicker\n")
                  case Failure(e) => e.printStackTrace
              }
              pubclient.productTrades(id).onComplete {
                  case Success(value) => println(value + "\n^productTrades\n")
                  case Failure(e) => e.printStackTrace
              }
              pubclient.productStats(id).onComplete {
                  case Success(value) => println(value + "\n^productStats\n")
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
    case Success(value) => println(value + "\n^currencies\n")
    case Failure(e) => e.printStackTrace
  }


  //TODO need a better way to "join" futures
  api.Client.sleep(1000*3)

  //TODO
  val authclient = api.Client.authenticated()
}
