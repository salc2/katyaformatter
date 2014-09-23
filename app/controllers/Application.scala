package controllers

import play.api.libs.ws.WS
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Application extends Controller {

  def index = Action {
    Ok("Hola")
  }

  def upload2 = Action.async(parse.multipartFormData)  { request =>
    request.body.file("csv").map { csv =>
      WS.url("").get().map { response =>
        Ok("Feed title: " + (response.json \ "title").as[String])
      }
    }.getOrElse(Future(Ok("Error")))


  }



  def upload = Action(parse.multipartFormData) { request =>


    request.body.file("csv").map { csv =>
      Ok({
      val B = new StringBuilder
      val filename = csv.filename
      val contentType = csv.contentType
      val source = scala.io.Source.fromFile(csv.ref.file)

     source.getLines.toList map(x => {
           formatPretty(x) onComplete {
             case Success(l) => if(!l.trim.isEmpty){ B ++= l+"\n"; println(l+"\n")}
             case Failure(l) => println("Error")
           }
     })
      source.close()
      B.toString().getBytes
      }).as("text/csv")
    }.getOrElse {
      Results.BadRequest("Error!!!!")
    }
  }


  def formatPretty(bigline:String):Future[String] ={
    if (bigline.split(";").length > 0){
    val id = bigline.split(";")(0)
    val desc = bigline.split(";")(1).replace(".","_").replace(" ","_")

    def conver2Letters(line:String):String = {
      val pattern = "^[ña-zÑA-Z]+$"
      val splitL = line.split("_")
      def choice(l:List[String],total:List[String]):String = {
        if(l.isEmpty) total.mkString(" ") else if(l.head matches(pattern)) choice(l.tail,total:+l.head)
        else{ choice(l.tail,total)}}
      choice(splitL.toList,List.empty).toUpperCase()
    }

    val o = conver2Letters(desc)
    val out = if(o.trim.isEmpty) "FAULT" else o
    s"X=$id;#$id"+"_"+out
    WS.url(s"http://translate.google.com/translate_a/t?client=t&text=$out&hl=pt&sl=pt&tl=en&multires=1&otf=2&pc=1&ssel=0&tsel=0&sc=1".replace(" ","%20").replace("|","%7C")).get().map{
      //f => s"X=$id;#$id"+"_"+(f.json \ "responseData" \ "translatedText").as[String]
      f => {
        s"X=$id;#$id"+"_"+f.ahcResponse.getResponseBody.replace("[","").replace("]","").split(",")(0).replace("\"","").toUpperCase()
      }
    }
    }else{
      Future("")
    }
  }
  /*def formatPretty(bigline:String):Future[String] ={
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
    WS.url(s"http://mymemory.translated.net/api/get?q=$out&langpair=pt|en".replace(" ","%20").replace("|","%7C")).get().map{
      f => {val p = s"X=$id;#$id"+"_"+(f.json \ "responseData" \ "translatedText").as[String]
            println(p)
        p
      }
    }
  }*/

}