import play.api.libs.ws.WS

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
val li = "33,  *  zzENetPlc[6].TimeoutFault  Falha de TimeOut Plc 06 Remoto                  /"


def formatPretty(bigline:String):Future[String] ={
  val id = bigline.split(",")(0)
  val desc = bigline.split(",")(1)

  def conver2Letters(line:String):String = {
    val pattern = "^[ña-zÑA-Z]+$"
    val splitL = line.split(" ")
    def choice(l:List[String],total:String):String = {
      if(l.tail.isEmpty) total else if(l.head matches(pattern)) choice(l.tail,total.trim+" "+l.head)
      else{ choice(l.tail,total)}}
    choice(splitL.toList,"").toUpperCase()
  }

  val out = conver2Letters(desc)

  WS.url(s"http://translate.google.com/translate_a/t?client=t&text=$out&hl=pt&sl=pt&tl=en&multires=1&otf=2&pc=1&ssel=0&tsel=0&sc=1".replace(" ","%20").replace("|","%7C")).get().map{
    //f => s"X=$id;#$id"+"_"+(f.json \ "responseData" \ "translatedText").as[String]
    f => f.ahcResponse.getResponseBody
      ""
  }

}

formatPretty(li)


