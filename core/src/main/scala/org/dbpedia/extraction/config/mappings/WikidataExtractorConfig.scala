package org.dbpedia.extraction.config.mappings


//Test class of client
object WikidataExtractorConfig{

  val test1= new WdtkMapping(propertyTransformations =  List(new ReplaceFunction("dbo:birthPlace")), objectTransformations = List.empty)
 // val test2 =

  def mapping(value:String="") = Map(
    "P19" -> test1.executeAll()
  //  "P434" -> new WdtkMapping(propertyTransformations = List(new ReplaceFunction(("owl:sameAs"))), objectTransformations = List(new AddPrefixFunction("http://musicbrainz.org/artist/", value)))
  )
}

//The Command Interface
trait TRansformationFunction {
  def execute()
}

//The Invoker Class
class WdtkMapping(val propertyTransformations:List[TRansformationFunction],val objectTransformations: List[TRansformationFunction]) {
  def executeAll(): Unit = {
    for (propertyTrans <- propertyTransformations) {
      propertyTrans.execute()
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
  def execute() = replace()
}

//Concrete command class
class AddPrefixFunction (prefix:String="", value:String="") extends TRansformationFunction {
  def addPrefix():String = prefix + value
  def execute() = addPrefix()
}


