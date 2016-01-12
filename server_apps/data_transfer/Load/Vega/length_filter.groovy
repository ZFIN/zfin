#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import org.zfin.framework.HibernateSessionCreator
import org.zfin.properties.ZfinProperties
import org.zfin.repository.*

//ZfinProperties.init("${System.getenv()['SOURCEROOT']}/home/WEB-INF/zfin.properties")
//new HibernateSessionCreator()
//println RepositoryFactory.markerRepository.getGeneByAbbreviation("fgf8a").abbreviation

def vegaDir = "/research/zprod/data/VEGA"
def thisLoad = "2015-12"
def prevLoad = "2014-11"

Map lengthMap = [:]
Map ottdartToOttdargMap = [:]
buildMaps(vegaDir, thisLoad, lengthMap, ottdartToOttdargMap)

Map previousLengthMap = [:]
buildMaps(vegaDir, prevLoad, previousLengthMap, null)

Set ottdartsToKeep = [] as Set
Set ottdargsToKeep = [] as Set


lengthMap.each() { String ottdart, Integer length ->
    Integer previousLength = previousLengthMap.get(ottdart) 
    Integer difference = null

    if (length != null && previousLength != null) {
         difference = (length - previousLength).abs()
    }

    if (difference && difference > 50) {
        ottdartsToKeep.add(ottdart)

    } else if (difference == null && previousLength == null) {
         //this means it's new
         ottdartsToKeep.add(ottdart)
    }

}

ottdartsToKeep.each {
    ottdargsToKeep.add(ottdartToOttdargMap[it])
}

ottdargsToKeep.each { println it }


def buildMaps(String vegaDir, String dataDir, Map lengthMap, Map ottdartToOttdargMap) {

    def lengthFile = new File(vegaDir + "/" + dataDir + "/defline_length.txt")
    lengthFile.eachLine { line ->
        def tokens = line.split("\\|")
        Integer length = null
        String ottdart = null
        String ottdarg = null

        if (tokens && tokens.length == 2) {
            length = new Integer(tokens[1])

            tokens[0].split().each {
                if (it.startsWith("OTTDART")) {
                    ottdart = it
                }
                if (it.startsWith(("OTTDARG"))) {
                    ottdarg = it
                }
            }
        }

        if (ottdartToOttdargMap != null) {
            ottdartToOttdargMap[ottdart] = ottdarg
        }

        lengthMap.put(ottdart, length)

    }

}



