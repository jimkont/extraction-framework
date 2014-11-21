package org.dbpedia.extraction.config.mappings


//Test class of client
object WikidataExtractorConfig{

  val replaceFunction =new ReplaceFunction("dbo:birthPlace")
  val test1= new WdtkMapping(propertyTransformations = List(replaceFunction), objectTransformations = List.empty)
  test1.executeAll()
 // val test2 =

  def mapping(value:String="") = Map(
    "P19" -> test1.propertyArray(0)
  //  "P434" -> new WdtkMapping(propertyTransformations = List(new ReplaceFunction(("owl:sameAs"))), objectTransformations = List(new AddPrefixFunction("http://musicbrainz.org/artist/", value)))
  )
}

//The Command Interface
trait TRansformationFunction {
  def execute():String
}

//The Invoker Class
class WdtkMapping(val propertyTransformations:List[TRansformationFunction],val objectTransformations: List[TRansformationFunction]) {
  val propertyArray=Array[String]()
  def executeAll(): Unit = {
    for (propertyTrans <- propertyTransformations) {
      propertyArray:+propertyTrans.execute()
    }

    for (objTrans <- objectTransformations) {
      objTrans.execute()
    }
  }
}


//The Receiver class
class Functions {

}

//Concrete command class
class ReplaceFunction(newValue:String="") extends TRansformationFunction {
  def replace():String = newValue
  def execute():String = replace()
}

//Concrete command class
class AddPrefixFunction (prefix:String="", value:String="") extends TRansformationFunction {
  def addPrefix():String = prefix + value
  def execute():String = addPrefix()
}


