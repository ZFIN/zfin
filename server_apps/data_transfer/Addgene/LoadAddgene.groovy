#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import groovy.json.JsonSlurper
import org.hibernate.Session
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.Species
import org.zfin.infrastructure.RecordAttribution
import org.zfin.properties.ZfinProperties
import org.zfin.repository.RepositoryFactory
import org.zfin.sequence.ForeignDB
import org.zfin.sequence.ForeignDBDataType
import org.zfin.sequence.MarkerDBLink
import org.zfin.sequence.ReferenceDatabase
import org.zfin.util.ReportGenerator

import java.util.zip.GZIPInputStream
import java.util.zip.ZipException


cli = new CliBuilder(usage: 'LoadAddgene')
cli.jobName(args: 1, 'Name of the job to be displayed in report')
cli.localData('Attempt to load a local addgene-plasmids.json file instead of downloading')
cli.commit('Commits changes, otherwise rollback will be performed')
cli.report('Generates an HTML report')
options = cli.parse(args)
if (!options) {
    System.exit(1)
}

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
new HibernateSessionCreator()

DOWNLOAD_URL = "https://www.addgene.org/download/2cae1f5eb19075da8ba8de3ac954e4d5/plasmids/"

Session session = HibernateUtil.currentSession()
tx = session.beginTransaction()


addgeneDb = RepositoryFactory.sequenceRepository.getReferenceDatabase(
        ForeignDB.AvailableName.ADDGENE,
        ForeignDBDataType.DataType.OTHER,
        ForeignDBDataType.SuperType.SUMMARY_PAGE,
        Species.Type.ZEBRAFISH)
entrezGeneDb = session.get(ReferenceDatabase.class, 'ZDB-FDBCONT-040412-1')


if (options.localData) {
    print "Loading local JSON file ... "
    json = new JsonSlurper().parse(new FileReader("addgene-plasmids.json"))
} else {
    print "Downloading JSON file from $DOWNLOAD_URL ... "
    // download, gunzip, and parse json from addgene
    DOWNLOAD_URL
            .toURL()
            .newInputStream(requestProperties: ['User-Agent': 'ZFINbot/1.0'])
            .withStream { rawInputStream ->
        rawInputStream.mark(100)
        try {
            gzipInputStream = new GZIPInputStream(rawInputStream)
            gzipInputStream.withReader { gzipReader ->
                json = new JsonSlurper().parse(gzipReader)
            }
            gzipInputStream.close()
        } catch (ZipException e) {
            // possibly not in GZIP format?
            rawInputStream.reset()
            rawInputStream.withReader { reader ->
                json = new JsonSlurper().parse(reader)
            }
        } catch (IOException e) {
            println "Error downloading or unzipping Addgene JSON file. Please verify download works manually."
            e.printStackTrace()
            System.exit(1)
        }
    }
}
println "done"


print "Extracting IDs from JSON file ... "
entrezIdsFromAddgene = json.plasmids.collectMany { plasmid ->
    plasmid.inserts.collectMany { insert ->
        insert.entrez_gene.collect { gene -> gene.id as String }
    }
} as Set
println "done"


println "Deleting db links for Entrez IDs no longer supported by Addgene "
hql = """from MarkerDBLink dbl
         where dbl.referenceDatabase = :refDb
         and dbl.accessionNumber not in (:accNums)"""
query = session.createQuery(hql)
query.setParameter("refDb", addgeneDb)
query.setParameterList("accNums", entrezIdsFromAddgene)
linksToDelete = query.list()
linksToDelete.each { link ->
    println "  $link.zdbID"
    session.delete(link)
}


println "Adding new Addgene db links "
hql = """from MarkerDBLink
         where referenceDatabase = :entrezGeneDb
         and accessionNumber in (:accNums)
         and dataZdbID not in (
             select dataZdbID
             from MarkerDBLink
             where referenceDatabase = :addgeneDb
         )"""
query = session.createQuery(hql)
query.setParameter("addgeneDb", addgeneDb)
query.setParameter("entrezGeneDb", entrezGeneDb)
query.setParameterList("accNums", entrezIdsFromAddgene)
newLinks = query.list()

hql = """from MarkerDBLink
         where referenceDatabase = :entrezGeneDb
         and accessionNumber in (:accNums)
         and dataZdbID not in (
             select dataZdbID
             from MarkerDBLink
             where referenceDatabase = :addgeneDb
         )"""
query = session.createQuery(hql)
query.setParameter("addgeneDb", addgeneDb)
query.setParameter("entrezGeneDb", entrezGeneDb)
query.setParameterList("accNums", entrezIdsFromAddgene)
addedLinks = query.list().collect { entrezLink ->
    addgeneLink = new MarkerDBLink()
    addgeneLink.with {
        marker = entrezLink.marker
        accessionNumber = entrezLink.accessionNumber
        accessionNumberDisplay = entrezLink.accessionNumberDisplay
        linkInfo = String.format("Uncurated: addgene load for %tF", new Date())
        referenceDatabase = addgeneDb
    }
    session.save(addgeneLink)
    println "  $addgeneLink.zdbID"
    addgeneLink
    println "Adding new attributions test "
    recAttr = new RecordAttribution()
    recAttr.with {
        dataZdbID = addgeneLink.zdbID
        sourceZdbID = "ZDB-PUB-160316-6"
        sourceType = RecordAttribution.SourceType.STANDARD
    }
    session.save(recAttr)
    recAttr
}
println "getting db links for IDs  "
hql = """from MarkerDBLink dbl
         where referenceDatabase = :addgeneDb"""

query = session.createQuery(hql)
query.setParameter("addgeneDb", addgeneDb)

linksAdded = query.list()
linksAdded.each { link ->
    println "  $link.zdbID"

}

if (options.report) {
    // one more query that only matters if we're doing a report
    hql = """select count(*)
             from MarkerDBLink
             where referenceDatabase = :addgeneDb"""
    query = session.createQuery(hql)
    query.setParameter("addgeneDb", addgeneDb)
    count = query.uniqueResult()

    print "Generating report ... "
    ReportGenerator rg = new ReportGenerator();
    rg.setReportTitle("Report for $options.jobName")
    rg.includeTimestamp();
    rg.addIntroParagraph("With this load there are now $count Addgene links in total.")
    rg.addDataTable("${linksToDelete.size()} Links Removed", ["Gene", "Accession Number"], linksToDelete.collect { link -> [link.getMarker().getZdbID(), link.getAccessionNumber()] })
    rg.addDataTable("${newLinks.size()} Links Added", ["Gene", "Accession Number"], newLinks.collect { link -> [link.getMarker().getZdbID(), link.getAccessionNumber()] })
    new File("addgene-report.html").withWriter { writer ->
        rg.write(writer, ReportGenerator.Format.HTML)
    }
    println "done"
}


if (options.commit) {
    print "Committing changes ... "
    tx.commit()
} else {
    print "Rolling back changes ... "
    tx.rollback()
}
session.close()
println "done"

System.exit(0)

