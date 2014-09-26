

val li = "546;  *  _1A2_1M3.ModuleStatus      Falha EtherNet Modulo Safety                    /"



def formatPrettyOffline(bigline:String):String = {

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
    "X=" + id + ";#" + id + "_" + out
  }
}



formatPrettyOffline(li)