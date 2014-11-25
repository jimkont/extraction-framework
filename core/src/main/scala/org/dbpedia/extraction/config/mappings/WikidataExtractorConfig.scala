package org.dbpedia.extraction.config.mappings


/**
 * Created by ali on 11/22/14.
 */


//Test class of client
object WikidataExtractorConfig{

  def conf(data:String):String= {
    val result:WikidataMapping=new WikidataMapping(data)

    val configFileExample = List("replace P19 dbo:birthPlace")

    //Create all avialable commands
    val repCommand:ReplaceFunction = new ReplaceFunction(result)

    val myMapping:WdtkMapping = new WdtkMapping()

    for (eachLine <- configFileExample ) {
      val eachLineSplit=eachLine.split("\\s+")
      val eachCmd = eachLineSplit(0)
      eachCmd match {
        case "replace" => {
          if (data == eachLineSplit(1).trim()) {
            val oldStr=eachLineSplit(1).trim
            val newStr=eachLineSplit(2)
            result.setReplaceParameters(oldStr,newStr)
          }

          myMapping.executeCommand(repCommand)
        }
        case _=> // do nothing

      }
    }

    result.testMap
  }

}

//The Command Interface
trait TRansformationFunction {
  def execute()
}

//The Invoker Class
class WdtkMapping() {

  def executeCommand(cmd:TRansformationFunction): Unit = {
    cmd.execute()
  }
}


//The Receiver class
class WikidataMapping(testData:String) {

  var testMap:String=""
  var OldString:String=""
  var NewString:String=""
  var prefix =""

  val checkPoint = OldString

  def setPrefix(prefix:String): Unit ={
    this.prefix=prefix
  }

  def setReplaceParameters(oldStr:String,newString:String){
    OldString=oldStr
    NewString=newString
  }

  def replace (){
    testData match {
      case checkPoint => testMap=NewString
      case _=>
    }

  }

  def addPrefix() {
    testMap=prefix + testData
  }

}

//Concrete command class
class ReplaceFunction(mapping:WikidataMapping) extends TRansformationFunction {
  //  def replace():String = newValue
  def execute(): Unit ={
    mapping.replace()
  }
}

//Concrete command class
class AddPrefixFunction (mapping:WikidataMapping) extends TRansformationFunction {
  def execute(): Unit = {
    mapping.replace()
  }
}

