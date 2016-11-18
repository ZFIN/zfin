import org.apache.log4j.Logger
import org.hibernate.criterion.Restrictions
import org.zfin.Species
import org.zfin.infrastructure.DataAliasGroup
import org.zfin.infrastructure.DataNote
import org.zfin.marker.Marker
import org.hibernate.Session
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.marker.MarkerRelationship
import org.zfin.marker.MarkerAlias
import org.zfin.marker.MarkerType
import org.zfin.mutant.SequenceTargetingReagent
import org.zfin.ontology.datatransfer.AbstractScriptWrapper
import org.zfin.profile.Person
import org.zfin.properties.ZfinProperties
import org.zfin.publication.Publication
import org.zfin.repository.RepositoryFactory
import org.zfin.sequence.ForeignDB
import org.zfin.sequence.ForeignDBDataType
import org.zfin.sequence.MarkerDBLink
import org.zfin.sequence.ReferenceDatabase
import org.zfin.sequence.STRMarkerSequence
import org.zfin.sequence.DBLink
import javax.persistence.*
import org.hibernate.Session

import static com.xlson.groovycsv.CsvParser.parseCsv


Logger log = Logger.getLogger(getClass())

def env = System.getenv()

AbstractScriptWrapper abstractScriptWrapper = new AbstractScriptWrapper()
abstractScriptWrapper.initProperties("${env['TARGETROOT']}/home/WEB-INF/zfin.properties")
abstractScriptWrapper.initDatabaseWithoutSysmaster()
abstractScriptWrapper.initializeLogger("./log4j.xml")

Session session = HibernateUtil.currentSession()
session.beginTransaction()


def markerPrefix = "CRISPR"
def markerDelim = "-"


def burgessCRISPR = parseCsv(new FileReader("crisprz.txt"))

