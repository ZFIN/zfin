#!/bin/bash
//usr/bin/env groovy -cp "<!--|GROOVY_CLASSPATH|-->:." "$0" $@; exit $?

import java.time.LocalDate
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

final WORKING_DIR = new File("${ZfinPropertiesEnum.TARGETROOT}/server_apps/data_transfer/PUBMED")
final PARSE_MESH = new File(WORKING_DIR, "parseMesh.log")
final PARSE_PUBS = new File(WORKING_DIR, "parsePubs.log")

def processArticle = { pubmedArticle, idx ->
    row = []
    medlineCitation = pubmedArticle.MedlineCitation
    pmid = medlineCitation.PMID.text()
    if (pmid == '28539358') {
        return
    }

    row.push(pmid)
    row.push(medlineCitation.KeywordList.Keyword.iterator().collect { it.text() }.join(", "))

    article = medlineCitation.Article
    row.push(article.ArticleTitle.text().replaceAll(/\.+$/, ''))
    row.push(article.Pagination.text())

    fullAbstract = ''
    article.Abstract.AbstractText.each { abstractText ->
        label = abstractText.@Label.text() ?: abstractText.@NlmCategory.text()
        text = abstractText.text().replaceAll(/\|/, '\\|')
        if (label && label != 'UNLABELLED' && label != 'UNASSIGNED') {
            fullAbstract += "<div class='pub-abstract-section'><span class='pub-abstract-section-label'>${label.toLowerCase().capitalize()}</span> ${text}</div>"
        } else {
            fullAbstract += text
        }
    }
    abstractFile = new File(WORKING_DIR, "abstract${idx}.clob")
    abstractFile.write(fullAbstract, "UTF-8")
    row.push(abstractFile.absolutePath)

    authors = []
    article.AuthorList.Author.each { author ->
        if (!author.CollectiveName.isEmpty()) {
            authors.push(author.CollectiveName.text())
        } else {
            lastName = author.LastName.text()
            initials = author.Initials.text()
            authors.push("${lastName}, ${initials.split('').join('.')}.")
        }
    }
    row.push(authors.join(', '))
    row.push(authors.size())

    LocalDate pubDate = PubmedUtils.getPublicationDate(pubmedArticle)
    row.push(pubDate ? pubDate.getYear() : '')
    row.push(pubDate ? pubDate.getMonthValue() : '')
    row.push(pubDate ? pubDate.getDayOfMonth() : '')

    journal = article.Journal
    journalIssue = journal.JournalIssue
    row.push(journal.ISSN.text())
    row.push(journalIssue.Volume.text())
    row.push(journalIssue.Issue.text())
    row.push(journal.Title.text())
    row.push(journal.ISOAbbreviation.text())

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

    row.push(pubmedArticle.PubmedData.PublicationStatus.text())

    PARSE_PUBS.append(row.collect { it.toString().replaceAll(/\|/, '\\|') }.join('|') + "\n", "UTF-8")
}

WORKING_DIR.eachFileMatch(~/abstract.*\.clob/) { it.delete() }
WORKING_DIR.eachFileMatch(~/new.*\.txt/) { it.delete() }
WORKING_DIR.eachFileMatch(~/loadSQL.*\.txt/) { it.delete() }
PARSE_MESH.delete()
PARSE_PUBS.delete()

PARSE_MESH.createNewFile()
PARSE_PUBS.createNewFile()

if (args.size() > 0) {
    def articleSet = PubmedUtils.getFromPubmed(args)
    articleSet.PubmedArticle.eachWithIndex(processArticle)
} else {
    def query = 'zebrafish[TW] OR "zebra fish"[TW] OR "danio rerio"[ALL]'
    PubmedUtils.searchPubmed(query).eachWithIndex(processArticle)
}

['/bin/bash', '-c', "${ZfinPropertiesEnum.INFORMIXDIR}/bin/dbaccess " +
        "-a ${ZfinPropertiesEnum.DB_NAME} ${WORKING_DIR.absolutePath}/loadNewPubs.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
