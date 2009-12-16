package org.zfin.marker.presentation.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.client.TermNotFoundException;
import org.zfin.infrastructure.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.*;
import org.zfin.marker.presentation.client.BlastDatabaseAccessException;
import org.zfin.marker.presentation.client.TranscriptRPCService;
import org.zfin.marker.presentation.client.TranscriptTypeStatusMismatchException;
import org.zfin.marker.presentation.dto.*;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;
import org.zfin.repository.SessionCreator;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.BlastDatabaseException;
import org.zfin.sequence.blast.MountedWublastBlastService;

import java.util.*;


/**
 */
public class TranscriptRPCServiceImpl extends RemoteServiceServlet implements TranscriptRPCService {

    static {
        SessionCreator.instantiateDBForHostedMode();
    }


    private transient MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private transient InfrastructureRepository infrastructureRepository
            = RepositoryFactory.getInfrastructureRepository();
    private transient Logger logger = Logger.getLogger(TranscriptRPCServiceImpl.class);

    private transient List<ReferenceDatabaseDTO> transcriptEditAddableNucleotideSequenceReferenceDatabases;
    private transient List<ReferenceDatabaseDTO> transcriptEditAddableProteinReferenceDatabases;
    private transient List<ReferenceDatabaseDTO> transcriptEditDBLinkReferenceDatabases;
    private transient List<ReferenceDatabaseDTO> geneEditAddableProteinReferenceDatabases;
    private transient List<ReferenceDatabaseDTO> geneEditAddableNucleotideReferenceDatabases;


    public TranscriptDTO changeTranscriptHeaders(TranscriptDTO transcriptDTO) throws TranscriptTypeStatusMismatchException {

        SessionCreator.instantiateDBForHostedMode();

        Transcript transcript = markerRepository.getTranscriptByZdbID(transcriptDTO.getZdbID());
        logger.info("got transcript: " + transcript.getZdbID());

        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        try {

            if (!transcript.getName().equals(transcriptDTO.getName())) {
                String oldName = transcript.getName();
                transcript.setName(transcriptDTO.getName());
                transcript.setAbbreviation(transcriptDTO.getName().toLowerCase());
                InfrastructureService.insertUpdate(transcript, "Name", oldName, transcript.getName());
                //run regen script
                markerRepository.runMarkerNameFastSearchUpdate(transcript);
            }


            TranscriptType newType = RepositoryFactory.getMarkerRepository().getTranscriptTypeForName(transcriptDTO.getTranscriptType());
            if (!transcript.getTranscriptType().equals(newType)) {
                TranscriptType oldType = transcript.getTranscriptType();
                transcript.setTranscriptType(newType);
                InfrastructureService.insertUpdate(transcript, "Transcript Type", oldType.getDisplay(), newType.getDisplay());
            }


            TranscriptStatus oldStatus = transcript.getStatus();
            TranscriptStatus newStatus;
            if (transcriptDTO.getTranscriptStatus() == null
                    || transcriptDTO.getTranscriptStatus().equals("null")
                    || transcriptDTO.getTranscriptStatus().equals("")) {
                newStatus = null;
            } else {
                newStatus = RepositoryFactory.getMarkerRepository().getTranscriptStatusForName(transcriptDTO.getTranscriptStatus());
            }

            if (false == TranscriptStatus.equals(newStatus, oldStatus)) {

                List<TranscriptStatus.Status> statuses = TranscriptType.Type.getStatusList(newType.getType());
                if (newStatus != null && false == statuses.contains(newStatus.getStatus())) {
                    List<String> statusStrings = new ArrayList<String>();
                    for (TranscriptStatus.Status status : statuses) {
                        statusStrings.add(status.toString());
                    }

                    throw new TranscriptTypeStatusMismatchException(statusStrings);
                }


                transcript.setStatus(newStatus);

                String oldStatusString = (oldStatus == null) ? "null" : oldStatus.getDisplay();
                String newStatusString = (newStatus == null) ? "null" : newStatus.getDisplay();

                InfrastructureService.insertUpdate(transcript, "Transcript Status", oldStatusString, newStatusString);
            }


            session.update(transcript);
            session.flush();
            session.refresh(transcript);


            //sanity check
            transcriptDTO.setName(transcript.getName());
            transcriptDTO.setTranscriptType(transcript.getTranscriptType().getType().toString());
            if (transcript.getStatus() != null) {
                transcriptDTO.setTranscriptStatus(transcript.getStatus().getStatus().toString());
            }

            logger.info("updated transcript: " + transcript);

            transaction.commit();
        }
        catch (TranscriptTypeStatusMismatchException t) {
            transaction.rollback();
            logger.error("Failed to update transcript: " + t);
            throw new TranscriptTypeStatusMismatchException(t);
        }
        catch (Exception e) {
            transaction.rollback();
            logger.error("Failed to update transcript: " + e);
        }

        return transcriptDTO;
    }

