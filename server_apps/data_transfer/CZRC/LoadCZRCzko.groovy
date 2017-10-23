#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?


import org.apache.log4j.Logger
import org.hibernate.criterion.Restrictions
import org.zfin.Species
import org.zfin.feature.Feature
import org.zfin.feature.FeatureMarkerRelationship
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum
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
import org.zfin.sequence.STRMarkerSequence

import javax.persistence.*
import org.hibernate.Session

import static com.xlson.groovycsv.CsvParser.parseCsv


Logger log = Logger.getLogger(getClass())

def env = System.getenv()

/*AbstractScriptWrapper abstractScriptWrapper = new AbstractScriptWrapper()
abstractScriptWrapper.initProperties("${env['TARGETROOT']}/home/WEB-INF/zfin.properties")
abstractScriptWrapper.initDatabaseWithoutSysmaster()
abstractScriptWrapper.initializeLogger("./log4j.xml")*/

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
new HibernateSessionCreator()



Session session = HibernateUtil.currentSession()
 session.beginTransaction()


def markerPrefix = "CRISPR"
def markerDelim = "-"



def zkoCRISPR = parseCsv(new FileReader("zkoCRISPRs.csv"))

zkoCRISPR.each { csv ->

    println("""
----------------------------------------------------------------
            feature:    $csv.feature
            sequence:      $csv.sequence
            tgtgeneid:   $csv.tgtgeneid
tgtgenesymbol:   $csv.tgtgenesymbol
mutationtype:   $csv.mutationtype
pageURL:         $csv.pageURL
            """
    )
    Publication pub = RepositoryFactory.publicationRepository.getPublication('ZDB-PUB-171002-4')
    def crisprIndex = 1
    println csv.sequence[0..-4]
    if (csv.tgtgeneid == '') {
        List<Marker> tgtGenes = RepositoryFactory.featureRepository.getMarkerIsAlleleOf(RepositoryFactory.featureRepository.getFeatureByAbbreviation(csv.feature));
        tgtGene=RepositoryFactory.markerRepository.getMarkerByAbbreviation(tgtGenes[0].getAbbreviation())
        geneSym = tgtGenes[0].getAbbreviation();
    } else {

        tgtGene = RepositoryFactory.markerRepository.getMarkerOrReplacedByID(csv.tgtgeneid);
        geneSym = tgtGene.getAbbreviation();
    }






    crisprName = markerPrefix + crisprIndex + markerDelim + geneSym

//if a crispr for the gene already exists in ZFIN(but with a different sequence increment the crispr number)
    if (RepositoryFactory.markerRepository.getMarkerByName(crisprName)) {
        if (RepositoryFactory.markerRepository.getSequenceTargetingReagentBySequence(Marker.Type.CRISPR, csv.sequence[0..-4])) {
            println crisprName
            RepositoryFactory.markerRepository.addMarkerPub(RepositoryFactory.markerRepository.getMarkerByName(crisprName), pub)
            extCRISPR = RepositoryFactory.markerRepository.getMarkerByName(crisprName)
            Feature ftr = RepositoryFactory.featureRepository.getFeatureByAbbreviation(csv.feature)
            FeatureMarkerRelationship newFMRel = createNewFMReln(extCRISPR, ftr, pub)
            RepositoryFactory.infrastructureRepository.insertRecordAttribution(newFMRel.zdbID, pub.zdbID)
        }
    }
    if (!RepositoryFactory.markerRepository.getMarkerByName(crisprName)){
            print tgtGene
            crisprCount = RepositoryFactory.markerRepository.getCrisprCount(tgtGene.zdbID)
            crisprIndex = crisprCount + 1
            crisprName = markerPrefix + crisprIndex + markerDelim + geneSym
            def crisprseq = csv.sequence[0..-4]
            STRMarkerSequence newSequenceTargetingReagentSequence = new STRMarkerSequence()
            newSequenceTargetingReagentSequence.setSequence(crisprseq)
            newSequenceTargetingReagentSequence.setType("Nucleotide")
            Marker newCRISPR = createNewCrispr(newSequenceTargetingReagentSequence, crisprName, pub)
            println newCRISPR.abbreviation
            MarkerRelationship newReln = createNewReln(newCRISPR, tgtGene, pub)
            Feature ftr = RepositoryFactory.featureRepository.getFeatureByAbbreviation(csv.feature)
            RepositoryFactory.markerRepository.addMarkerPub(newCRISPR, pub)
            RepositoryFactory.infrastructureRepository.insertRecordAttribution(newReln.zdbID, pub.zdbID)
            FeatureMarkerRelationship newFMRel = createNewFMReln(newCRISPR, ftr, pub)
            RepositoryFactory.infrastructureRepository.insertRecordAttribution(newFMRel.zdbID, pub.zdbID)

        }

    }


    if ("--rollback" in args)
        session.getTransaction().rollback()
    else
        session.getTransaction().commit()

session.close()
println "done"
System.exit(0)
   public Person getPerson(String zdbID) {
        return (Person) HibernateUtil.currentSession().createCriteria(Person.class)
                .add(Restrictions.eq("zdbID", zdbID))
                .uniqueResult()
    }

    Marker createNewCrispr(newSequenceTargetingReagentSequence, String crisprName, Publication pub) {
        SequenceTargetingReagent newSequenceTargetingReagent = new SequenceTargetingReagent()
        MarkerType mt = RepositoryFactory.markerRepository.getMarkerTypeByName(Marker.Type.CRISPR.toString())
        newSequenceTargetingReagent.setMarkerType(mt)
        SequenceTargetingReagent crispR = new SequenceTargetingReagent()
        Person owner = (Person) HibernateUtil.currentSession().createCriteria(Person.class)
                .add(Restrictions.eq("zdbID", "ZDB-PERS-981201-7"))  //Doug
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


    MarkerRelationship createNewReln(Marker newCrispr, Marker tgtGene, Publication pub) {
        MarkerRelationship mRel = new MarkerRelationship()
        mRel.setFirstMarker(newCrispr)
        mRel.setSecondMarker(tgtGene)
        mRel.setType(MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE)

        HibernateUtil.currentSession().save(mRel)
        return mRel
    }
    FeatureMarkerRelationship createNewFMReln(Marker newCrispr, Feature ftr, Publication pub) {
        FeatureMarkerRelationship fmRel = new FeatureMarkerRelationship()
        fmRel.setMarker(newCrispr)
        fmRel.setFeature(ftr)
        fmRel.setType(FeatureMarkerRelationshipTypeEnum.CREATED_BY)

        HibernateUtil.currentSession().save(fmRel)
        return fmRel
    }

