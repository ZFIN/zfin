#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import groovy.json.JsonSlurper
import org.hibernate.Session
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.Species
import org.zfin.infrastructure.PublicationAttribution
import org.zfin.properties.ZfinProperties
import org.zfin.repository.RepositoryFactory
import org.zfin.sequence.ForeignDB
import org.zfin.sequence.ForeignDBDataType
import org.zfin.sequence.MarkerDBLink
import org.zfin.sequence.ReferenceDatabase
import org.zfin.infrastructure.RecordAttribution
import org.zfin.util.ReportGenerator

import java.util.zip.GZIPInputStream
import java.util.zip.ZipException



cli = new CliBuilder(usage: 'LoadSignafish')
cli.jobName(args:1, 'Name of the job to be displayed in report')
cli.localData('Attempt to load a local addgene-plasmids.json file instead of downloading')
cli.commit('Commits changes, otherwise rollback will be performed')
cli.report('Generates an HTML report')
options = cli.parse(args)
if (!options) {
    System.exit(1)
}

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
new HibernateSessionCreator()


Session session = HibernateUtil.currentSession()
tx = session.beginTransaction()


signafishDb = RepositoryFactory.sequenceRepository.getReferenceDatabase(
        ForeignDB.AvailableName.SIGNAFISH,
        ForeignDBDataType.DataType.OTHER,
        ForeignDBDataType.SuperType.SUMMARY_PAGE,
        Species.Type.ZEBRAFISH)

DOWNLOAD_URL = "http://signafish.org/zfin_ids.lst"

def file = new FileOutputStream(DOWNLOAD_URL.tokenize("/")[-1])
def out = new BufferedOutputStream(file)
out << new URL(DOWNLOAD_URL).openStream()
out.close()


println "done"
File inputFile=new File("zfin_ids.lst")
def line
inputFile.withReader { reader ->
    while ((line = reader.readLine()) != null) {
        println " ${line}"

    }
}
def array = new File('zfin_ids.lst') as Set

/*print "Extracting IDs from JSON file ... "
entrezIdsFromAddgene = json.plasmids.collectMany { plasmid ->
    plasmid.inserts.collectMany { insert ->
        insert.entrez_gene.collect { gene -> gene.id as String }
    }
} as Set
println "done"*/

print array.first()

println "Deleting db links for IDs no longer supported by Addgene "
hql = """from MarkerDBLink dbl
         where dbl.referenceDatabase = :refDb
         and dbl.accessionNumber not in (:accNums)"""
query = session.createQuery(hql)
query.setParameter("refDb", signafishDb)
query.setParameterList("accNums", array)
linksToDelete = query.list()
linksToDelete.each { link ->
    println "  $link.zdbID"
    session.delete(link)
}


println "Adding new Signafish db links "
hql = """from Marker
         where zdbID in (:accNums)
          """
query = session.createQuery(hql)

query.setParameterList("accNums", array)
addedLinks = query.list().collect { genes ->
    signafishLink = new MarkerDBLink()
    signafishLink.with {
        marker = genes
        accessionNumber = genes.zdbID
        accessionNumberDisplay = genes.zdbID
        linkInfo = String.format("Uncurated: signafish load for %tF", new Date())
        referenceDatabase = signafishDb
    }
    session.save(signafishLink)
    println "  $signafishLink.zdbID"
    signafishLink
    println "Adding new attributions test "
    recAttr = new RecordAttribution()
    recAttr.with {

        dataZdbID = signafishLink.zdbID
        sourceZdbID = "ZDB-PUB-160316-7"
        sourceType=RecordAttribution.SourceType.STANDARD
    }
    session.save(recAttr)
    recAttr
}
println "getting db links for IDs  "
hql = """from MarkerDBLink dbl
         where referenceDatabase = :signafishDb"""

query = session.createQuery(hql)
query.setParameter("signafishDb", signafishDb)

linksAdded = query.list()
linksAdded.each { link ->
    println "  $link.zdbID"
    session.delete(link)
}

if (options.report) {
    // one more query that only matters if we're doing a report
    hql = """select count(*)
             from MarkerDBLink
             where referenceDatabase = :signafishDb"""
    query =  session.createQuery(hql)
    query.setParameter("signafishDb", signafishDb)
    count = query.uniqueResult()

    print "Generating report ... "
    ReportGenerator rg = new ReportGenerator();
    rg.setReportTitle("Report for $options.jobName")
    rg.includeTimestamp();
    rg.addIntroParagraph("With this load there are now $count Signafish links in total.")
    rg.addDataTable("${linksToDelete.size()} Links Removed", ["Gene", "Accession Number"], linksToDelete.collect { link -> [link.getMarker().getZdbID(), link.getAccessionNumber()] })
    rg.addDataTable("${linksAdded.size()} Links Added", ["Gene", "Accession Number"], linksAdded.collect { link -> [link.getMarker().getZdbID(), link.getAccessionNumber()] })
    new File("signafish-report.html").withWriter { writer ->
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