    public TranscriptDTO getTranscriptForZdbID(String zdbID) throws BlastDatabaseAccessException {

        SessionCreator.instantiateDBForHostedMode();

        Transcript transcript = markerRepository.getTranscriptByZdbID(zdbID);
        TranscriptDTO transcriptDTO = new TranscriptDTO();

        // get simple attributes
        transcriptDTO.setZdbID(transcript.getZdbID());
        transcriptDTO.setName(transcript.getName());
        transcriptDTO.setAbbreviationOrder(transcript.getAbbreviationOrder());
        transcriptDTO.setMarkerType(transcript.getMarkerType().getType().name());
        transcriptDTO.setTranscriptType(transcript.getTranscriptType().getType().toString());

        // set internal RNA sequences
        ArrayList<SequenceDTO> rnaInternalSequenceDTOs = new ArrayList<SequenceDTO>();
        List<Sequence> rnaSequences = null;
        try {
            rnaSequences = MountedWublastBlastService.getInstance().getSequencesForTranscript(transcript, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
        } catch (BlastDatabaseException e) {
            throw new BlastDatabaseAccessException("Failed to retrieve RNA sequences", e);
        }
        for (Sequence sequence : rnaSequences) {
            rnaInternalSequenceDTOs.addAll(DTOHelper.createSequenceDTOsForPublications(sequence, transcript.getName()));
        }
        transcriptDTO.setRnaSequences(rnaInternalSequenceDTOs);


        if (transcript.getStatus() != null) {
            transcriptDTO.setTranscriptStatus(transcript.getStatus().getStatus().toString());
        }


        // get direct attributions
        ActiveData activeData = new ActiveData();
        activeData.setZdbID(zdbID);
        List<RecordAttribution> recordAttributions = RepositoryFactory.getInfrastructureRepository().getRecordAttributions(activeData);
        ArrayList<String> attributions = new ArrayList<String>();
        for (RecordAttribution recordAttribution : recordAttributions) {
            attributions.add(recordAttribution.getSourceZdbID());
        }
        transcriptDTO.setRecordAttributions(attributions);

        // get notes
        ArrayList<String> curatorNotes = new ArrayList<String>();
        Set<DataNote> dataNotes = transcript.getDataNotes();
        for (DataNote dataNote : dataNotes) {
            curatorNotes.add(dataNote.getNote());
        }
        transcriptDTO.setCuratorNotes(curatorNotes);

        ArrayList<String> publicNotes = new ArrayList();
        publicNotes.add(transcript.getPublicComments());
        transcriptDTO.setPublicNotes(publicNotes);

        // get alias's
        Set<MarkerAlias> aliases = transcript.getAliases();
        ArrayList<RelatedEntityDTO> aliasRelatedEntities = new ArrayList<RelatedEntityDTO>();
        if (aliases != null) {
            for (MarkerAlias alias : aliases) {
                Set<PublicationAttribution> publicationAttributions = alias.getPublications();
                aliasRelatedEntities.addAll(DTOHelper.createAttributesForPublication(transcript.getZdbID(), alias.getAlias(), publicationAttributions));
            }
        }
        transcriptDTO.setAliasAttributes(aliasRelatedEntities);

        // get related genes
        // get related clones
        Set<MarkerRelationship> markerRelationships = transcript.getSecondMarkerRelationships();
        logger.debug("# of marker relationships: " + markerRelationships.size());
        ArrayList<MarkerDTO> relatedGenes = new ArrayList<MarkerDTO>();
        ArrayList<MarkerDTO> targetedGenes = new ArrayList<MarkerDTO>();
        ArrayList<MarkerDTO> relatedClones = new ArrayList<MarkerDTO>();
        for (MarkerRelationship markerRelationship : markerRelationships) {
            if (
                    markerRelationship.getFirstMarker().isInTypeGroup(Marker.TypeGroup.GENEDOM)
                            &&
                            markerRelationship.getType().equals(MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT)
                    ) {
                Marker gene = markerRelationship.getFirstMarker();
                logger.info("genes found: " + gene.getAbbreviation());
//                relatedGenes.addAll(DTOHelper.createAttributesForPublication(gene.getAbbreviation(),markerRelationship.getPublications())) ;
                relatedGenes.addAll(DTOHelper.createLinksForPublication(DTOHelper.createMarkerDTOFromMarker(gene), markerRelationship.getPublications()));
            } else if (
                    markerRelationship.getFirstMarker().isInTypeGroup(Marker.TypeGroup.CLONE)
                            &&
                            markerRelationship.getType().equals(MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT)
                    ) {
                Marker clone = markerRelationship.getFirstMarker();
//                relatedClones.addAll(DTOHelper.createAttributesForPublication(clone.getAbbreviation(),markerRelationship.getPublications())) ;
                relatedClones.addAll(DTOHelper.createLinksForPublication(DTOHelper.createMarkerDTOFromMarker(clone), markerRelationship.getPublications()));
            }
        }
        for (MarkerRelationship mrel : transcript.getFirstMarkerRelationships()) {
            if (mrel.getType().equals(MarkerRelationship.Type.TRANSCRIPT_TARGETS_GENE)) {
                Marker gene = mrel.getSecondMarker();
                targetedGenes.addAll(DTOHelper.createLinksForPublication(DTOHelper.createMarkerDTOFromMarker(gene), mrel.getPublications()));
            }

        }

        logger.debug("# of related genes: " + relatedGenes.size());
        logger.debug("# of targeted genes: " + targetedGenes.size());
        logger.debug("# of related clones: " + relatedClones.size());

        Collections.sort(relatedGenes);
        transcriptDTO.setRelatedGeneAttributes(relatedGenes);
        Collections.sort(targetedGenes);
        transcriptDTO.setTargetedGeneAttributes(targetedGenes);
        Collections.sort(relatedClones);
        transcriptDTO.setRelatedCloneAttributes(relatedClones);


        // get related proteins
        // get proteins from VEGA
        ArrayList<RelatedEntityDTO> relatedProteins = new ArrayList<RelatedEntityDTO>();


        List<ReferenceDatabase> referenceDatabases = RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(
                DisplayGroup.GroupName.DISPLAYED_PROTEIN_SEQUENCE);


        for (TranscriptDBLink transcriptDBLink : RepositoryFactory.getSequenceRepository().getTranscriptDBLinksForTranscript(transcript,
                referenceDatabases.toArray(new ReferenceDatabase[referenceDatabases.size()]))) {
//            for(TranscriptDBLink transcriptDBLink : RepositoryFactory.getSequenceRepository().getTranscriptDBLinksForTranscript(transcript, TranscriptService.getPolypeptideProductsReferenceDatabases())){
            List<DBLinkDTO> dbLinkDTOs = DTOHelper.createDBLinkDTOsFromDBLink(transcriptDBLink, transcript.getName(), transcript.getZdbID());
            for (DBLinkDTO dbLinkDTO : dbLinkDTOs) {
                relatedProteins.addAll(DTOHelper.<DBLinkDTO>createLinksForPublication(dbLinkDTO, transcriptDBLink.getPublications()));
            }
//                    relatedProteins.addAll(DTOHelper.createAttributesForPublication(transcriptDBLink.getAccessionNumber(), transcriptDBLink.getPublications())) ;
        }
        transcriptDTO.setRelatedProteinAttributes(relatedProteins);

//        logger.debug("# of markerDBLinks: "+ markerDBLinks.size());
        logger.debug("# of related proteins: " + relatedProteins.size());

        // get supporting sequences
        List<DBLink> dbLinks = TranscriptService.getSupportingDBLinks(transcript);
        ArrayList<DBLinkDTO> dbLinkDTOList = new ArrayList<DBLinkDTO>();
        dbLinkDTOList.addAll(DTOHelper.createDBLinkDTOsFromDBLink(dbLinks, transcript.getZdbID(), transcript.getAbbreviation()));

        transcriptDTO.setSupportingSequenceLinks(dbLinkDTOList);

        return transcriptDTO;
    }

    public String getTranscriptTypeForZdbID(String zdbID) {
        SessionCreator.instantiateDBForHostedMode();

        Transcript transcript = markerRepository.getTranscriptByZdbID(zdbID);

        return transcript.getTranscriptType().getType().toString();
    }

    // sequence method

    public SequenceDTO getProteinSequenceForAccessionAndRefDB(String accession, String refDBName)
            throws BlastDatabaseAccessException {

        SessionCreator.instantiateDBForHostedMode();

        ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                ForeignDB.AvailableName.getType(refDBName), ForeignDBDataType.DataType.POLYPEPTIDE,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.ZEBRAFISH
        );

        List<Sequence> sequences = null;
        try {
            sequences = MountedWublastBlastService.getInstance().getSequencesForAccessionAndReferenceDBs(accession, referenceDatabase);
        } catch (BlastDatabaseException e) {
            logger.fatal("Failed to retrive protein sequences", e);
            throw new BlastDatabaseAccessException("Failed to retrive protein sequences", e);
        }

        List<TranscriptDBLink> transcriptDBLinks = RepositoryFactory.getSequenceRepository().getTranscriptDBLinksForAccession(accession, referenceDatabase);
        TranscriptDBLink transcriptDBLink;
        // we only care about the first one
        if (transcriptDBLinks.size() > 0) {
            transcriptDBLink = transcriptDBLinks.get(0);
        } else {
            logger.error("Could not find transcriptDBLINK for [" + accession + "] refDB[" + refDBName + "]");
            transcriptDBLink = null;
        }

        if (sequences.size() == 1) {
            Sequence sequence = sequences.get(0);
            SequenceDTO sequenceDTO = new SequenceDTO();
            sequenceDTO.setSequence(sequence.getData());
            sequenceDTO.setDefLine(sequence.getDefLine().toString());
            if (transcriptDBLink != null) {
                sequenceDTO.setDataZdbID(transcriptDBLink.getZdbID());
            }
            return sequenceDTO;
        } else {
            logger.error("Found wrong # of sequences[" + accession + "] refDB[" + refDBName + "]: " + sequences.size());
            return null;
        }
    }


