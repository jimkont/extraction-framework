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
        if (keyVal._2 != null) {
          if (keyVal._2.contains("$1")) {
            val v= substitute(keyVal._2, value.toString.replace("\"", "").replace(" ", "_").trim)
            MapResult += (keyVal._1 -> v)
          }
          else {
            keyVal._2 match {
              case "$getLatitude" => MapResult += (keyVal._1 -> getLatitude(value).toString)
              case "$getLongitude" => MapResult += (keyVal._1 -> getLongitude(value).toString)
              case "$getGeoRss" => MapResult += (keyVal._1 -> getGeoRss(value))
              case "$getSpatialThing"  =>  MapResult += (keyVal._1 -> "$getSpatialThing")
              case _=>
            }


          }

        }
        else {
          MapResult += (keyVal._1 -> value.toString.replace("(item)", "").replace("\"", "").trim)
        }
      }
    }

  }

 def getLatitude(value:Value) = value match {
    case v:GlobeCoordinatesValue => {
      v.getLatitude
    }
 }

  def getLongitude(value:Value) = value match {
    case v:GlobeCoordinatesValue => {
      v.getLongitude
    }
  }

  def getGeoRss(value:Value) = value match {
    case v:GlobeCoordinatesValue => {
      v.getLatitude + " " + v.getLongitude
    }
  }

  def substitute(newValue: String, value: String): String = {
      newValue.replace("$1", value)

  }

}
