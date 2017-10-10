#!/bin/bash
//usr/bin/env groovy -cp "<!--|GROOVY_CLASSPATH|-->:." "$0" $@; exit $?

import groovy.sql.Sql
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.util.ReportGenerator

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

def months = ['Jan': '01', 'Feb': '02', 'Mar': '03', 'Apr': '04', 'May': '05', 'Jun': '06',
              'Jul': '07', 'Aug': '08', 'Sep': '09', 'Oct': '10', 'Nov': '11', 'Dec': '12']

def db = [
        url: "jdbc:informix-sqli://${ZfinPropertiesEnum.SQLHOSTS_HOST}:${ZfinPropertiesEnum.INFORMIX_PORT}/${ZfinPropertiesEnum.DBNAME}:INFORMIXSERVER=${ZfinPropertiesEnum.INFORMIXSERVER};DB_LOCALE=en_US.utf8",
        driver: 'com.informix.jdbc.IfxDriver'
]

def pubsNotUpdated = []
def pubsUpdated = []

Sql.withInstance(db) { Sql sql ->
    def pubsWithNoDate = sql.rows('select zdb_id, accession_no from publication where pub_date is null and accession_no is not null')

    println("Fetching ${pubsWithNoDate.size()} pubs from PubMed")
    def count = 0

    // break up the results into batches of up to 500 because that's a good number to fetch
    // from pubmed with one request
    pubsWithNoDate.collate(500).each { batch ->
        // make a mapping between ZDB and pubmed IDs for easy lookup later
        def idMap = batch.collectEntries { result -> [result.accession_no as String, result.zdb_id as String] }

        // do the pubmed request
        def articleSet = PubmedUtils.getFromPubmed(batch.collect { it.accession_no })
        count += articleSet.PubmedArticle.size()
        println("Fetched ${count}")
        articleSet.PubmedArticle.each { article ->
            def id = article.MedlineCitation.PMID as String
            def createDate = article.MedlineCitation.DateCreated
            def pubDate = article.MedlineCitation.Article.Journal.JournalIssue.PubDate

            def year = pubDate.Year
            if (year == '') {
                pubsNotUpdated.add([idMap[id], id])
                return
            }

            def month = pubDate.Month as String
            if (month == '') {
                month = createDate.Month
            } else if (months.containsKey(month)) {
                month = months[month]
            }
            if (month == '') {
                pubsNotUpdated.add([idMap[id], id])
                return
            }

            def day = pubDate.Day
            if (day == '') {
                day = '01'
            }

            newDate = "$month/$day/$year" as String
            pubsUpdated.add([idMap[id], id, newDate])
        }
    }

    pubsUpdated.each { println(it) }

    // for pubs with a new date, update the publication record and insert an update record
    sql.withTransaction {
        sql.withBatch('update publication set pub_date = ? where zdb_id = ?') { ps ->
            pubsUpdated.each { update -> ps.addBatch(update[2], update[0]) }
        }
        sql.withBatch('insert into updates (rec_id, field_name, new_value, comments, upd_when) values (?, "pub_date", ?, "automated pub date loader", current)') { ps ->
            pubsUpdated.each { update -> ps.addBatch(update[0], update[2]) }
        }
    }
}

def report = new ReportGenerator()
if (args) {
    report.setReportTitle("Report for ${args[0]}")
}
report.includeTimestamp()
report.addDataTable("Added date to ${pubsUpdated.size()} pubs",
        ["ZDB ID", "PubMed ID", "Publication Date"], pubsUpdated)
report.addDataTable("No date found for ${pubsNotUpdated.size()} pubs",
        ["ZDB ID", "PubMed ID"], pubsNotUpdated)
report.writeFiles(new File("."), "addPubDateReport")
