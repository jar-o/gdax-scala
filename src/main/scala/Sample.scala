import GDAX._

object Sample extends App {
  println("This is a sample app showing basic usage.")


  val pubclient = api.Client.public()
  val productsCb = (v: Option[Any]) => {
    v.foreach {
      _.asInstanceOf[List[Map[Any,Any]]].foreach {
        for ((k,v) <- _) printf("\t%s : %s\n", k, v)
      }
    }
  }

  pubclient.products(productsCb)
  pubclient.products(productsCb)
  //TODO need a better way to "join" futures 
  api.Client.sleep(500*3)

  //TODO 
  val authclient = api.Client.authenticated()
}
