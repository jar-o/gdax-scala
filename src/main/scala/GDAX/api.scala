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

}

class Auth {
  println("TODO auth'd client")
}
