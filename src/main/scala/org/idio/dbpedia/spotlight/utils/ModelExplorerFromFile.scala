package org.idio.dbpedia.spotlight.utils

import org.idio.dbpedia.spotlight.CustomSpotlightModel

/**
 * Given a file, it gives an insight of what SF,DbpediaResources exists in a model
 * The format of each life of the file is:
 * dbpediaURI tab surfaceForm1|surfaceForm2
 * Created by dav009 on 03/01/2014.
 */
class ModelExplorerFromFile(pathToModelFolder: String, pathToFile: String) {

  /*
  * Parses an input line.
  * Returns the SurfaceForm, DbpediaID, Types, ContextWords, ContextCounts
  * */
  def parseLine(line: String): (Array[String], String) = {
    val splittedLine = line.trim.split("\t")
    var dbpediaUri = splittedLine(0)
    var surfaceForms = splittedLine(1).split('|')

    (surfaceForms, dbpediaUri)
  }

  /*
  * Checks if the SF, dbpediaUris specified in the input file exists
  * and checks whetheter the SF are linked to the DbpediaURIs
  * */
  def checkEntitiesInFile() {
    var customSpotlightModel: CustomSpotlightModel = new CustomSpotlightModel(this.pathToModelFolder)
    val source = scala.io.Source.fromFile(this.pathToFile)
    val lines = source.bufferedReader()
    var line = lines.readLine()

    var countsOfFoundTopics = 0
    var countsOfFoundSF = 0
    var countsOfLinkedSFTopic = 0
    var totalSF = 0
    var totalTopics = 0
    var totalSFAndTopics = 0

    while (line != null) {

      val (surfaceForms, dbpediaUri) = parseLine(line)

      var dbpediaId = -1
      var isDbpediaResourceinModel = customSpotlightModel.searchForDBpediaResource(dbpediaUri)

      totalTopics = totalTopics + 1

      if (isDbpediaResourceinModel) {
        dbpediaId = customSpotlightModel.customDbpediaResourceStore.resStore.idFromURI.get(dbpediaUri)
        countsOfFoundTopics = countsOfFoundTopics + 1
      }

      for (surfaceForm <- surfaceForms) {
        totalSF = totalSF + 1
        var surfaceId = -1
        val normalizedSF = customSpotlightModel.customSurfaceFormStore.sfStore.normalize(surfaceForm)
        var isSFinModel = customSpotlightModel.customSurfaceFormStore.sfStore.idForString.containsKey(surfaceForm) | customSpotlightModel.customSurfaceFormStore.sfStore.idForString.containsKey(normalizedSF)

        var areSFandResourceLinked = false

        if (isSFinModel) {
          countsOfFoundSF = countsOfFoundSF + 1
          try {
            surfaceId = customSpotlightModel.customSurfaceFormStore.sfStore.idForString.get(surfaceForm)
          } catch {

            case ex: Exception => {
              println("")
              println("used normalized SF")
              println("normalized:" + normalizedSF)
              println("")
              surfaceId = customSpotlightModel.customSurfaceFormStore.sfStore.idForString.get(normalizedSF)
            }
          }

        }

        try {
          areSFandResourceLinked = customSpotlightModel.customCandidateMapStore.checkCandidateInSFCandidates(surfaceId, dbpediaId)
        } catch {
          case ex: Exception => {}
        }

        if (areSFandResourceLinked) {
          countsOfLinkedSFTopic = countsOfLinkedSFTopic + 1
        }

        println("----------------------------")
        println("SF: " + surfaceForm)
        println("\t in model?\t\t" + isSFinModel)
        println("Topic: " + dbpediaUri)
        println("\t in model?\t\t" + isDbpediaResourceinModel)
        println("is SF connected to the Topic?")
        println("\t" + areSFandResourceLinked)
        println("----------------------------")
      }
      line = lines.readLine()
    }
    println("totals")
    println("TOPICS")
    println("\t# of topics: " + totalTopics)
    println("\t# of found topics: " + countsOfFoundTopics)
    println("SF")
    println("\t# of SF: " + totalSF)
    println("\t# of found SF: " + countsOfFoundSF)
    println("topics linked to SF: " + countsOfLinkedSFTopic)
    println("expected number of topics and SF links: " + totalSF)
    source.close()
  }

}
