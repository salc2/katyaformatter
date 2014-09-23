import play.api.libs.ws.WS

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
val li = "33,  *  zzENetPlc[6].TimeoutFault  Falha de TimeOut Plc 06 Remoto                  /"


def formatPretty(bigline:String):Future[String] ={
  val id = bigline.split(",")(0)
  val desc = bigline.split(",")(1).replace(".","_").replace(" ","_")

  def conver2Letters(line:String):String = {
    val pattern = "^[ña-zÑA-Z]+$"
    val splitL = line.split("_")
    def choice(l:List[String],total:List[String]):String = {
      if(l.isEmpty) total.mkString(" ") else if(l.head matches(pattern)) choice(l.tail,total:+l.head)
      else{ choice(l.tail,total)}}
    choice(splitL.toList,List.empty).toUpperCase()
  }

  val out = conver2Letters(desc)

  WS.url(s"http://translate.google.com/translate_a/t?client=t&text=$out&hl=pt&sl=pt&tl=en&multires=1&otf=2&pc=1&ssel=0&tsel=0&sc=1".replace(" ","%20").replace("|","%7C")).get().map{
    //f => s"X=$id;#$id"+"_"+(f.json \ "responseData" \ "translatedText").as[String]
    f => {
      s"X=$id;#$id"+"_"+f.ahcResponse.getResponseBody.replace("[","").replace("]","").split(",")(0).replace("\"","").toUpperCase()
    }
  }

}

formatPretty(li)


