import GDAX._

// We need these to handle the futures that the library returns
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Sample extends App {
  println("This is a sample app showing basic usage.")

  val pubclient = api.Client.public()

  val productsHandler = (v: Option[Any]) => {
    v.foreach {
      _.asInstanceOf[List[Map[Any,Any]]].foreach {
        for ((k,v) <- _) printf("\t%s : %s\n", k, v)
      }
    }
  }

  pubclient.products().onComplete {
    case Success(value) => productsHandler(value)
    case Failure(e) => e.printStackTrace
  }
  //TODO just using this for concurrency atm, delete when have other examples
  pubclient.products().onComplete {
    case Success(value) => productsHandler(value)
    case Failure(e) => e.printStackTrace
  }

  //TODO need a better way to "join" futures 
  api.Client.sleep(500*3)

  //TODO 
  val authclient = api.Client.authenticated()
}
