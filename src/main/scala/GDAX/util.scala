package GDAX.util
import scala.collection.mutable.ListBuffer

object JsonConverter {
  def toJson(o: Any) : String = {
    var json = new ListBuffer[String]()
    o match {
      case m: Map[_,_] => {
        for ( (k,v) <- m ) {
          var key = escape(k.asInstanceOf[String])
          v match {
            case a: Map[_,_] => json += "\"" + key + "\":" + toJson(a)
            case a: List[_] => json += "\"" + key + "\":" + toJson(a)
            case a: Int => json += "\"" + key + "\":" + a
            case a: Boolean => json += "\"" + key + "\":" + a
            case a: String => json += "\"" + key + "\":\"" + escape(a) + "\""
            case _ => ;
          }
        }
      }
      case m: List[_] => {
        var list = new ListBuffer[String]()
        for ( el <- m ) {
          el match {
            case a: Map[_,_] => list += toJson(a)
            case a: List[_] => list += toJson(a)
            case a: Int => list += a.toString()
            case a: Boolean => list += a.toString()
            case a: String => list += "\"" + escape(a) + "\""
            case _ => ;
          }
        }
        return "[" + list.mkString(",") + "]"
      }
      case _ => ;
    }
    return "{" + json.mkString(",") + "}"
  }

  private def escape(s: String) : String = {
    return s.replaceAll("\"" , "\\\\\"");
  }
}

// TODO should test this
//println(JsonConverter.toJson(Map("a"-> 1, "b" -> Map("nes\"ted"->"yeah{\"some\":true"), "c" -> List(1,2,"3",List(true,false,true,Map("1"->"two","3"->"four"))))))
