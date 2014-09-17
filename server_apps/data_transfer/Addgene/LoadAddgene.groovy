#!/bin/bash
//usr/bin/env groovy -cp "$SOURCEROOT/home/WEB-INF/lib*:$SOURCEROOT/lib/Java/*:$SOURCEROOT/home/WEB-INF/classes:$CATALINA_HOME/endorsed/*" "$0" $@; exit $?

import freemarker.template.Configuration
import groovy.json.JsonSlurper
import org.hibernate.Session
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.orthology.Species
import org.zfin.properties.ZfinProperties
import org.zfin.repository.RepositoryFactory
import org.zfin.sequence.ForeignDB
import org.zfin.sequence.ForeignDBDataType
import org.zfin.sequence.MarkerDBLink
import org.zfin.sequence.ReferenceDatabase

import java.util.zip.GZIPInputStream


cli = new CliBuilder(usage: 'LoadAddgene')
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

DOWNLOAD_URL = "https://www.addgene.org/download/2cae1f5eb19075da8ba8de3ac954e4d5/plasmids/"

Session session = HibernateUtil.currentSession()
tx = session.beginTransaction()


addgeneDb = RepositoryFactory.sequenceRepository.getReferenceDatabase(
        ForeignDB.AvailableName.ADDGENE,
        ForeignDBDataType.DataType.OTHER,
        ForeignDBDataType.SuperType.SUMMARY_PAGE,
        Species.ZEBRAFISH)
entrezGeneDb = session.get(ReferenceDatabase.class, 'ZDB-FDBCONT-040412-1')


if (options.localData) {
    print "Loading local JSON file ... "
    json = new JsonSlurper().parse(new FileReader("addgene-plasmids.json"))
} else {
    print "Downloading JSON file from $DOWNLOAD_URL ... "
    // download, gunzip, and parse json from addgene
    DOWNLOAD_URL.toURL().withInputStream { rawInputStream ->
        try {
            gzipInputStream = new GZIPInputStream(rawInputStream)
            gzipInputStream.withReader { gzipReader ->
                json = new JsonSlurper().parse(gzipReader)
            }
            gzipInputStream.close()
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
}


if (options.report) {
    // one more query that only matters if we're doing a report
    hql = """select count(*)
             from MarkerDBLink
             where referenceDatabase = :addgeneDb"""
    query =  session.createQuery(hql)
    query.setParameter("addgeneDb", addgeneDb)
    count = query.uniqueResult()

    print "Generating report ... "
    config = new Configuration()
    template = config.getTemplate("addgene-email.ftl")
    root = [jobName: options.jobName ?: "",
            dateRun: new Date(),
            deletedLinks: linksToDelete,
            addedLinks: addedLinks,
            totalLinks: count]
    new File("addgene-report.html").withWriter { writer ->
        template.process(root, writer)
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