    /**
     * Adds a protein dblink for any transcript as well as associates any proteins with the producing genes.
     *
     * @param transcriptZdbID        Transcript to affect.
     * @param sequenceData           Sequence data.
     * @param pubZdbID               Publicaiton to use for attribution.
     * @param referenceDatabaseZdbID Indicates foreignDB / blast Database to send to.
     * @return Returns DBLinkDTO created by adding sequence.
     */
    public DBLinkDTO addProteinSequence(String transcriptZdbID, String sequenceData, String pubZdbID, String referenceDatabaseZdbID) throws BlastDatabaseAccessException {

        SessionCreator.instantiateDBForHostedMode();

        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();

        try {
            Sequence sequence = MountedWublastBlastService.getInstance().addSequenceToTranscript(transcriptZdbID, sequenceData, referenceDatabaseZdbID);
            TranscriptDBLink transcriptDBLink = (TranscriptDBLink) sequence.getDbLink();
            ReferenceDatabase referenceDatabase = transcriptDBLink.getReferenceDatabase();
            String generatedAccessionNumber = transcriptDBLink.getAccessionNumber();
            transcriptDBLink.setAccessionNumberDisplay(generatedAccessionNumber);
            Transcript transcript = transcriptDBLink.getTranscript();

            // add genes related to transcripts
            Set<MarkerRelationship> markerRelationships = transcript.getSecondMarkerRelationships();
            Set<MarkerDBLink> relatedGenes = new HashSet<MarkerDBLink>();

            for (MarkerRelationship markerRelationship : markerRelationships) {
                if (markerRelationship.getFirstMarker().isInTypeGroup(Marker.TypeGroup.GENEDOM)
                        &&
                        markerRelationship.getType().equals(MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT)) {

                    MarkerDBLink markerDBLink = new MarkerDBLink();
                    markerDBLink.setLength(sequenceData.length());
                    markerDBLink.setMarker(markerRelationship.getFirstMarker());

                    markerDBLink.setReferenceDatabase(referenceDatabase);
                    markerDBLink.setAccessionNumber(generatedAccessionNumber);
                    markerDBLink.setAccessionNumberDisplay(generatedAccessionNumber);
                    relatedGenes.add(markerDBLink);
                    session.save(markerDBLink);
                }
            }


            // flush to get dblink zdbIDs
            session.flush();

            // write attribution out (just one!)
            if (true == StringUtils.isNotEmpty(pubZdbID)) {
                RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(transcriptDBLink.getZdbID(), pubZdbID);

                // attribute all of the genes, the same
                for (MarkerDBLink markerDBLink : relatedGenes) {
                    RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(markerDBLink.getZdbID(), pubZdbID);
                }
            }


            List<DBLinkDTO> dbLinkDTOs = DTOHelper.createDBLinkDTOsFromTranscriptDBLink(transcriptDBLink);
            DBLinkDTO dbLinkDTO = dbLinkDTOs.get(0);
            dbLinkDTO.setPublicationZdbID(pubZdbID);

            // if we can't generate the UI code then revert
            transaction.commit();


            HibernateUtil.closeSession();
            return dbLinkDTO;
        }
        catch (Exception e) {
            logger.error("Failure to add internal protein sequence", e);
            transaction.rollback();
            HibernateUtil.closeSession();
            throw new BlastDatabaseAccessException("Failed to add internal protein sequence", e);
        }
    }

