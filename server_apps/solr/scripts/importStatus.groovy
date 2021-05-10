import groovy.json.JsonSlurper

while (true) {

    String host = System.getenv('DOMAIN_NAME')
    String core = 'prototype'

    String linkText = new URL( "http://$host/solr/$core/dataimport?wt=json&indent=true" ).text
    def json = new JsonSlurper().parseText( linkText )

    //println json.status
    print "."

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

    sleep(5*1000)
}