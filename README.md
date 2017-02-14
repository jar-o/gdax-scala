# gdax-scala

An unofficial Scala client for the Coinbase GDAX API.

(This is a work-in-progress, and I will improve docs soon. In the meantime, I
suggest reading through `Sample.scala` for a demo on how to use it.)

## Getting started

You will need **Scala** (naturally) and [sbt](http://www.scala-sbt.org/)
installed. This has a single dependency,
[scalaj-http](https://github.com/scalaj/scalaj-http), which should be fetched
by **sbt**. To run do

```
sbt run
```

GDAX consists of public and private (or authenticated) APIs.

To obtain the public endpoint methods do

```
  val pubclient = api.Client.public()
```

To obtain the private endpoint methods you need to use your API credentials. E.g. to get them from the environment you could do

```
  val authclient = api.Client.authenticated(
    sys.env("APIKEY"),
    sys.env("APISECRET"),
    sys.env("APIPASSPHRASE")
  )
```

All the API methods are `Future` based and  asynchronous. They are generally called in the following manner:

```
pubclient.productOrderBook(
	id = "id,
	level = "2"
).onComplete {
	case Success(resp) => { println(resp) }
	case Failure(e) => e.printStackTrace
}
```

### Public API Methods

`pubclient.products().onComplete { ... }`


`pubclient.productTicker(id: String).onComplete { ... }`

`pubclient.productTrades(id : String).onComplete { ... }`

`pubclient.productStats(id : String).onComplete { ... }`

`pubclient.currencies().onComplete { ... }`

`pubclient.time().onComplete { ... }`

### Private API Methods

`authclient.accounts().onComplete { ... }`

`authclient.account(id: String).onComplete { ... }`

`authclient.accountHistory(id: String, params: Map[String, String] = null).onComplete { ... }`

`authclient.accountHolds(id: String).onComplete { ... }`

`authclient.getOrders(params: Map[String, String] = null).onComplete { ... }`

`authclient.getOrder(id: String).onComplete { ... }`

`authclient.placeOrder(json: Map[String, Any]).onComplete { ... }`

`authclient.cancelOrder(id: String).onComplete { ... }`

`authclient.deposit(amount: String, coinbaseAccountId: String).onComplete { ... }`

`authclient.withdraw(amount: String, coinbaseAccountId: String).onComplete { ... }`