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

  println("public client")
  // Default
  var url = "https://api-public.sandbox.gdax.com"

  def products(callback: (Option[Any]) => Unit) = {

    val f = Future {
      val resp: HttpResponse[String] = Http(url + "/products").asString
      JSON.parseFull(resp.body)
    }

    f.onComplete {
      case Success(value) => { callback(value) }
      case Failure(e) => e.printStackTrace
    }
  }

}

class Auth {
  println("auth client")
}