    public List<DBLinkDTO> getTranscriptDBLinksForAccessionAndRefDB(String accession, String refDB) {

        SessionCreator.instantiateDBForHostedMode();

        List<TranscriptDBLink> transcriptDBLinks = TranscriptService.getProteinTranscriptDBLinksForAccessionForRefDBName(accession, refDB);
        List returnList = new ArrayList();
        for (TranscriptDBLink transcriptDBLink : transcriptDBLinks) {
            returnList.add(DTOHelper.createDBLinkDTOsFromTranscriptDBLink(transcriptDBLink));
        }
        return returnList;
    }

    // todo: get types for the supergroup transcript

    public List<String> getTranscriptTypes() {

        SessionCreator.instantiateDBForHostedMode();

        TranscriptType.Type[] transcriptTypes = TranscriptType.Type.values();
        Set<String> types = new TreeSet<String>();
        for (TranscriptType.Type transcriptType : transcriptTypes) {
            types.add(transcriptType.toString());
        }

        List<String> typeList = new ArrayList<String>();
        for (String status : types) {
            typeList.add(status);
        }

        return typeList;
    }

    public List<String> getTranscriptStatuses() {


        TranscriptStatus.Status[] statuses = TranscriptStatus.Status.values();
        List<String> statusList = new ArrayList<String>();
        for (TranscriptStatus.Status status : statuses) {
            if (status != TranscriptStatus.Status.NONE) {
                statusList.add(status.toString());
            }
        }

        return statusList;
    }


