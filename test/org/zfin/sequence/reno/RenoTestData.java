package org.zfin.sequence.reno;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.zfin.Species;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.blast.Query;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.Date;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * This class adds Reno test data.
 */
public class RenoTestData {

    private final Logger logger = LogManager.getLogger(RenoTestData.class);
    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private ProfileRepository personRepository = RepositoryFactory.getProfileRepository();
    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();


    public void setUpSharedRedundancyAndNomenclatureData() {
        Session session = currentSession();

        Marker gene = new Marker();
        gene.setAbbreviation("reno");
        gene.setName("Reno Test Name");
        //should this be an enum?
        gene.setMarkerType(markerRepository.getMarkerTypeByName(Marker.Type.GENE.toString()));
//        gene.setMarkerType(markerRepository.getMarkerTypeByName("GENE"));
        gene.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(gene);

        Marker cDNA = new Marker();
        cDNA.setAbbreviation("MGC:test");
        cDNA.setName("MGC:test");
        //should this be an enum?
        cDNA.setMarkerType(markerRepository.getMarkerTypeByName(Marker.Type.CDNA.toString()));
        cDNA.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(cDNA);

    }

    public String createRedundancyData() {
        return createRedundancyData("zgc:test");
    }

    /**
     * Returns run candidate ZDB ID for Redundancy.
     *
     * @return zdb ID
     */
    public String createRedundancyData(String candidateSuggestedName) {
        Session session = currentSession();

        Publication publication = publicationRepository.getPublication("ZDB-PUB-070122-15");

        // create Run
        RedundancyRun redunRun = new RedundancyRun();
        redunRun.setRelationPublication(publication);
        redunRun.setName("TestRedunRun");
//        redunRun.setType(Run.Type.REDUNDANCY);
        redunRun.setProgram("BLASTN");
        redunRun.setBlastDatabase("zfin_cdna_seq");
        redunRun.setNomenclaturePublication(publication);
        Date date = new Date();
        logger.debug("date: " + date);
        redunRun.setDate(date);
        session.save(redunRun);

        // create Candidate
        Candidate candidate = new Candidate();
        candidate.setRunCount(1);
        candidate.setLastFinishedDate(new Date());
        //candidate.setIdentifiedMarker(cDNA);
        candidate.setSuggestedName(candidateSuggestedName);
        candidate.setMarkerType(Marker.Type.GENE.toString());
//        candidate.setIdentifiedMarker(markerRepository.getMarkerByAbbreviation("MGC:test"));
//        logger.debug("the candidate bean identified marker is:" + candidate.getIdentifiedMarker().getAbbreviation());
        session.save(candidate);


        // create RunCandidateRedun
        RunCandidate runCandidate = new RunCandidate();
        runCandidate.setRun(redunRun);
        runCandidate.setDone(false);
        runCandidate.setLockPerson(personRepository.getPerson("ZDB-PERS-030520-1"));
        runCandidate.setCandidate(candidate);
        session.save(runCandidate);


        ReferenceDatabase refDb = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.RNA,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.Type.ZEBRAFISH);

        Accession accession1 = new Accession();
        //accession1.setID((long) 1);
        accession1.setNumber("AC:TEST1");
        accession1.setDefline("defline jam");
        accession1.setLength(12);
        accession1.setReferenceDatabase(refDb);
        //logger.info("accession1 number is:"+accession1.getID());

        // note that it FAILS here because it tries to save the marker again.
        MarkerDBLink queryGene = new MarkerDBLink();
        queryGene.setMarker(markerRepository.getMarkerByAbbreviation("MGC:test"));
        queryGene.setAccessionNumber(accession1.getNumber());
        queryGene.setAccessionNumberDisplay(accession1.getNumber());
        queryGene.setLength(1243);
        queryGene.setReferenceDatabase(accession1.getReferenceDatabase());
        session.save(queryGene);
        session.save(accession1);

        Accession accession2 = new Accession();
        //accession2.setID((long) 2);
        accession2.setNumber("AC:TEST2");
        accession2.setDefline("defline jam");
        accession2.setLength(12);
        accession2.setReferenceDatabase(refDb);
        session.save(accession2);
        //logger.info("accession2 number is:" +accession2.getID());

