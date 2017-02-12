package GDAX.api

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scalaj.http._
import scala.util.parsing.json._

object Client {
  def public() : Public = {
    return new Public()
  }
  def authenticated() : Auth = {
    return new Auth()
  }
  def sleep(duration: Long) { Thread.sleep(duration) }
}

class Public {

  // Default to sandbox environment
  var url = "https://api-public.sandbox.gdax.com"

  def products() : Future[Option[Any]] = {
    return Future {
      val resp: HttpResponse[String] = Http(url + "/products").asString
      JSON.parseFull(resp.body)
    }
  }

  def productOrderBook(id : String, level : String = "1") : Future[Option[Any]] = {
    return Future {
      val resp: HttpResponse[String] =
        Http(url + s"/products/$id/book").param("level", level).asString
      JSON.parseFull(resp.body)
    }

  }

  def productTicker(id : String) : Future[Option[Any]] = {
    return Future {
      val resp: HttpResponse[String] =
          Http(url + s"/products/$id/ticker").asString
      JSON.parseFull(resp.body)
    }
  }

  def productTrades(id : String) : Future[Option[Any]] = {
    return Future {
      val resp: HttpResponse[String] =
          Http(url + s"/products/$id/trades").asString
      JSON.parseFull(resp.body)
    }
  }

  // 24hr stats
  def productStats(id : String) : Future[Option[Any]] = {
    return Future {
      val resp: HttpResponse[String] =
          Http(url + s"/products/$id/stats").asString
      JSON.parseFull(resp.body)
    }
  }
  //TODO
  //def productHistoricRates(id : String) : Future[Option[Any]] = {}

  def currencies() : Future[Option[Any]] = {
    return Future {
      val resp: HttpResponse[String] = Http(url + "/currencies").asString
      JSON.parseFull(resp.body)
    }
  }

}

class Auth {
  println("TODO auth'd client")
}
