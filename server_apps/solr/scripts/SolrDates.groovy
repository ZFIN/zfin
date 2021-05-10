#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrResponse
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.impl.HttpSolrServer

import java.text.SimpleDateFormat

def host = System.env.get("DOMAIN_NAME")

SolrServer server = new HttpSolrServer("http://$host/solr/prototype")
SolrQuery query = new SolrQuery()

Date today = new Date()
Date fiveYearsAgo = new Date(today.getTime() - (5 * 365 * 24 * 60 * 60 * 1000L))

println "today: $today - fiveYearsAgo: $fiveYearsAgo"

//query.addDateRangeFacet("date", fiveYearsAgo, today, "+1YEAR")

query.addFilterQuery("category:Publication")
query.addFilterQuery("publication_type:Journal")

query.set("facet.range", "date"); // this is the field name date, so should be an enum
query.set("facet.range.start","NOW/YEAR-5YEARS");
query.set("facet.range.end","NOW/YEAR+1YEARS");
query.set("facet.range.gap","+1YEAR");
query.set("facet.range.other","all");

String last30Days = "date:[NOW/DAY-30DAYS TO NOW/DAY]"
String last90Days = "date:[NOW/DAY-90DAYS TO NOW/DAY]"
//String moreThan5Years = "date:[NOW/DAY-1000YEARS TO NOW/DAY-6YEARS]"

String moreThan5Years = "date:[* TO NOW/DAY-5YEARS]"

//this year: date:[NOW/YEAR+0YEAR TO NOW/YEAR+1YEAR]
//this year minus 1: date:[NOW/YEAR-1YEAR TO NOW/YEAR+0YEAR]
//this year minus 2: date:[NOW/YEAR-2YEAR TO NOW/YEAR-1YEAR]
//this year minus 3: date:[NOW/YEAR-3YEAR TO NOW/YEAR-2YEAR]
//this year minus 4: date:[NOW/YEAR-4YEAR TO NOW/YEAR-3YEAR]
//this year minus 5: date:[NOW/YEAR-5YEAR TO NOW/YEAR-4YEAR]
//more than 5 : date:[* TO NOW/YEAR-5YEAR]



query.addFacetQuery(last30Days)
query.addFacetQuery(last90Days)
query.addFacetQuery(moreThan5Years)

SolrResponse response = server.query(query)

String baseUrl = "http://$host/search?q=&search=&fq=category%3A%22Publication%22&category=Publication&fq=publication_type%3A%22Journal%22"

println "<ul>"
println """<li><a href="${baseUrl}&fq=$last30Days"> Last 30 Days</a> (${response.getFacetQuery().get(last30Days).toString()}) """
println """<li><a href="${baseUrl}&fq=$last90Days">    Last 90 Days</a> (${response.getFacetQuery().get(last90Days).toString()}) """



response.getFacetRanges().each { range ->
    range.getCounts().reverse().each { count ->
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Date end = sdf.parse(count.value)
        Date start = new Date(end.getTime() + 365 * 24 * 60 * 60 * 1000L)
        //this is just a mess, year is depricated, needs to have 1900 added to it, but the date returned
        //is dec 31st of the year before, so I'm adding 1901...  sorry
        String year = (sdf.parse(count.value).year + 1901L ).toString()
        String fq = "&fq=date:[" + sdf.format(end) + "Z TO " + sdf.format(start) + "Z]"

        println """<li><a href="${baseUrl}$fq">$year</a> (${count.count}) """
    }
    println "range.before: " + range.before
    println "range.after: " + range.after
}

println """<li><a href="${baseUrl}&fq=$moreThan5Years">Older than 5 years</a> (${response.getFacetQuery().get(moreThan5Years).toString()}) """
println "</ul>"
