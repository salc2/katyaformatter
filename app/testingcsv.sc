
val g = "231;  .  _1A514S.Channel            Portao Acesso 14 Fechado                        /     367.4"

def formatPrettyOffline(bigline:String):String = {
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
    s
  }
}

formatPrettyOffline(g)