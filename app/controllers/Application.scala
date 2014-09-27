package controllers

import java.nio.charset.CodingErrorAction
import java.util.UUID

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoCollection}
import play.api.libs.ws.WS
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("KatyaFormatter"))
  }


 /* def upload = Action.async(parse.multipartFormData) { request =>
    val csv =request.body.file("csv").get
    val output:Output = Resource.fromFile("/tmp/ready-"+csv.filename)
    val futu = scala.concurrent.future{
      val B = new StringBuilder
      val filename = csv.filename
      val contentType = csv.contentType
      val source = scala.io.Source.fromFile(csv.ref.file)(io.Codec.ISO8859)
      source.getLines.toList map(x => {
        if(!x.trim.isEmpty){ formatPretty(x) map {x =>  output.write( x +"\n" ) (scalax.io.Codec.ISO8859) }}
      })
      source.close()
      B.toString()
    }
    futu map {
      x => Ok.sendFile(
        content = new java.io.File("/tmp/ready-"+csv.filename),
        fileName = _ => "readys/ready-"+csv.filename
      )
    }

  }
*/
  def upload = Action.async(parse.multipartFormData) { request =>
    val csv =request.body.file("csv").get
   val uuii = "files"+UUID.randomUUID().toString.replace("-","").substring(0,4)
    //val output:Output = Resource.fromFile(File.separator+"tmp"+File.separator+csv.filename)
   val collec:MongoCollection = MongoClient("localhost")("dbfiles")(uuii)
      val futu = scala.concurrent.future{
        val filename = csv.filename
        val contentType = csv.contentType
        implicit val codec = scala.io.Codec("UTF-8")
        codec.onMalformedInput(CodingErrorAction.REPLACE)
        codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
        val source = scala.io.Source.fromFile(csv.ref.file)
        val g = request.body.asFormUrlEncoded.get("translate").getOrElse(List.empty)
        if(g.isEmpty){
          source.getLines.toList map(x => {
           if(!x.trim.isEmpty){ formatPrettyOffline(x,collec) }
          })
          source.close()
        }else{
          source.getLines.toList map(x => {
            if(!x.trim.isEmpty){ formatPrettyOnline(x,collec) onSuccess  {case x => x }}
          })
          source.close()
        }
      }
      futu map {
       // val output:Output = Resource.fromFile("/tmp/"+csv.filename)
       // for (inv <- collec) yield output.write(inv.get("line").toString+"\n")
        x => Ok(uuii)/*sendFile(
          content = new java.io.File("/tmp/"+csv.filename),
          fileName = _ => uuii
        )*/
    }

  }


/*  def formatPretty(biglineIn:String):Future[String] ={
    if (biglineIn.split(";").length > 1){
      val bigline =if(biglineIn.split(";").length==1) biglineIn+"FAULT" else biglineIn
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
  }*/




  def formatPrettyOnline(bigline:String,collec:MongoCollection):Future[String] ={
    val lsl = bigline.split("""\;""").toList
    if(lsl.isEmpty){Future("")}else{
      val tail = if(lsl.tail.isEmpty) "FAULT" else lsl.tail.head
      val id = lsl.head
      val desc = tail.replace(".","_").replace(" ","_")
      def conver2Letters(line:String):String = {
        val pattern = "^[ña-zÑA-Z]+$"
        val splitL = line.replaceAll("[^ña-zÑA-Z]"," ").replace(" ","_").split("_")
        def choice(l:List[String],total:List[String]):String = {
          if(l.isEmpty) total.mkString(" ") else if(l.head matches(pattern)) choice(l.tail,total:+l.head)
          else{ choice(l.tail,total)}}
        choice(splitL.toList,List.empty).toUpperCase()
      }

    val o = conver2Letters(desc)
    val out = if(o.trim.isEmpty) "FAULT" else o
    WS.url(s"http://translate.google.com/translate_a/t?client=t&text=$out&hl=pt&sl=pt&tl=en&multires=1&otf=2&pc=1&ssel=0&tsel=0&sc=1".replace(" ","%20").replace("|","%7C")).get().map{
      f => {
        val s = "X="+id+";#"+id+"_"+f.ahcResponse.getResponseBody.replace("[","").replace("]","").split(",")(0).replace("\"","").toUpperCase().replace("ROBO","ROBOT").replace("MESA","TABLE").replace("BOOK","RESERVE")
        collec.save(MongoDBObject("line"->s))
        s
      }
    }
   }
   }




  def formatPrettyOffline(bigline:String,collec:MongoCollection):String = {

    val lsl = bigline.split("""\;""").toList
    if(lsl.isEmpty){""}else{
      val tail = if(lsl.tail.isEmpty) "FAULT" else lsl.tail.head
      val id = lsl.head
      val desc = tail.replace(".","_").replace(" ","_")
      def conver2Letters(line:String):String = {
        val pattern = "^[ña-zÑA-Z]+$"
        val splitL = line.replaceAll("[^ña-zÑA-Z]"," ").replace(" ","_").split("_")
        def choice(l:List[String],total:List[String]):String = {
          if(l.isEmpty) total.mkString(" ") else if(l.head matches(pattern)) choice(l.tail,total:+l.head)
          else{ choice(l.tail,total)}}
        choice(splitL.toList,List.empty).toUpperCase()
      }

      val o = conver2Letters(desc)
      val out = if (o.trim.isEmpty) "FAULT" else o
      val s = "X=" + id + ";#" + id + "_" + out
      collec.save(MongoDBObject("line"->s))
      s
    }
  }


}