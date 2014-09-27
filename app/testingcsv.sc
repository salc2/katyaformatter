import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject

val conn = MongoClient("localhost")("test")

conn("somes").save(MongoDBObject("name" -> "whatever"))

