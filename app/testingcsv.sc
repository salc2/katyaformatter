

val li = "613;  *  _1A230_KQ1FT               Dispositivo Programavel Fulga Terra Nok         /     1571,6"

def formatPretty(biglineIn:String):String  ={
  if (biglineIn.split(";").length > 1){
    val bigline =if(biglineIn.split(";").length==1) biglineIn+"FAULT" else biglineIn
    val id = bigline.split(";")(0)
    val desc = bigline.split(";")(1).replace(".","_").replace(" ","_")

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
    //s"X=$id;#$id"+"_"+out

      "X="+id+";#"+id+"_"+out
  }else{
    ""
  }
}

formatPretty(li)