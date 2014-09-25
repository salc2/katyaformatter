package controllers

import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scalax.io.{Output, Resource}

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
    val output:Output = Resource.fromFile("/tmp/ready-"+csv.filename)
      val futu = scala.concurrent.future{
        val B = new StringBuilder
        val filename = csv.filename
        val contentType = csv.contentType
        val source = scala.io.Source.fromFile(csv.ref.file)(io.Codec.ISO8859)
        source.getLines.toList map(x => {
          if(!x.trim.isEmpty){ output.write( formatPretty(x) +"\n" ) (scalax.io.Codec.ISO8859)}
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



/*
  def formatPretty(bigline:String):Future[String] ={

  val lsl = bigline.split(";").toList
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
        "X="+id+";#"+id+"_"+f.ahcResponse.getResponseBody.replace("[","").replace("]","").split(",")(0).replace("\"","").toUpperCase().replace("ROBO","ROBOT").replace("MESA","TABLE").replace("BOOK","RESERVE")
      }
    }
   }*/




  def formatPretty(bigline:String):String ={

    val lsl = bigline.split(";").toList
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
      val o2 = if(o.trim.isEmpty) "FAULT" else o
      val out = o2.replace(" ","%20")
     "X="+id+";#"+id+"_"+out
     }



}