    public SequenceDTO addNucleotideSequenceToTranscript(TranscriptDTO transcriptDTO,
                                                         SequenceDTO sequenceDTO,
                                                         ReferenceDatabaseDTO referenceDatabaseDTO) throws BlastDatabaseAccessException {

        SessionCreator.instantiateDBForHostedMode();

        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();

        try {
            Sequence sequence = MountedWublastBlastService.getInstance().addSequenceToTranscript(transcriptDTO.getZdbID(), sequenceDTO.getSequence(), referenceDatabaseDTO.getZdbID());
            DBLink dbLink = sequence.getDbLink();
            sequenceDTO.setDataName(transcriptDTO.getName());
            sequenceDTO.setDefLine(sequence.getDefLine().toString());
            sequenceDTO.setName(dbLink.getAccessionNumber());
            sequenceDTO.setDataZdbID(dbLink.getDataZdbID());
            sequenceDTO.setDbLinkZdbID(dbLink.getZdbID());
            Transcript transcript = markerRepository.getTranscriptByZdbID(transcriptDTO.getZdbID());

            if (StringUtils.isNotEmpty(sequenceDTO.getPublicationZdbID().trim())) {
                List<PublicationAttribution> publicationAttributions = infrastructureRepository.getPublicationAttributions(sequenceDTO.getDbLinkZdbID());
                if (publicationAttributions.size() > 0) {
                    RepositoryFactory.getInfrastructureRepository().insertPublicAttribution(sequence.getDbLink().getZdbID(), sequenceDTO.getPublicationZdbID());
                    sequenceDTO.setAttributionType(RecordAttribution.SourceType.STANDARD.toString());
                } else {
                    RepositoryFactory.getInfrastructureRepository().insertPublicAttribution(sequence.getDbLink().getZdbID(), sequenceDTO.getPublicationZdbID(), RecordAttribution.SourceType.FIRST_CURATED_SEQUENCE_PUB);
                    sequenceDTO.setAttributionType(RecordAttribution.SourceType.FIRST_CURATED_SEQUENCE_PUB.toString());
                }
            }

            String updateComment = "Added Nucleotide Sequence: " + sequence.getDbLink().getAccessionNumber();

            InfrastructureService.insertUpdate(transcript, updateComment);

            markerRepository.runMarkerNameFastSearchUpdate(transcript);

            session.flush();
            transaction.commit();

            HibernateUtil.closeSession();

//            TranscriptDTO returnTranscriptDTO = getTranscriptForZdbID(transcriptDTO.getZdbID());
            return sequenceDTO;
        }
        catch (Exception e) {
            logger.error(e);
            transaction.rollback();
            throw new BlastDatabaseAccessException("Failed to Add Blast Sequence", e);
        }
    }

