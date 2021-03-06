package controllers

import java.nio.charset.CodingErrorAction
import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.ws.WS
import play.api.mvc._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationDouble
import scalax.io.Resource

object Application extends Controller {

  val myActor = Akka.system.actorOf(Props[QueueActor], name = "myactor")
  case class Putter(fileName:String,row:String)
  case class Getter(fileName:String)

  class QueueActor extends Actor with ActorLogging {
    val map:mutable.Map[String,String] = mutable.HashMap()
    def receive = {
      case Putter(fileName,row) =>{
        map.get(fileName) match {
          case Some(x) => Resource.fromFile(x.toString).write(row+"\n")
          case None => Resource.fromFile("/tmp/"+fileName).write(row+"\n");map.put(fileName , "/tmp/"+fileName)
        }
        log.info(fileName +" -> "+ row)
      }
      case Getter(fileName) => map.get(fileName) match {
        case Some(x) => sender ! x.toString
        case None => sender ! None
      }
     }
  }


  def index = Action {
    Ok(views.html.index("KatyaFormatter"))
  }


 def indexDownload(fileName:String,col:String) = Action {
   Ok(views.html.indexDownload(fileName,col))
 }


  def download = Action.async{ implicit request =>
    val uuii = request.body.asFormUrlEncoded.get("uuii").toList.head
    val fileName = request.body.asFormUrlEncoded.get("fileName").toList.head
    implicit  val timeout= Timeout(5.seconds)
    (myActor ? Getter(uuii)) map { response =>
    Ok.sendFile(
      content = new java.io.File(response.toString),
      fileName = _ => fileName.replace(".","-PRO.")
    )
    }
  }


  def upload = Action.async(parse.multipartFormData) { request =>
    val csv =request.body.file("csv").get
   val uuii = "files"+UUID.randomUUID().toString.replace("-","").substring(0,4)
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
           if(!x.trim.isEmpty){ formatPrettyOffline(x,uuii) }
          })
          source.close()
        }else{
          source.getLines.toList map(x => {
            if(!x.trim.isEmpty){ formatPrettyOnline(x,uuii) onSuccess  {case x => x }}
          })
          source.close()
        }
      }
      futu map {
        x =>Redirect(routes.Application.indexDownload(csv.filename,uuii)) //Ok(uuii+", "+csv.filename)
    }

  }


  def formatPrettyOnline(bigline:String,fileN:String):Future[String] ={
    val lsl = bigline.split("""\;""").toList
    if(lsl.isEmpty){Future("")}else{
      val tail = if(lsl.tail.isEmpty) "FAULT" else lsl.tail.head
      val id = lsl.head
      val desc = tail.replace(".","_").replace(" ","_")
      val dessp = if(!desc.split("""\/""").toList.isEmpty){desc.split("""\/""").toList.head}else{desc}
      def conver2Letters(line:String):String = {
        val pattern = "^[ña-zÑA-Z0-9]+$"
        val splitL = line.replaceAll("[^ña-zÑA-Z0-9]"," ").replace(" ","_").split("_")
        def choice(l:List[String],total:List[String]):String = {
          if(l.isEmpty) total.mkString(" ") else if(l.head matches(pattern)) choice(l.tail,total:+l.head)
          else{ choice(l.tail,total)}}
        choice(splitL.toList,List.empty).toUpperCase()
      }

    val o = conver2Letters(dessp)
    val out = if(o.trim.isEmpty) "FAULT".toLowerCase else o.toLowerCase
    WS.url(s"http://translate.google.com/translate_a/t?client=t&text=$out&hl=pt&sl=pt&tl=en&multires=1&otf=2&pc=1&ssel=0&tsel=0&sc=1".replace(" ","%20").replace("|","%7C")).get().map{
      f => {
        val s = "X="+id+";#"+id+"_"+f.ahcResponse.getResponseBody.replace("[","").replace("]","").split(",")(0).replace("\"","").toUpperCase().replace("ROBO","ROBOT").replace("MESA","TABLE").replace("BOOK","RESERVE").replace("PECA","PART").toUpperCase
         myActor ! Putter(fileN,s)
        s
      }
    }
   }
   }




  def formatPrettyOffline(bigline:String,fileN:String):String = {
    val lsl = bigline.split("""\;""").toList
    if(lsl.isEmpty){""}else{
      val tail = if(lsl.tail.isEmpty) "FAULT" else lsl.tail.head
      val id = lsl.head
      val desc = tail.replace(".","_").replace(" ","_")
      val dessp = if(!desc.split("""\/""").toList.isEmpty){desc.split("""\/""").toList.head}else{desc}
      def conver2Letters(line:String):String = {
        val pattern = "^[ña-zÑA-Z0-9]+$"
        val splitL = line.replaceAll("[^ña-zÑA-Z0-9]"," ").replace(" ","_").split("_")
        def choice(l:List[String],total:List[String]):String = {
          if(l.isEmpty) total.mkString(" ") else if(l.head matches(pattern)) choice(l.tail,total:+l.head)
          else{ choice(l.tail,total)}}
        choice(splitL.toList,List.empty).toUpperCase()
      }

      val o = conver2Letters(dessp)
      val out = if (o.trim.isEmpty) "FAULT" else o
      val s = "X=" + id + ";#" + id + "_" + out
      myActor ! Putter(fileN,s)
      s
    }
  }


}