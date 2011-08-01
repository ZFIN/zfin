package org.zfin.sequence.reno;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.Species;
import org.zfin.people.repository.ProfileRepository;
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

    private final Logger logger = Logger.getLogger(RenoTestData.class);
    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private ProfileRepository personRepository = RepositoryFactory.getProfileRepository();
    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();


    public void setUpSharedRedundancyAndNomenclatureData()  {
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

    /**
     * Returns run candidate ZDB ID for Redundancy.
     * @return zdb ID
     */
    public String createRedundancyData(){
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
        candidate.setSuggestedName("zgc:test");
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
                Species.ZEBRAFISH);

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

        /*       Hit hit2 = new Hit();
                hit2.setQuery(query);
                hit2.setHitNumber(2);
                hit2.setTargetAccession(accession2);
                query.getBlastHits().add(hit2);
        //        hit2.setZfinAccession(markerRepository.getMarkerByAbbreviation("reno"));
                hit2.setExpectValue(1.3e-56);
                hit2.setScore(800);
                hit2.setPositivesNumerator(2);
                hit2.setPositivesDenominator(4);
                session.save(hit2);
        */
        return runCandidate.getZdbID();

    }

    /**
     * Returns run candidate ZDB ID for Nomenclature.
     * @return zdb ID
     */
    public String createNomenclatureData(){
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
                Species.ZEBRAFISH);
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
                Species.MOUSE);
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
                Species.HUMAN);
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
                ForeignDBDataType.DataType.ORTHOLOGUE,
                ForeignDBDataType.SuperType.ORTHOLOGUE,
                Species.HUMAN);
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
                ForeignDBDataType.DataType.ORTHOLOGUE,
                ForeignDBDataType.SuperType.ORTHOLOGUE,
                Species.MOUSE);
        logger.debug("refdbNomenRelMouse " + refDbNomenRelM.getForeignDB().getDbName());
        accessionNomenRelMouse.setReferenceDatabase(refDbNomenRelM);
        session.save(accessionNomenRelMouse);

        //create an accessionRelationship for Human

//        AccessionRelationship arelNomenHuman = new AccessionRelationship();
//        arelNomenHuman.setAccession(accessionNomenHit2);
//        arelNomenHuman.setRelatedAccession(accessionNomenRelHuman);
//        AccessionRelationshipType areltype = new AccessionRelationshipType();
//        areltype.setAccessionRelationshipType("Human Protein hit to Entrez Ac");
//        arelNomenHuman.setRelationshipType(areltype);
//        session.save(arelNomenHuman);
/*
        logger.debug("the related human accession number: "+accessionNomenRelHuman.getID()+" "+accessionNomenRelHuman.getNumber());
        logger.debug("the related accession is not null :" +accessionNomenRelHuman);
        logger.debug("the related accession zdbID is not null :" +arelNomenHuman.getZdbID());
        logger.debug("the relatedAccession is null: " +accessionNomenHit2.getRelatedAccessions());

        accessionNomenHit2.getRelatedAccessions().add(accessionNomenRelHuman);*/

        /*
            //create an accessionRelationship for Mouse

            AccessionRelationship arelNomenMouse = new AccessionRelationship();
            arelNomenMouse.setAccession(accessionNomenHit1);
            arelNomenMouse.setRelatedAccession(accessionNomenRelMouse);
            AccessionRelationshipType areltypeMouse = new AccessionRelationshipType();
            areltypeMouse.setAccessionRelationshipType("Mouse Protein hit to Entrez Ac");
            arelNomenMouse.setRelationshipType(areltypeMouse);
            session.save(arelNomenMouse);
        */


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

        /*       Hit hitNomenWithGene = new Hit();
                hitNomenWithGene.setQuery(queryNomen);
                hitNomenWithGene.setHitNumber(6);
                hitNomenWithGene.setTargetAccession(accessionNomenHit1);
                Marker geneToAdd = markerRepository.getMarkerByAbbreviation("reno") ;
                logger.debug(geneToAdd.getZdbID());
                hitNomenWithGene.setZfinAccession(geneToAdd);
                hitNomenWithGene.setExpectValue(1.8e-6);
                hitNomenWithGene.setScore(100);
                hitNomenWithGene.setPositivesNumerator(6);
                hitNomenWithGene.setPositivesDenominator(12);
                queryNomen.getBlastHits().add(hitNomenWithGene);
                session.save(hitNomenWithGene);
                logger.debug("apparently saved it" + hitNomenWithGene.getHitNumber());
        */
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
                ForeignDBDataType.DataType.ORTHOLOGUE,
                ForeignDBDataType.SuperType.ORTHOLOGUE,
                Species.HUMAN);
        //copied the acc_num out of the database; maybe this will change??
        Accession humanAccession = sequenceRepository.getAccessionByAlternateKey("100650", refDBHuman);
        logger.debug("humanAccession: " + humanAccession.getNumber());

        /*       Hit hitHuman = new Hit();
                hitHuman.setQuery(queryNomen);
                hitHuman.setHitNumber(9);
                hitHuman.setTargetAccession(humanAccession);
                hitHuman.setExpectValue(1.8e-6);
                hitHuman.setScore(100);
                hitHuman.setPositivesNumerator(1);
                hitHuman.setPositivesDenominator(1);
                queryNomen.getBlastHits().add(hitHuman);
                session.save(hitHuman);

                //Accession relatedAccession = sequenceRepository.getAccessionByAlternateKey();


                ForeignDB foreignDBMouse = sequenceRepository.getForeignDBByName("MGI");
                ReferenceDatabase refDBMouse = sequenceRepository.getReferenceDatabase(
                        foreignDBMouse,
                        ForeignDBDataType.DataType.ORTHOLOGUE,
                        ForeignDBDataType.SuperType.ORTHOLOGUE,
                        Species.MOUSE);
                //copied the acc_num out of the database; maybe this will change??
                Accession mouseAccession = sequenceRepository.getAccessionByAlternateKey("100002",refDBMouse);
                logger.debug("mouseAccession: " +mouseAccession);

                Hit hitMouse = new Hit();
                hitMouse.setQuery(queryNomen);
                hitMouse.setHitNumber(10);
                hitMouse.setTargetAccession(mouseAccession);
                hitMouse.setExpectValue(1.8e-6);
                hitMouse.setScore(100);
                hitMouse.setPositivesNumerator(1);
                hitMouse.setPositivesDenominator(1);
                queryNomen.getBlastHits().add(hitMouse);
                session.save(hitMouse);
        */
        logger.debug("runCan got created: "+runCandidateNomen.getZdbID());
        return runCandidateNomen.getZdbID();
    }


}
