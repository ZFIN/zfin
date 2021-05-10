#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

import groovy.util.slurpersupport.GPathResult
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum
import groovy.xml.StreamingMarkupBuilder
import java.time.LocalDate

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

final WORKING_DIR = new File("${ZfinPropertiesEnum.TARGETROOT}/server_apps/data_transfer/PUBMED")
final PARSE_MESH = new File(WORKING_DIR, "parseMesh.log")
final PARSE_PUBS = new File(WORKING_DIR, "parsePubs.log")

def getTextWithMarkup(node) {
    // this is weird and maybe a little hacky but when, for example, the title or abstract contains html markup
    // the full document is no longer valid XML so the groovy XML tools don't give a very nice way to extract it.
    nodeString = new StreamingMarkupBuilder().bindNode(node).toString()
    nodeMatch = nodeString =~ /^<(\w*)\b[^>]*>(.*?)<\/\1>$/
    return (nodeMatch ? nodeMatch[0][2] : nodeString).replaceAll(/\R+/, ' ')
}

def processArticle = { CSVPrinter printer, GPathResult pubmedArticle, int idx ->
    row = []
    medlineCitation = pubmedArticle.MedlineCitation
    pubMedData = pubmedArticle.PubmedData
    pmcId = ""

    mId = ""
    if (pubMedData.ArticleList.ArticleId.@IdType == 'pmc' ) {
        pmcId = pubMedData.ArticleList.ArticleId.text()

    }
    if(pubMedData.ArticleList.ArticleId.@IdType == 'mid' ) {
        mId = pubMedData.ArticleList.ArticleId.text()

    }
    row.add(pmcId)
    row.add(mId)
    pmid = medlineCitation.PMID.text()
    if (pmid == '28539358') {
        return
    }

    row.add(pmid)
    row.add(medlineCitation.KeywordList.Keyword.iterator().collect { it.text().trim() }.join(", "))

    article = medlineCitation.Article
    row.add(getTextWithMarkup(article.ArticleTitle).replaceAll(/\.+$/, ''))
    row.add(article.Pagination.text())

    fullAbstract = ''
    article.Abstract.AbstractText.each { abstractText ->
        label = abstractText.@Label.text() ?: abstractText.@NlmCategory.text()
        text = getTextWithMarkup(abstractText)
        if (label && label != 'UNLABELLED' && label != 'UNASSIGNED') {
            fullAbstract += "<div class='pub-abstract-section'><span class='pub-abstract-section-label'>${label.toLowerCase().capitalize()}</span> ${text}</div>"
        } else {
            fullAbstract += text
        }
    }
    row.add(fullAbstract)

    authors = []
    article.AuthorList.Author.each { author ->
        if (!author.CollectiveName.isEmpty()) {
            authors.add(author.CollectiveName.text())
        } else {
            lastName = author.LastName.text()
            initials = author.Initials.text()
            authors.add("${lastName}, ${initials.split('').join('.')}.")
        }
    }
    row.add(authors.join(', '))
    row.add(authors.size())

    LocalDate pubDate = PubmedUtils.getPublicationDate(pubmedArticle)
    row.add(pubDate ? pubDate.getYear() : '')
    row.add(pubDate ? pubDate.getMonthValue() : '')
    row.add(pubDate ? pubDate.getDayOfMonth() : '')

    journal = article.Journal
    journalIssue = journal.JournalIssue
    row.add(journal.ISSN.text())
    row.add(journalIssue.Volume.text())
    row.add(journalIssue.Issue.text())
    row.add(journal.Title.text())
    row.add(journal.ISOAbbreviation.text())


    medlineCitation.MeshHeadingList.MeshHeading.each { meshHeading ->
        descriptor = meshHeading.DescriptorName
        descriptorId = descriptor.@UI.text()
        descriptorIsMajor = descriptor.@MajorTopicYN.text().tr('YN', 'tf')
        PARSE_MESH.append("${pmid}|${descriptorId}||${descriptorIsMajor}\n", "UTF-8")
        meshHeading.QualifierName.each { qualifier ->
            qualifierId = qualifier.@UI.text()
            qualifierIsMajor = qualifier.@MajorTopicYN.text().tr('YN', 'tf')
            PARSE_MESH.append("${pmid}|${descriptorId}|${qualifierId}|${qualifierIsMajor}\n", "UTF-8")
        }
    }

    row.add(pubmedArticle.PubmedData.PublicationStatus.text())
    def isReview = ''
    article.PublicationTypeList.PublicationType.each { pubtype ->
        if (pubtype == 'Review'){
            isReview = 'review'
            //print("pub is identified as review " + pmid + "\n")
        }
        if (pubtype == 'Preprint'){
            isReview = 'preprint'
            //print("pub is identified as review " + pmid + "\n")
        }
    }
    row.add(isReview)
    
    printer.printRecord(row.collect { col -> col.toString().replace('\n', '\\n') })
}

WORKING_DIR.eachFileMatch(~/new.*\.txt/) { it.delete() }
WORKING_DIR.eachFileMatch(~/loadSQL.*\.txt/) { it.delete() }
PARSE_MESH.delete()
PARSE_PUBS.delete()

PARSE_MESH.createNewFile()
PARSE_PUBS.createNewFile()

PARSE_PUBS.withWriter { pubLog ->
    def articles
    CSVPrinter printer = new CSVPrinter(pubLog, CSVFormat.TDF.withQuote(null))
    if (args.size() > 0) {
        def articleSet = PubmedUtils.getFromPubmed(args)
        articles = articleSet.PubmedArticle
    } else {
        def query = 'zebrafish[TW] OR "zebra fish"[TW] OR "danio rerio"[ALL]'
        articles = PubmedUtils.searchPubmed(query)
    }
    articles.eachWithIndex{ GPathResult article, int i -> processArticle(printer, article, i) }
}

dbaccessProc = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/loadNewPubs.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
dbaccessProc.waitFor()