        // create Redundancy Query
        Query query = new Query();
        query.setRunCandidate(runCandidate);
        query.setAccession(accession1);
        runCandidate.getCandidateQueries().add(query);
        session.save(query);


        // create 5 Hits
        Hit hit1 = new Hit();
        hit1.setQuery(query);
        hit1.setHitNumber(1);
        hit1.setTargetAccession(accession2);
        query.getBlastHits().add(hit1);

        //when we have methods to create a gene, then create one instead of using exiting
        //that could be merged/deleted; or maybe we should modify the getMarkerByAbbreviation method
        //to check for aliases...

//        hit1.setZfinAccession(markerRepository.getMarkerByAbbreviation("reno"));
        hit1.setExpectValue(0.00);
        hit1.setScore(999);
        hit1.setPositivesNumerator(1);
        hit1.setPositivesDenominator(1);
        session.save(hit1);

        return runCandidate.getZdbID();

    }

    /**
     * Returns run candidate ZDB ID for Nomenclature.
     *
     * @return zdb ID
     */
    public String createNomenclatureData() {
        Session session = currentSession();
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        Publication publication = publicationRepository.getPublication("ZDB-PUB-070122-15");
        Publication publicationO = publicationRepository.getPublication("ZDB-PUB-030905-1");

        //create a NomenAccession
        Accession accessionNomenQuery = new Accession();
        accessionNomenQuery.setNumber("AC:NOMENQUERY");
        logger.debug("accessionNomenQuery" + accessionNomenQuery.getNumber());
        accessionNomenQuery.setDefline("defline jam for NOMENQUERY");
        accessionNomenQuery.setLength(12);
        ReferenceDatabase refDbNomenQuery = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.GENPEPT,
                ForeignDBDataType.DataType.POLYPEPTIDE,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.Type.ZEBRAFISH);
        logger.debug("refdbNomen " + refDbNomenQuery.getForeignDB().getDbName());
        accessionNomenQuery.setReferenceDatabase(refDbNomenQuery);
        session.save(accessionNomenQuery);

        //create a NomenAccession
        Accession accessionNomenHit1 = new Accession();
        accessionNomenHit1.setNumber("AC:NOMENHIT");
        logger.debug("accessionNomenHit1" + accessionNomenHit1.getNumber());
        accessionNomenHit1.setDefline("defline jam for NOMENHIT");
        accessionNomenHit1.setLength(12);
        ReferenceDatabase refDbNomenHit = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.UNIPROTKB,
                ForeignDBDataType.DataType.POLYPEPTIDE,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.Type.MOUSE);
        logger.debug("refdbNomenHit " + refDbNomenHit.getForeignDB().getDbName());
        accessionNomenHit1.setReferenceDatabase(refDbNomenHit);
        session.save(accessionNomenHit1);

        //create a NomenAccession
        Accession accessionNomenHit2 = new Accession();
        accessionNomenHit2.setNumber("AC:NOMENHIT2");
        logger.debug("accessionNomenHit2" + accessionNomenHit2.getNumber());
        accessionNomenHit2.setDefline("defline jam for NOMENHIT2");
        accessionNomenHit2.setLength(12);
        ReferenceDatabase refDbNomenHit2 = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.UNIPROTKB,
                ForeignDBDataType.DataType.POLYPEPTIDE,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.Type.HUMAN);
        logger.debug("refdbNomenHit2 " + refDbNomenHit2.getForeignDB().getDbName());
        accessionNomenHit2.setReferenceDatabase(refDbNomenHit2);
        session.save(accessionNomenHit2);

        //create a NomenAccessionHuman
        Accession accessionNomenRelHuman = new Accession();
        accessionNomenRelHuman.setNumber("AC:NOMENRELATEDh");
        logger.debug("accessionNomenRelated" + accessionNomenRelHuman.getNumber());
        accessionNomenRelHuman.setDefline("this is the related entrez id");
        accessionNomenRelHuman.setLength(12);
        ReferenceDatabase refDbNomenRelHuman = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.GENE,
                ForeignDBDataType.DataType.ORTHOLOG,
                ForeignDBDataType.SuperType.ORTHOLOG,
                Species.Type.HUMAN);
        logger.debug("refdbNomenRelHuman is: " + refDbNomenRelHuman.getForeignDB().getDbName());
        accessionNomenRelHuman.setReferenceDatabase(refDbNomenRelHuman);
        session.save(accessionNomenRelHuman);

        //create a NomenAccessionHuman
        Accession accessionNomenRelMouse = new Accession();
        accessionNomenRelMouse.setNumber("AC:NOMENRELATEDm");
        logger.debug("accessionNomenRelated" + accessionNomenRelMouse.getNumber());
        accessionNomenRelMouse.setDefline("this is the related entrez id for mouserel");
        accessionNomenRelMouse.setLength(12);
        ReferenceDatabase refDbNomenRelM = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.GENE,
                ForeignDBDataType.DataType.ORTHOLOG,
                ForeignDBDataType.SuperType.ORTHOLOG,
                Species.Type.MOUSE);
        logger.debug("refdbNomenRelMouse " + refDbNomenRelM.getForeignDB().getDbName());
        accessionNomenRelMouse.setReferenceDatabase(refDbNomenRelM);
        session.save(accessionNomenRelMouse);

        // create Run
        NomenclatureRun nomenRun = new NomenclatureRun();
        nomenRun.setNomenclaturePublication(publication);
        nomenRun.setOrthologyPublication(publicationO);
        nomenRun.setName("TestNomenRun");
