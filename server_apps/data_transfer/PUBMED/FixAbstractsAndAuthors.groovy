import org.apache.commons.lang3.text.translate.NumericEntityEscaper
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.infrastructure.Updates
import org.zfin.properties.ZfinProperties
import org.zfin.publication.Publication
import org.zfin.util.ZfinStringUtils

ZfinProperties.init("../../../home/WEB-INF/zfin.properties")
new HibernateSessionCreator()

NCBI_BATCH_SIZE = 150
PUB_ARRIVAL_START_DATE = "2014-05-13"       // first day of using pub acquisition script


def saveUpdate(pubID, field, newVal, oldVal = null) {
    Updates updates = new Updates()
    updates.with {
        recID = pubID
        fieldName = field
        newValue = newVal
        oldValue = oldVal
        comments = "FixAbstractsAndAuthors.groovy script"
        whenUpdated = new Date()
    }
    HibernateUtil.currentSession().save(updates)
}


tx = HibernateUtil.currentSession().beginTransaction()

// using createSQLQuery because pub_arrival_date column isn't mapped
pubs = HibernateUtil.currentSession()
        .createSQLQuery("select * from publication where pub_arrival_date >= ?")
        .setString(0, PUB_ARRIVAL_START_DATE)
        .addEntity(Publication.class)
        .list()
pubs.retainAll { pub -> pub.accessionNumber?.isInteger() }

// collate so we don't need too many fetches
pubs.collate(NCBI_BATCH_SIZE).each { pubBatch ->

    // need to look up pubs by pubmed id number later
    pubMap = pubBatch.collectEntries { pub -> [pub.accessionNumber, pub] }

    // fetch pub batch and parse results
    ids = pubMap.keySet().join(",")
    url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=$ids&retmode=xml"
    xmlPubs = new XmlSlurper().parse(url)

    xmlPubs.PubmedArticle.each { xmlPub ->

        // look up the pub in the map
        pmid = xmlPub.MedlineCitation.PMID.text()
        pub = pubMap[pmid]

        // pub acquisition script didn't process CollectiveName nodes previously.
        // if we find one of those, regenerate the authors column of the pub.
        authorList = xmlPub.MedlineCitation.Article.AuthorList
        if (authorList.depthFirst().find { it.name() == 'CollectiveName'} ) {
            authorNames = []
            authorList.Author.each { author ->
                if (!author.CollectiveName.isEmpty()) {
                    authorNames << author.CollectiveName.text()
                } else {
                    lastName = author.LastName.text()
                    foreName = author.ForeName.text()
                    initials = author.Initials.text().split("")[1..-1].join(".") + "."
                    authorNames << "$lastName, $initials"
                }
            }
            oldAuthors = pub.authors;
            newAuthors = authorNames.join(", ")
            pub.authors = newAuthors
            println "Authors".padRight(10) + pub.zdbID
            HibernateUtil.currentSession().save(pub)
            saveUpdate(pub.zdbID, "authors", newAuthors, oldAuthors)
        }

        // find AbstractText element(s) -- if there are more than one, assume we need to rebuild
        // the abstract string using the NlmCategory and Label attributes and the elements' text.
        xmlAbstractText = xmlPub.MedlineCitation.Article.Abstract.AbstractText
        if (xmlAbstractText?.size() > 1) {
            abstractText = ""
            xmlAbstractText.each { node ->
                cat = node.@NlmCategory
                label = node.@Label
                text = node.text()
                if ((!label.isEmpty() && label != "UNLABELLED") || (!cat.isEmpty() && cat != "UNLABELLED")) {
                    displayLabel = (label.isEmpty() ? cat : label).text().toLowerCase().capitalize()
                    abstractText += "<div class='pub-abstract-section'><span class='pub-abstract-section-label'>$displayLabel</span> $text</div>"
                } else {
                    abstractText += text
                }
            }
            // update the pub -- need SQL because pub_abstract column is also not mapped!
            println "Abstract".padRight(10) + pub.zdbID
            HibernateUtil.currentSession()
                    .createSQLQuery("update publication set pub_abstract = ? where zdb_id = ?")
                    .setString(0, ZfinStringUtils.escapeHighUnicode(abstractText))
                    .setString(1, pub.zdbID)
                    .executeUpdate()
            saveUpdate(pub.zdbID, "pub_abstract", abstractText)
        }
    }
}
tx.commit()