    /**
     * @param relatedEntityDTO DBLinkDTO
     * @return Attribution returned from adding protein accession.
     * @throws TermNotFoundException
     */
    public DBLinkDTO addProteinRelatedEntity(RelatedEntityDTO relatedEntityDTO) throws TermNotFoundException {

        SessionCreator.instantiateDBForHostedMode();

        Transcript transcript = markerRepository.getTranscriptByZdbID(relatedEntityDTO.getDataZdbID());

        List<ReferenceDatabase> referenceDatabases = RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(DisplayGroup.GroupName.TRANSCRIPT_EDIT_ADDABLE_PROTEIN_SEQUENCE);

        // todo:
        // 1. find accession in UNPUBLISHED PROTEIN or internal ZPROTA
        // getting from here in order to get the proper reference database and verify the link
        Accession accession = RepositoryFactory.getSequenceRepository().getAccessionByAlternateKey(relatedEntityDTO.getName(), referenceDatabases.toArray(new ReferenceDatabase[referenceDatabases.size()]));

        HibernateUtil.createTransaction();
        // 2. add dblink with the ReferenceDatabase we found it with or throw a TermNotFoundException
        if (accession == null) {
            List<TranscriptDBLink> dbLinkList = RepositoryFactory.getSequenceRepository().getTranscriptDBLinksForAccession(relatedEntityDTO.getName(), referenceDatabases.toArray(
                    new ReferenceDatabase[referenceDatabases.size()]));
            if (dbLinkList.size() >= 1) {
                TranscriptDBLink transcriptDBLink = dbLinkList.get(0);
                DBLink dbLink = markerRepository.addDBLink(transcript, transcriptDBLink.getAccessionNumber(), transcriptDBLink.getReferenceDatabase(), relatedEntityDTO.getPublicationZdbID());
                HibernateUtil.flushAndCommitCurrentSession();
                logger.info("accession is found in: " + transcriptDBLink.getReferenceDatabase());
                return DTOHelper.createDBLinkDTOFromDBLinkForPub(dbLink, transcript.getZdbID(), transcript.getAbbreviation());
            } else {
                logger.info("accession is null");
                throw new TermNotFoundException(relatedEntityDTO.getName(), "accession/dblink");
            }
        } else {
            DBLink dbLink = markerRepository.addDBLink(transcript, accession.getNumber(), accession.getReferenceDatabase(), relatedEntityDTO.getPublicationZdbID());
            HibernateUtil.flushAndCommitCurrentSession();
            logger.info("accession is found in: " + dbLink.getReferenceDatabase());
            return DTOHelper.createDBLinkDTOFromDBLinkForPub(dbLink, transcript.getZdbID(), transcript.getAbbreviation(), relatedEntityDTO.getPublicationZdbID());
        }
    }