//        nomenRun.setType(Run.Type.NOMENCLATURE);
        nomenRun.setProgram("BLASTP");
        nomenRun.setBlastDatabase("sptr_hssptr_mssptr_zf");
//        nomenRun.setType(Run.Type.NOMENCLATURE);
        Date date2 = new Date();
        logger.debug("date2: " + date2);
        nomenRun.setDate(date2);
        session.save(nomenRun);


        // create Candidate
        Candidate candidateNomen = new Candidate();
        candidateNomen.setRunCount(1);
        candidateNomen.setLastFinishedDate(new Date());
        //candidate.setIdentifiedMarker(cDNA);
        candidateNomen.setSuggestedName("renoRename");
//        candidateNomen.setIdentifiedMarker(markerRepository.getMarkerByAbbreviation("reno"));
        candidateNomen.setMarkerType(Marker.Type.GENE.toString());
        session.save(candidateNomen);

        // create RunCandidateNomen
        RunCandidate runCandidateNomen = new RunCandidate();
        runCandidateNomen.setRun(nomenRun);
        runCandidateNomen.setDone(false);
        runCandidateNomen.setLockPerson(personRepository.getPerson("ZDB-PERS-030520-1"));
        runCandidateNomen.setCandidate(candidateNomen);
        session.save(runCandidateNomen);

        // create a Nomenclature Query
        Query queryNomen = new Query();
        queryNomen.setRunCandidate(runCandidateNomen);
        queryNomen.setAccession(accessionNomenQuery);
        runCandidateNomen.getCandidateQueries().add(queryNomen);
        session.save(queryNomen);

        Hit hitNomenWithoutGene = new Hit();
        hitNomenWithoutGene.setQuery(queryNomen);
        hitNomenWithoutGene.setHitNumber(7);
        hitNomenWithoutGene.setTargetAccession(accessionNomenHit2);
        hitNomenWithoutGene.setExpectValue(1.8e-6);
        hitNomenWithoutGene.setScore(100);
        hitNomenWithoutGene.setPositivesNumerator(7);
        hitNomenWithoutGene.setPositivesDenominator(14);
        queryNomen.getBlastHits().add(hitNomenWithoutGene);
        session.save(hitNomenWithoutGene);
        logger.debug("apparently saved it 7" + hitNomenWithoutGene.getHitNumber());

        ReferenceDatabase refDBHuman = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.OMIM,
                ForeignDBDataType.DataType.ORTHOLOG,
                ForeignDBDataType.SuperType.ORTHOLOG,
                Species.Type.HUMAN);
        //copied the acc_num out of the database; maybe this will change??
        Accession humanAccession = sequenceRepository.getAccessionByAlternateKey("100650", refDBHuman);
        logger.debug("humanAccession: " + humanAccession.getNumber());

        logger.debug("runCan got created: " + runCandidateNomen.getZdbID());
        return runCandidateNomen.getZdbID();
    }


}