burgessCRISPR.each { csv ->

    println("""
----------------------------------------------------------------
            crispralias:    $csv.crisprID
            crisprseq:      $csv.sequence
            tgtgene:   $csv.geneabbrev
            """
    )
    def crisprIndex = 1
    Marker tgtGene = RepositoryFactory.markerRepository.getMarkerByAbbreviation(csv.geneabbrev)

    List<Publication> publication1 = RepositoryFactory.publicationRepository.getPublicationByPmid("26048245")
    if (publication1.empty) {
        usePub2 = true
    } else {
        usePub2 = false
        Publication pub=RepositoryFactory.publicationRepository.getPublication(publication1.first().zdbID)
    }

    Publication publication2 = RepositoryFactory.publicationRepository.getPublication("ZDB-PUB-151209-4") /*data load pub*/
    crisprName = markerPrefix+crisprIndex+markerDelim+csv.geneabbrev


//if a crispr for the gene already exists in ZFIN(but with a different sequence increment the crispr number)
    if (RepositoryFactory.markerRepository.getMarkerByName(crisprName)) {
        crisprCount = RepositoryFactory.markerRepository.getCrisprCount(tgtGene.zdbID)
        crisprIndex = crisprCount + 1
        crisprName = markerPrefix + crisprIndex + markerDelim + csv.geneabbrev
    }

    def crisprseq = csv.sequence
    STRMarkerSequence newSequenceTargetingReagentSequence = new STRMarkerSequence()

    newSequenceTargetingReagentSequence.setSequence(crisprseq)
    newSequenceTargetingReagentSequence.setType("Nucleotide")
/*
    Person owner = (Person) HibernateUtil.currentSession().createCriteria(Person.class)
            .add(Restrictions.eq("zdbID", "ZDB-PERS-030612-2"))  //Doug
            .uniqueResult();
*/
 // Person owner = getPerson("ZDB-PERS-030612-2")
    Marker newCRISPR = createNewCrispr(newSequenceTargetingReagentSequence, crisprName,publication2)
    MarkerAlias crisprAlias = createNewAlias(newCRISPR,csv.crisprID,publication2)
    MarkerRelationship newReln= createNewReln(newCRISPR,tgtGene,publication2)
    DBLink newDBLink=createNewDBLink(newCRISPR,csv.crisprID,publication2)



   if (usePub2== false) {
       Publication pub=RepositoryFactory.publicationRepository.getPublication(publication1.first().zdbID)
       RepositoryFactory.markerRepository.addMarkerPub(newCRISPR,pub)
        RepositoryFactory.infrastructureRepository.insertRecordAttribution(newReln.zdbID, pub.zdbID)
    }
    RepositoryFactory.markerRepository.addMarkerPub(newCRISPR,publication2)
    RepositoryFactory.infrastructureRepository.insertRecordAttribution(crisprAlias.zdbID,publication2.zdbID)
    RepositoryFactory.infrastructureRepository.insertRecordAttribution(newReln.zdbID, publication2.zdbID)
    RepositoryFactory.infrastructureRepository.insertRecordAttribution(newDBLink.zdbID, publication2.zdbID)
}




    if ("--rollback" in args)
        session.getTransaction().rollback()
    else
        session.getTransaction().commit()


    public Person getPerson(String zdbID) {
        return (Person) HibernateUtil.currentSession().createCriteria(Person.class)
                .add(Restrictions.eq("zdbID", zdbID))
                .uniqueResult()
    }

    Marker createNewCrispr(newSequenceTargetingReagentSequence, String crisprName,  Publication pub) {
        SequenceTargetingReagent newSequenceTargetingReagent = new SequenceTargetingReagent()
        MarkerType mt = RepositoryFactory.markerRepository.getMarkerTypeByName(Marker.Type.CRISPR.toString())
        newSequenceTargetingReagent.setMarkerType(mt)
       SequenceTargetingReagent crispR=new SequenceTargetingReagent()
        Person owner = (Person) HibernateUtil.currentSession().createCriteria(Person.class)
                .add(Restrictions.eq("zdbID", "ZDB-PERS-030520-2"))  //Doug
                .uniqueResult();
       // Person owner=RepositoryFactory.profileRepository.getPerson("ZDB-PERS-030612-2")
        crispR.setOwner(owner)
        crispR.setName(crisprName)
        crispR.setAbbreviation(crisprName)
        crispR.setMarkerType(mt)
        crispR.setSequence(newSequenceTargetingReagentSequence)
        HibernateUtil.currentSession().save(crispR)
        return crispR

    }


MarkerRelationship createNewReln(Marker newCrispr,Marker tgtGene,Publication pub) {
    MarkerRelationship mRel = new MarkerRelationship()
    mRel.setFirstMarker(newCrispr)
    mRel.setSecondMarker(tgtGene)
    mRel.setType(MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE)

    HibernateUtil.currentSession().save(mRel)
    return mRel
}

MarkerAlias createNewAlias(Marker newCrispr,String aliasStr,Publication pub) {
    MarkerAlias crisprAlias= new MarkerAlias()
    crisprAlias.setMarker(newCrispr);
    crisprAlias.setAlias(aliasStr)
    String groupName = DataAliasGroup.Group.ALIAS.toString();
    DataAliasGroup group = RepositoryFactory.infrastructureRepository.getDataAliasGroupByName(groupName);
    crisprAlias.setAliasGroup(group)
    HibernateUtil.currentSession().save(crisprAlias)
    return crisprAlias
}

DBLink createNewDBLink(Marker newCrispr,String accession,Publication pub) {
    MarkerDBLink mdb = new MarkerDBLink();
    mdb.setMarker(newCrispr);
    mdb.setAccessionNumber(accession);
    ReferenceDatabase crisprRefDB = RepositoryFactory.sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.CRISPRZ,
            ForeignDBDataType.DataType.OTHER, ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.Type.ZEBRAFISH);
    mdb.setReferenceDatabase(crisprRefDB);
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