    public DBLinkDTO addProteinAttribution(DBLinkDTO dbLinkDTO) {
        Transcript transcript = markerRepository.getTranscriptByZdbID(dbLinkDTO.getDataZdbID());

        HibernateUtil.createTransaction();

        List<TranscriptDBLink> transcriptDBLinks = RepositoryFactory.getSequenceRepository().getTranscriptDBLinksForAccession(dbLinkDTO.getName(), transcript);
        if (transcriptDBLinks.size() == 1) {
            RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(transcriptDBLinks.get(0).getZdbID(), dbLinkDTO.getDbLinkZdbID());
            HibernateUtil.flushAndCommitCurrentSession();
            return DTOHelper.createDBLinkDTOFromDBLinkForPub(transcriptDBLinks.get(0), transcript.getZdbID(), transcript.getAbbreviation(), dbLinkDTO.getPublicationZdbID());
        } else {
            logger.error("found wrong # of transcripts for accession [" + dbLinkDTO.getName() + "] and transcript [" + transcript + "]: " + transcriptDBLinks.size());
            HibernateUtil.currentSession().getTransaction().rollback();
            return null;
        }
    }

    public DBLinkDTO removeProteinRelatedEntity(DBLinkDTO dbLinkDTO) {
        Transcript transcript = markerRepository.getTranscriptByZdbID(dbLinkDTO.getDataZdbID());

        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();

        List<TranscriptDBLink> transcriptDBLinks = RepositoryFactory.getSequenceRepository().getTranscriptDBLinksForAccession(dbLinkDTO.getName(), transcript);
        if (transcriptDBLinks.size() == 1) {
            TranscriptDBLink transcriptDBLink = transcriptDBLinks.get(0);
            RepositoryFactory.getInfrastructureRepository().deleteRecordAttributionsForData(transcriptDBLink.getZdbID());
            session.delete(transcriptDBLink);

            session.flush();
            transaction.commit();
        } else {
            logger.error("found wrong # of transcripts for accession [" + dbLinkDTO.getName() + "] and transcript [" + transcript + "]: " + transcriptDBLinks.size());

            transaction.rollback();
        }
        return dbLinkDTO;
    }


    public DBLinkDTO removeProteinAttribution(DBLinkDTO dbLinkDTO) {
        if (dbLinkDTO.getPublicationZdbID() == null || dbLinkDTO.getPublicationZdbID().length() == 0)
            throw new RuntimeException("No attribution found to remove");

        Transcript transcript = markerRepository.getTranscriptByZdbID(dbLinkDTO.getDataZdbID());

        List<TranscriptDBLink> dbLinkList = RepositoryFactory.getSequenceRepository().getTranscriptDBLinksForAccession(dbLinkDTO.getName(), transcript);


        //todo: Need to set to specific proeitn database.  MarkerDBLInk will be a proteinSequence
        // todo: referenceDatabase should be interal ZPROT
        if (dbLinkList.size() == 1) {
//            if(dbLinkList.size()==1){
            TranscriptDBLink transcriptDBLink = dbLinkList.get(0);
            //now deal with attribution
//            if(pubZdbID!=null && pubZdbID.length()>0 && transcript.equals(transcriptDBLink.getTranscript())){
            HibernateUtil.createTransaction();
            int deletedRecord = RepositoryFactory.getInfrastructureRepository().deleteRecordAttribution(transcriptDBLink.getZdbID(), dbLinkDTO.getPublicationZdbID());
            logger.info("deleted record attrs: " + deletedRecord);
            HibernateUtil.flushAndCommitCurrentSession();
        } else {
            logger.error("found wrong # of transcripts for accession [" + dbLinkDTO.getName() + "] and transcript [" + transcript + "]: " + dbLinkList.size());
        }
        return dbLinkDTO;
    }


