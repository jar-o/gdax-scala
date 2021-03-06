package GDAX.api

import GDAX.util.JsonConverter
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scalaj.http._
import scala.util.parsing.json._
import scalaj.http.Base64
import scalaj.http.HttpConstants._
import java.net._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Client {
  def public() : Public = {
    return new Public()
  }

  def authenticated(
      apiKey: String,
      secretKey: String,
      passPhrase: String) : Private = {
    return new Private(apiKey, secretKey, passPhrase)
  }

  def sleep(duration: Long) { Thread.sleep(duration) }
}

// Object to handle basic HTTP requests by wrapping them in a Future,
// constructing and calling, and finally parsing the body to JSON.
object HttpJsonFutures {
  def get(
    url: String,
    params: Map[String, String] = null,
    headers: Map[String, String] = null
  ) : Future[Option[Any]] = {
    return Future {
      var httpreq = Http(url)
      if (headers != null) { httpreq = httpreq.headers(headers) }
      val resp: HttpResponse[String] = if (params == null)
        httpreq.asString else httpreq.params(params).asString
      JSON.parseFull(resp.body)
    }
  }

  def post(
    url: String,
    headers: Map[String, String] = null,
    json: String
  ) : Future[Option[Any]] = {
    return Future {
      var httpreq = Http(url)
      if (headers != null) { httpreq = httpreq.headers(headers) }
      httpreq = httpreq.postData(json).
        header("Content-Type", "application/json; charset=utf-8")
      val resp: HttpResponse[String] = httpreq.asString
      JSON.parseFull(resp.body)
    }
  }

  def delete(
    url: String,
    headers: Map[String, String] = null,
    json: String = null
  ) : Future[Option[Any]] = {
    return Future {
      var httpreq = Http(url)
      if (headers != null) { httpreq = httpreq.headers(headers) }
      if (json != null) { httpreq = httpreq.postData(json) }
      httpreq = httpreq.method("DELETE")
        .header("Content-Type", "application/json; charset=utf-8")
      val resp: HttpResponse[String] = httpreq.asString
      JSON.parseFull(resp.body)
    }
  }

}

// GDAX endpoints that require no authorization
class Public {
  // Default to sandbox environment
  var url = "https://api-public.sandbox.gdax.com"

  def products() : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + "/products")
  }

  def productOrderBook(id : String, level : String = "1") : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + s"/products/$id/book", Map("level" -> level))
  }

  def productTicker(id: String) : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + s"/products/$id/ticker")
  }

  def productTrades(id : String) : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + s"/products/$id/trades")
  }

  // 24hr stats
  def productStats(id : String) : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + s"/products/$id/stats")
  }

  //TODO
  //def productHistoricRates(id : String) : Future[Option[Any]] = {}

  def currencies() : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + "/currencies")
  }

  def time() : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + "/time")
  }

}

// GDAX endpoints requiring API authorization
class Private(apiKey: String, secretKey: String, passPhrase: String) {
  // Default to sandbox environment
  var url = "https://api-public.sandbox.gdax.com"

  var httpreq : HttpRequest = Http(url + "/accounts")//.postData("{\"helo\":1}")

  def accounts() : Future[Option[Any]] = {
    val auth = CoinbaseAuth(apiKey,
      secretKey, passPhrase, url + "/accounts", "GET")
    return HttpJsonFutures.get(url = url + "/accounts", headers = auth)
  }

  def account(id: String) : Future[Option[Any]] = {
    val auth = CoinbaseAuth(apiKey,
      secretKey, passPhrase, url + s"/accounts/$id", "GET")
    return HttpJsonFutures.get(url = url + s"/accounts/$id", headers = auth)
  }

  // NOTE use params for pagination. See https://docs.gdax.com/#pagination
  def accountHistory(id: String, params: Map[String, String] = null) : Future[Option[Any]] = {
    val auth = CoinbaseAuth(apiKey,
      secretKey, passPhrase, url + s"/accounts/$id/ledger", "GET")
    return HttpJsonFutures.get(url + s"/accounts/$id/ledger", params, auth)
  }

  def accountHolds(id: String) : Future[Option[Any]] = {
    val auth = CoinbaseAuth(apiKey,
      secretKey, passPhrase, url + s"/accounts/$id/holds", "GET")
    return HttpJsonFutures.get(url = url + s"/accounts/$id/holds", headers = auth)
  }

  def getOrders(params: Map[String, String] = null) : Future[Option[Any]] = {
    val auth = CoinbaseAuth(apiKey,
      secretKey, passPhrase, url + "/orders", "GET")
    return HttpJsonFutures.get(
      url = url + "/orders", params = params, headers = auth)
  }

  def getOrder(id: String) : Future[Option[Any]] = {
    val auth = CoinbaseAuth(apiKey,
      secretKey, passPhrase, url + s"/orders/$id", "GET")
    return HttpJsonFutures.get(url = url + s"/orders/$id", headers = auth)
  }

  def placeOrder(json: Map[String, Any]) : Future[Option[Any]] = {
    val jsonstr = JsonConverter.toJson(json)
    val auth = CoinbaseAuth(apiKey,
      secretKey, passPhrase, url + "/orders", "POST", jsonstr)
    return HttpJsonFutures.post(
      url = url + "/orders",
      headers = auth,
      json = jsonstr
    )
  }

  def cancelOrder(id: String) : Future[Option[Any]] = {
    val auth = CoinbaseAuth(apiKey,
      secretKey, passPhrase, url + s"/orders/$id", "DELETE")
    return HttpJsonFutures.delete(url = url + s"/orders/$id", headers = auth)
  }

  def deposit(amount: String, coinbaseAccountId: String) : Future[Option[Any]] = {
    return transfers(amount, coinbaseAccountId, "deposit")
  }

  def withdraw(amount: String, coinbaseAccountId: String) : Future[Option[Any]] = {
    return transfers(amount, coinbaseAccountId, "withdraw")
  }

  private def transfers(
    amount: String,
    coinbaseAccountId: String,
    transferType: String) : Future[Option[Any]] = {
    val json = new JSONObject(
      Map(
        "amount" -> amount.toString(),
        "coinbase_account_id" -> coinbaseAccountId,
        "type" -> transferType
      )).toString()
    val auth = CoinbaseAuth(apiKey,
      secretKey, passPhrase, url + "/transfers", "POST", json)
    return HttpJsonFutures.post(
      url = url + "/transfers",
      headers = auth,
      json = json
    )

  }

}

// Construct the Auth headers expected by GDAX
object CoinbaseAuth {

  def apply(
    apiKey: String,
    secretKey: String,
    passPhrase: String,
    url: String,
    verb: String,
    data: String = "") : Map[String, String] = {

    val timestamp: Long = System.currentTimeMillis / 1000
    val reqpath: String = new URL(url).getPath()
    val message = timestamp + verb.toUpperCase + reqpath + data
    val hmacKey: Array[Byte] = Base64.decode(secretKey)
    val secret = new SecretKeySpec(hmacKey, "HmacSHA256")
    val hmac = Mac.getInstance("HmacSHA256")
    hmac.init(secret)

    Map(
      "CB-ACCESS-SIGN" -> base64(hmac.doFinal(message.getBytes)),
      "CB-ACCESS-TIMESTAMP" -> timestamp.toString,
      "CB-ACCESS-KEY" -> apiKey,
      "CB-ACCESS-PASSPHRASE" -> passPhrase
      )
  }

}
