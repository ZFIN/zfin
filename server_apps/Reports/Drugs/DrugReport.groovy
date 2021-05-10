#!/bin/bash

//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.search.service.SolrService
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.impl.HttpSolrServer
import org.apache.solr.common.SolrDocument
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

ZfinProperties.init("${System.getenv()['SOURCEROOT']}/home/WEB-INF/zfin.properties")


SolrServer server = null
try {
    server = new HttpSolrServer("http://${ZfinPropertiesEnum.SOLR_HOST.value}:${ZfinPropertiesEnum.SOLR_PORT.value}/solr/prototype/");
} catch (Exception e) {
    println "couldn't get SolrServer"
    println e
}
SolrQuery query = new SolrQuery()
QueryResponse response;
query.setRequestHandler("/pub-drug")

def fos = new FileOutputStream("zfin-chemical-report.csv")
OutputStreamWriter outputwriter = new OutputStreamWriter(new BufferedOutputStream(fos))
CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
CSVPrinter csvPrinter = new CSVPrinter(outputwriter, csvFormat);


csvPrinter.printRecord("Chemical","PMID", "Title", "Abstract", "Keywords")

new File("/research/zprod/data/druglist/finaldruglist5.txt").eachLine { line ->

    String q = line.trim()
  
    //ignore terms with less than 4 chars
    if (q.size() >= 4) {

        query.setQuery("\"" + SolrService.luceneEscape(q) + "\"")

        try {
         response = server.query(query)
        } catch (Exception e) {
         println e;
        }

        response?.getResults().each { SolrDocument doc ->
            def accession = doc?.getFieldValue("related_accession")?.first()?:""
            def title = doc?.getFieldValue("full_name")?:""
            def abstractText = doc?.getFieldValue("abstract")?:""
            def keywords = doc?.getFieldValue("keyword")?:""
            //println("\"" + [q, accession, title,abstractText, keywords].join("\",\"") + "\"")
            csvPrinter.printRecord(q, accession, title, abstractText, keywords)

        }
    }

}

outputwriter.flush();
outputwriter.close();