    public List<ReferenceDatabaseDTO> getTranscriptSupportingSequencesReferenceDatabases() {
        if (transcriptEditDBLinkReferenceDatabases == null) {
            transcriptEditDBLinkReferenceDatabases =
                    DTOHelper.convertReferenceDTOs(RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(
                            DisplayGroup.GroupName.DBLINK_ADDING_ON_TRANSCRIPT_EDIT));
//            zebrafishSequenceDatabases =  DTOHelper.convertReferenceDTOs(TranscriptService.getSequenceReferenceDatabases()) ;
        }
        return transcriptEditDBLinkReferenceDatabases;
    }

    public List<ReferenceDatabaseDTO> getTranscriptAddableNucleotideSequenceReferenceDatabases(TranscriptDTO transcriptDTO) {
//        if(internalNucleotideSequenceReferenceDatabases == null){
        if (transcriptDTO.getTranscriptType().equals(TranscriptType.Type.MIRNA.toString())) {
            // todo: this should use curatedMatureMiRNA or something like that (see 3564)
            transcriptEditAddableNucleotideSequenceReferenceDatabases =
                    DTOHelper.convertReferenceDTOs(RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(
                            DisplayGroup.GroupName.TRANSCRIPT_EDIT_ADDABLE_MIRNA_NUCLEOTIDE_SEQUENCE));
        } else {
            transcriptEditAddableNucleotideSequenceReferenceDatabases =
                    DTOHelper.convertReferenceDTOs(RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(
                            DisplayGroup.GroupName.TRANSCRIPT_EDIT_ADDABLE_NUCLEOTIDE_SEQUENCE));
        }
//        }
        return transcriptEditAddableNucleotideSequenceReferenceDatabases;
    }

    // todo: should use a DisplayGruop

    public List<ReferenceDatabaseDTO> getGeneEditAddableStemLoopNucleotideSequenceReferenceDatabases() {
        if (geneEditAddableNucleotideReferenceDatabases == null) {
            geneEditAddableNucleotideReferenceDatabases = new ArrayList<ReferenceDatabaseDTO>();
            List<ReferenceDatabase> refdbs;
            refdbs = RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(DisplayGroup.GroupName.GENE_EDIT_ADDABLE_NUCLEOTIDE_SEQUENCE);

            for (ReferenceDatabase refdb : refdbs) {
                geneEditAddableNucleotideReferenceDatabases.add(DTOHelper.convertReferenceDTO(refdb));
            }

        }

        return geneEditAddableNucleotideReferenceDatabases;
    }

    public List<ReferenceDatabaseDTO> getTranscriptEditAddProteinSequenceReferenceDatabases() {
        if (transcriptEditAddableProteinReferenceDatabases == null) {
            transcriptEditAddableProteinReferenceDatabases =
                    DTOHelper.convertReferenceDTOs(RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(
                            DisplayGroup.GroupName.TRANSCRIPT_EDIT_ADDABLE_PROTEIN_SEQUENCE));
        }
        return transcriptEditAddableProteinReferenceDatabases;
    }


    public List<ReferenceDatabaseDTO> getGeneEditAddProteinSequenceReferenceDatabases() {
        if (geneEditAddableProteinReferenceDatabases == null) {
            geneEditAddableProteinReferenceDatabases =
                    DTOHelper.convertReferenceDTOs(RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(
                            DisplayGroup.GroupName.GENE_EDIT_ADDABLE_PROTEIN_SEQUENCE));
        }
        return geneEditAddableProteinReferenceDatabases;
    }
}
