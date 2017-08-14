#!/bin/bash
import org.hibernate.Session
import org.zfin.Species
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.gwt.root.util.StringUtils
import org.zfin.infrastructure.RecordAttribution
import org.zfin.marker.Marker

//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
import org.zfin.publication.Publication
import org.zfin.repository.RepositoryFactory
import org.zfin.sequence.DBLink
import org.zfin.sequence.ForeignDB
import org.zfin.sequence.ForeignDBDataType
import org.zfin.sequence.MarkerDBLink
import org.zfin.sequence.ReferenceDatabase
import org.zfin.util.ReportGenerator
import static com.xlson.groovycsv.CsvParser.parseCsv


cli = new CliBuilder(usage: 'LoadPanther')
cli.jobName(args:1, 'Name of the job to be displayed in report')
cli.localData('Attempt to load a local panther file instead of downloading')
cli.commit('Commits changes, otherwise rollback will be performed')
cli.report('Generates an HTML report')
options = cli.parse(args)
if (!options) {
    System.exit(1)
}


ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
DOWNLOAD_URL = "ftp://ftp.pantherdb.org/sequence_classifications/12.0/PANTHER_Sequence_Classification_files/PTHR12.0_zebrafish"
def file = new FileOutputStream(DOWNLOAD_URL.tokenize("/")[-1])
def out = new BufferedOutputStream(file)
out << new URL(DOWNLOAD_URL).openStream()
out.close()

new HibernateSessionCreator()


File inputFile=new File("PTHR12.0_zebrafish")

Session session = HibernateUtil.currentSession()
tx = session.beginTransaction()


ReferenceDatabase pantherDb = RepositoryFactory.sequenceRepository.getReferenceDatabase(
        ForeignDB.AvailableName.PANTHER,
        ForeignDBDataType.DataType.OTHER,
        ForeignDBDataType.SuperType.SUMMARY_PAGE,
        Species.Type.ZEBRAFISH)

def pantherIDs = parseCsv(new FileReader(inputFile), separator: '|')

pantherIDs.each { csv ->

    def zfinID = csv[1].substring(csv[1].lastIndexOf('=') + 1)
    def pantid = csv[2].split('\t')
    def colon = (pantid[2].indexOf(':'))
    def panthid = pantid[2]
    def pantherID = panthid.substring(0, colon)
    if (zfinID.startsWith("ZDB")) {
        println zfinID
        Marker pantGene = RepositoryFactory.getMarkerRepository().getMarkerOrReplacedByID(zfinID)
         MarkerDBLink pantDbId = RepositoryFactory.getMarkerRepository().getDBLink(pantGene, pantherID, pantherDb)
        if (pantDbId == null) {
            DBLink newDBLink=createNewDBLink(pantGene,pantherID,pantherDb)
            RepositoryFactory.infrastructureRepository.insertRecordAttribution(newDBLink.zdbID, 'ZDB-PUB-170810-14')
                    } else {
            if (pantherID != pantDbId.accessionNumber) {
                pantDbId.setAccessionNumber(pantherID)
            }
        }

    }
}
if (options.report) {
    // one more query that only matters if we're doing a report
    hql = """select count(*)
             from MarkerDBLink
             where referenceDatabase = :pantDb"""
    query =  session.createQuery(hql)
    query.setParameter("pantDb", pantherDb)
    count = query.uniqueResult()

    print "Generating report ... "
    ReportGenerator rg = new ReportGenerator();
    rg.setReportTitle("Report for $options.jobName")
    rg.includeTimestamp();
    rg.addIntroParagraph("With this load there are now $count Panther records in total.")
    //rg.addDataTable("${linksToDelete.size()} Links Removed", ["Gene", "Accession Number"], linksToDelete.collect { link -> [link.getMarker().getZdbID(), link.getAccessionNumber()] })
    //rg.addDataTable("${linksAdded.size()} Links Added", ["Gene", "Accession Number"], linksAdded.collect { link -> [link.getMarker().getZdbID(), link.getAccessionNumber()] })
    new File("panther-report.html").withWriter { writer ->
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

DBLink createNewDBLink(Marker newCrispr, String accession, ReferenceDatabase pantDB) {
    MarkerDBLink mdb = new MarkerDBLink();
    mdb.setMarker(newCrispr);
    mdb.setAccessionNumber(accession);

    mdb.setReferenceDatabase(pantDB);
    Set<MarkerDBLink> markerDBLinks = newCrispr.getDbLinks();
    if (markerDBLinks == null) {
        markerDBLinks = new HashSet<MarkerDBLink>();
        markerDBLinks.add(mdb);
        newCrispr.setDbLinks(markerDBLinks);
    } else
        newCrispr.getDbLinks().add(mdb);
    HibernateUtil.currentSession().save(mdb);
    return mdb;

}
