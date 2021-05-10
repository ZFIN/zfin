#!/usr/bin/env groovy
            import groovy.json.JsonSlurper

            String port = System.getenv('SOLR_PORT')
            String core = System.getenv('SOLR_CORE')
            def url = "http://localhost:$port/solr/$core/dataimport?wt=json&amp;indent=true"
            println url
            println "Indexing ..."
            while (true) {

                String linkText = new URL(url).text;
                def json = (new JsonSlurper()).parseText(linkText)
                def elapsedTime = json.statusMessages."Time Elapsed"
                if (elapsedTime != null)
                    println "Elapsed time: " + elapsedTime

                if (json.status != "busy") {
                    println ""
                    if (json.statusMessages.toString().contains("Indexing completed")) {
                        println linkText
                        println "SUCCESS!"
                        System.exit(0)
                    } else {
                        println linkText
                        println "FAIL!"
                        System.exit(-1)
                    }
                }
                sleep(60 * 1000)
            }