package org.dbpedia.extraction.config.mappings.wikidata

import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue
import org.wikidata.wdtk.datamodel.interfaces.Value
import scala.collection.mutable
/**
 * Created by ali on 12/20/14.
 */
trait WikidataTransformationCommands {
  def execute()
}

class WikidataOneToOneCommand(receiver: WikidataCommandReceiver) extends WikidataTransformationCommands {
  def execute(): Unit = {
    receiver.oneToOne()
  }
}

class WikidataOneToManyCommand(receiver: WikidataCommandReceiver) extends WikidataTransformationCommands {
  def execute(): Unit = {
    receiver.oneToMany()
  }
}

class WikidataCommandReceiver {

  var MapResult = mutable.Map.empty[String, String]
  private var property: String = ""
  private var value: Value = _
  private var map = mutable.Map.empty[String, String]

  def setParameters(property: String, value: Value, map: mutable.Map[String, String]): Unit = {
    this.property = property
    this.value = value
    this.map = map
  }

  def getMap(): mutable.Map[String, String] = {
    MapResult
  }

  def oneToOne(): Unit = {
    oldMapToNewMap()
  }

  def oneToMany(): Unit = {
    oldMapToNewMap()
  }

  def oldMapToNewMap(): Unit = {

    map.foreach {
      keyVal => {
        if (keyVal._2 != null && keyVal._2.contains("$")) {
          MapResult += (keyVal._1 -> substitute(keyVal._2, value.toString))
          /*

          * */
          if (value.isInstanceOf[GlobeCoordinatesValue]){
            val method = Class.forName(value.getClass.getName).getMethod("getLongitude")

          }


//           value.getClass().getMethods.foreach {
//            method => println(method.getName)
//          }
/*          value match {
            case v: GlobeCoordinatesValue => {
              if (keyVal._1.endsWith("type")) {
                MapResult += (keyVal._1 -> substitute(keyVal._2, "spatialThing"))
              } else if (keyVal._1.endsWith("lat")) {
                MapResult += (keyVal._1 -> substitute(keyVal._2, v.getLatitude.toString))
              } else if (keyVal._1.endsWith("long")) {
                MapResult += (keyVal._1 -> substitute(keyVal._2, v.getLongitude.toString))
              } else if (keyVal._1.endsWith("point")) {
                MapResult += (keyVal._1 -> substitute(keyVal._2, v.getLatitude.toString + " " + v.getLongitude.toString))
              }
            }
            case _ => {
              MapResult += (keyVal._1 -> substitute(keyVal._2, value.toString))
            }
          }*/

        }
        else {
          MapResult += (keyVal._1 -> value.toString)
        }
      }
    }

  }


  def substitute(newValue: String, value: String): String = {
    newValue.replace("$1", value)
  }

}
