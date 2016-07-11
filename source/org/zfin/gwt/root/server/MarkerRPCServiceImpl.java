package org.zfin.gwt.root.server;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.zfin.Species;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.ConstructRelationship;
import org.zfin.construct.repository.ConstructRepository;
import org.zfin.database.InformixUtil;
import org.zfin.expression.ExpressionExperiment2;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.gwt.root.ui.BlastDatabaseAccessException;
import org.zfin.gwt.root.ui.DBLinkNotFoundException;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.gwt.root.ui.MarkerRPCService;
import org.zfin.infrastructure.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.DiseaseAnnotation;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.profile.MarkerSupplier;
import org.zfin.profile.Organization;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.MountedWublastBlastService;
import org.zfin.util.ZfinStringUtils;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

/**
 */
public class MarkerRPCServiceImpl extends ZfinRemoteServiceServlet implements MarkerRPCService {

    private transient PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private transient MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private transient ConstructRepository constructRepository = RepositoryFactory.getConstructRepository();
    private transient InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private transient Logger logger = Logger.getLogger(MarkerRPCServiceImpl.class);

    private List<String> suppliers = null;

    private transient List<ReferenceDatabaseDTO> dblinkGeneAddReferenceDatabases = new ArrayList<ReferenceDatabaseDTO>();

    public NoteDTO addCuratorNote(NoteDTO noteDTO) {
        logger.info("adding curator note: " + noteDTO.getDataZdbID() + " - " + noteDTO.getNoteData());
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        Marker marker = markerRepository.getMarkerByID(noteDTO.getDataZdbID());
        DataNote dataNote = markerRepository.addMarkerDataNote(marker, noteDTO.getNoteData());
        infrastructureRepository.insertUpdatesTable(marker, "curator notes", "");
        transaction.commit();
        noteDTO.setZdbID(dataNote.getZdbID());
        return noteDTO;
    }

    /**
     * @param noteDTO NoteDTO
     */
    public void editCuratorNote(NoteDTO noteDTO) {
        logger.info("adding curator note: " + noteDTO.getDataZdbID() + " - " + noteDTO.getNoteData());
        Marker marker = markerRepository.getMarkerByID(noteDTO.getDataZdbID());
        Set<DataNote> dataNotes = marker.getDataNotes();
        for (DataNote dataNote : dataNotes) {
            if (dataNote.getZdbID().equals(noteDTO.getZdbID())) {
                dataNote.setNote(ZfinStringUtils.escapeHighUnicode(noteDTO.getNoteData()));
                HibernateUtil.currentSession().update(dataNote);
                HibernateUtil.currentSession().flush();
                return;
            }
        }
        logger.error("note not found with zdbID: " + noteDTO.getZdbID());
    }

    /**
     * @param noteDTO Note DTO
     */
    public void removeCuratorNote(NoteDTO noteDTO) {
        logger.info("remove curator note: " + noteDTO.getNoteData() + " - " + noteDTO.getZdbID());
        Marker marker = markerRepository.getMarkerByID(noteDTO.getDataZdbID());
        Set<DataNote> dataNotes = marker.getDataNotes();
        for (DataNote dataNote : dataNotes) {
            if (dataNote.getZdbID().equals(noteDTO.getZdbID())) {
                HibernateUtil.createTransaction();
                InfrastructureService.insertUpdate(marker, "removed curator note " + dataNote.getNote());
                HibernateUtil.currentSession().delete(dataNote);
                HibernateUtil.flushAndCommitCurrentSession();
                return;
            }
        }
        logger.error("note not found with zdbID: " + noteDTO.getZdbID());
    }


    public void editPublicNote(NoteDTO noteDTO) {
        HibernateUtil.createTransaction();
        Marker marker = markerRepository.getMarkerByID(noteDTO.getDataZdbID());
        String oldNote = marker.getPublicComments();
        String newNote = ZfinStringUtils.escapeHighUnicode(noteDTO.getNoteData());
        if (!StringUtils.equals(newNote, oldNote)) {
            marker.setPublicComments(ZfinStringUtils.escapeHighUnicode(noteDTO.getNoteData()));
            // InfrastructureService.insertUpdate(marker, "Public Note", oldNote, newNote);
            HibernateUtil.currentSession().update(marker);
            HibernateUtil.flushAndCommitCurrentSession();
        }
    }

    // self-attribution

    public void addAttribution(String markerZdbID, String pubZdbID) {
        HibernateUtil.createTransaction();
        infrastructureRepository.insertRecordAttribution(markerZdbID, pubZdbID);
        infrastructureRepository.insertUpdatesTable(markerZdbID, "record attribution", "", pubZdbID, "Added direct attribution");
        HibernateUtil.flushAndCommitCurrentSession();
    }

    public String removeAttribution(String markerZdbID, String pubZdbID) {
        String returnMessage = createRemoveAttributionMessage(markerZdbID, pubZdbID);
        if (StringUtils.isNotEmpty(returnMessage)) {
            return returnMessage;
        }
        HibernateUtil.createTransaction();
        infrastructureRepository.deleteRecordAttribution(markerZdbID, pubZdbID);
        DataAlias dAlias = infrastructureRepository.getDataAliasByID(markerZdbID);
        if (dAlias != null) {
            infrastructureRepository.deleteRecordAttribution(dAlias.getZdbID(), pubZdbID);
        }

        infrastructureRepository.insertUpdatesTable(markerZdbID, "record attribution", pubZdbID, "removed", "Removed direct attribution");
        HibernateUtil.flushAndCommitCurrentSession();
        return null;
    }

    /**
     * From citgeneric.apg.
     *
     * @param zdbID    ZdbID to check against.
     * @param pubZdbID Pub to check against.
     * @return A message if there is a problem.  Otherwise null.
     */
    private String createRemoveAttributionMessage(String zdbID, String pubZdbID) {

        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(pubZdbID);
        if (publication == null) {
            return "Invalid pub: " + pubZdbID;
        }


        // always check db-links first
        if (infrastructureRepository.getDBLinkAttributions(zdbID, pubZdbID) > 0) {
            return createMessage(zdbID, "is associated via a dblink that is");
        }

        // if used in inference of markergoentry
        // if there is a pub with a marker go entry on it that has inferences

        // if feature
        if (zdbID.startsWith("ZDB-ALT-")) {
            Feature feature = RepositoryFactory.getFeatureRepository().getFeatureByID(zdbID);
            return createRemoveAttributionMessageForFeature(feature, publication);
        }

        // if feature
        if (zdbID.startsWith("ZDB-GENO-")) {
            Genotype genotype = getMutantRepository().getGenotypeByID(zdbID);
            return createRemoveAttributionMessageForGenotype(genotype, publication);
        }

        // if a marker
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        if (marker != null) {
            // handle marker case
            return createRemoveAttributionMessageForMarker(marker, publication);
        }

        // if a fish
        List<DiseaseAnnotation> diseaseAnnotationList = getMutantRepository().getDiseaseModel(zdbID, pubZdbID);
        if (diseaseAnnotationList != null) {
            for (DiseaseAnnotation model : diseaseAnnotationList) {
               /* if (model.getDiseaseAnnotationModel() != null)
                    return createMessage(model.getEntityName(), "has a fishmodel");*/
            }
            return null;
        }

        // if anything else
        // direct association
        if (false == zdbID.startsWith("ZDB-GENO") && infrastructureRepository.
                getRecordAttribution(zdbID, pubZdbID, RecordAttribution.SourceType.STANDARD) != null) {
            return createMessage(zdbID, "is directly ");
        }
//            if (infrastructureRepository.getDataAliasesAttributions(zdbID, pubZdbID) > 0) {
//                return createMessage(zdbID,"has data aliases") ;
//            }

        // if marker
        return null;
    }

    private String createRemoveAttributionMessageForGenotype(Genotype genotype, Publication publication) {
        if (getMutantRepository().getZFINInferences(genotype.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(genotype.getHandle(), "is inferred as GO evidence that is");
        }

        if (infrastructureRepository.getGenotypeExperimentRecordAttributions(genotype.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(genotype.getHandle(), "is used in an experiment that is");
        }

        if (infrastructureRepository.getGenotypeExpressionExperimentRecordAttributions(genotype.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(genotype.getHandle(), "is used in expression that is");
        }

        if (infrastructureRepository.getGenotypePhenotypeRecordAttributions(genotype.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(genotype.getHandle(), "is used in phenotype that is");
        }

        return null;
    }

    private String createMessage(String name, String message) {
        return "Can't remove " + name + ": It " + message + " attributed to this pub.";
    }

    private String createRemoveAttributionMessageForMarker(Marker marker, Publication publication) {

        if (getMutantRepository().getZFINInferences(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "is inferred as GO evidence that is ");
        }

        if (infrastructureRepository.getGoRecordAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "has GO annotations ");
        }

        if (infrastructureRepository.getOrthologRecordAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "has been annotated with orthologs ");
        }

        if (infrastructureRepository.getMarkerFeatureRelationshipAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "has a related feature ");
        }

        if (infrastructureRepository.getMarkerGenotypeFeatureRelationshipAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "has a relationship to a feature and genotype is ");
        }

        if (infrastructureRepository.getDBLinkAssociatedToGeneAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "is related to a dblink that is ");
        }

        // see fogbugz 5872
        // we can not remove a gene if it has related markers that are attributed to this pub
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            if (infrastructureRepository.getFirstMarkerRelationshipAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
                return createMessage(marker.getAbbreviation(), "is related to a marker (in the second position) that is ");
            }

            if (infrastructureRepository.getSecondMarkerRelationshipAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
                return createMessage(marker.getAbbreviation(), "is related a marker (in the first position) that is ");
            }
        }

        if (infrastructureRepository.getExpressionExperimentMarkerAttributions(marker, publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "has expression data ");
        }

        if (infrastructureRepository.getSequenceTargetingReagentEnvironmentAttributions(marker.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(marker.getAbbreviation(), "is present in an environment");
        }

        return null;
    }

    private String createRemoveAttributionMessageForFeature(Feature feature, Publication publication) {

        if (infrastructureRepository.getFeatureGenotypeAttributions(feature.getZdbID(), publication.getZdbID()) > 0) {
            return createMessage(feature.getAbbreviation(), "used in a genotype");
        }

        return null;
    }

    public void deleteConstructMarkerRelationship(ConstructRelationshipDTO constructRelationshipDTO) {

        HibernateUtil.createTransaction();
        String zdbID = constructRelationshipDTO.getZdbID();
        infrastructureRepository.insertUpdatesTable(zdbID, "Construct", zdbID, "",
                "deleted construct/marker relationship between: "
                        + constructRelationshipDTO.getConstructDTO().getName()
                        + " and "
                        + constructRelationshipDTO.getMarkerDTO().getName()
                        + " of type "
                        + constructRelationshipDTO.getRelationshipType()
        );
        infrastructureRepository.deleteActiveDataByZdbID(zdbID);
        HibernateUtil.flushAndCommitCurrentSession();
        InformixUtil.runInformixProcedure("regen_construct_marker", constructRelationshipDTO.getConstructDTO().getZdbID() + "");
    }

    //

    /**
     * Add alias attribute.  If there is failure, we want the exception to propagate back
     * to the client.
     *
     * @param relatedEntityDTO bundle of data with data id, name & pub for attribution
     */
    public RelatedEntityDTO addDataAliasRelatedEntity(RelatedEntityDTO relatedEntityDTO) {

        Marker marker = markerRepository.getMarkerByID(relatedEntityDTO.getDataZdbID());
        Session session = HibernateUtil.currentSession();
        String aliasName = ZfinStringUtils.escapeHighUnicode(relatedEntityDTO.getName());
        session.beginTransaction();
        Publication publication = null;
        if (StringUtils.isNotEmpty(relatedEntityDTO.getPublicationZdbID())) {
            publication = (Publication) session.get(Publication.class, relatedEntityDTO.getPublicationZdbID());
        }
        DataAlias dataAlias = markerRepository.getSpecificDataAlias(marker, aliasName);
        if (dataAlias == null) {
            markerRepository.addMarkerAlias(marker, relatedEntityDTO.getName(), publication);
        } else {
            if (false == publication.getZdbID().equals(dataAlias.getSinglePublication())) {
                return addDataAliasAttribution(relatedEntityDTO);
            }
        }

        dataAlias = markerRepository.getSpecificDataAlias(marker, aliasName);
        //no need to handle the updates table here, since the repository method takes care of it

        HibernateUtil.flushAndCommitCurrentSession();
        relatedEntityDTO.setName(dataAlias.getAlias());
        relatedEntityDTO.setDataZdbID(marker.getZdbID());
        return relatedEntityDTO;
    }

    /**
     *
     */
    public String getEditableRelationshipTypesForConstruct() {
        return MarkerRelationship.Type.CONTAINS_ENGINEERED_REGION.toString();
    }

    public List<MarkerDTO> getMarkersForRelation(String featureTypeName, String publicationZdbID) {
        List<Marker> markers = markerRepository.getMarkersForRelation(featureTypeName, publicationZdbID);
        List<MarkerDTO> markerDTOs = new ArrayList<MarkerDTO>();
        for (Marker m : markers) {
            markerDTOs.add(DTOConversionService.convertToMarkerDTO(m));
        }
        return markerDTOs;
    }

    public RelatedEntityDTO addDataAliasAttribution(RelatedEntityDTO relatedEntityDTO) {
        logger.debug(relatedEntityDTO.toString());
        String aliasName = ZfinStringUtils.escapeHighUnicode(relatedEntityDTO.getName());

        Marker marker = markerRepository.getMarkerByID(relatedEntityDTO.getDataZdbID());
        Session session = HibernateUtil.currentSession();
        HibernateUtil.createTransaction();
        DataAlias dataAlias = markerRepository.getSpecificDataAlias(marker, aliasName);
        Publication publication = (Publication) session.get(Publication.class, relatedEntityDTO.getPublicationZdbID());
        if (publication != null) {
            markerRepository.addDataAliasAttribution(dataAlias, publication, marker);
        }
        HibernateUtil.flushAndCommitCurrentSession();
        relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publication.getZdbID());
        relatedEntityDTO.setName(dataAlias.getAlias());
        relatedEntityDTO.setDataZdbID(marker.getZdbID());
        return relatedEntityDTO;
    }

    /**
     * Delete a dataAlias
     *
     * @param relatedEntityDTO bindle of data with data id, name & pub for attribution
     */
    public void removeDataAliasRelatedEntity(RelatedEntityDTO relatedEntityDTO) {
        Marker marker = markerRepository.getMarkerByID(relatedEntityDTO.getDataZdbID());
        Publication publication = null;
        if (StringUtils.isNotEmpty(relatedEntityDTO.getPublicationZdbID())) {
            publication = RepositoryFactory.getPublicationRepository().getPublication(relatedEntityDTO.getPublicationZdbID());
        }
        MarkerAlias dataAlias = markerRepository.getSpecificDataAlias(marker, relatedEntityDTO.getName());
        HibernateUtil.createTransaction();
        InfrastructureService.insertUpdate(marker, "Removed alias: " + dataAlias.getAlias() + "' attributed to publication: '"
                + (publication == null ? "null" : publication.getZdbID()) + "'");
        markerRepository.deleteMarkerAlias(marker, dataAlias);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    /**
     * @param relatedEntityDTO bindle of data with data id, name & pub for attribution
     */
    public void removeDataAliasAttribution(RelatedEntityDTO relatedEntityDTO) {
        String aliasName = ZfinStringUtils.escapeHighUnicode(relatedEntityDTO.getName());
        String pub = relatedEntityDTO.getPublicationZdbID();
        Marker marker = markerRepository.getMarkerByID(relatedEntityDTO.getDataZdbID());
        DataAlias dataAlias = markerRepository.getSpecificDataAlias(marker, aliasName);

        infrastructureRepository.deleteRecordAttribution(dataAlias.getZdbID(), pub);
        InfrastructureService.insertUpdate(marker, "Removed attribution: '" + pub + "' from alias: '" + aliasName + "'");
    }

    public DBLinkDTO addInternalProteinSequence(String markerZdbID, String sequenceData, String pubZdbID, String referenceZdbID) throws BlastDatabaseAccessException {

        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();

        try {
            ReferenceDatabase referenceDatabase = (ReferenceDatabase) session.get(ReferenceDatabase.class, referenceZdbID);
            // get the appropriate referenceDatabase
            logger.info("referenceDB zdbID: " + referenceZdbID);
            logger.info("referenceDB: " + referenceDatabase);
            Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerZdbID);

            Sequence sequence = MountedWublastBlastService.getInstance().addProteinToMarker(marker, sequenceData, pubZdbID, referenceDatabase);

            List<DBLinkDTO> dbLinkDTOs = DTOConversionService.convertToDBLinkDTOs((MarkerDBLink) sequence.getDbLink());
            DBLinkDTO dbLinkDTO = dbLinkDTOs.get(0);
            dbLinkDTO.setPublicationZdbID(pubZdbID);

            InfrastructureService.insertUpdate(marker, "Added protein sequence: '" +
                    sequence.getDbLink().getAccessionNumber() + "' with attribution: '" + referenceZdbID + "'");

            //accession numbers end up in the name tables, so a regen will make the accession
            //search in markerselect work for this new sequence.
            markerRepository.runMarkerNameFastSearchUpdate(marker);

            transaction.commit();
            return dbLinkDTO;
        } catch (Exception e) {
            logger.error("Failure to add internal protein sequence", e);
            transaction.rollback();
            throw new BlastDatabaseAccessException("Failed to add protein sequence.", e);
        }
    }

    public DBLinkDTO addInternalNucleotideSequence(String markerZdbID, String sequenceData, String pubZdbID,
                                                   String referenceZdbID) throws BlastDatabaseAccessException {

        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();

        try {
            ReferenceDatabase referenceDatabase = (ReferenceDatabase) session.get(ReferenceDatabase.class, referenceZdbID);
            // get the appropriate referenceDatabase
            logger.info("referenceDB zdbID: " + referenceZdbID);
            logger.info("referenceDB: " + referenceDatabase);
            Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerZdbID);

            Sequence sequence = MountedWublastBlastService.getInstance().
                    addSequenceToMarker(marker, sequenceData, pubZdbID, referenceDatabase);

            List<DBLinkDTO> dbLinkDTOs = DTOConversionService.convertToDBLinkDTOs((MarkerDBLink) sequence.getDbLink());
            DBLinkDTO dbLinkDTO = dbLinkDTOs.get(0);
            dbLinkDTO.setPublicationZdbID(pubZdbID);

            String updateComment = "Added nucleotide sequence: '" + dbLinkDTO.getName()
                    + "' with attribution: " + pubZdbID;

            InfrastructureService.insertUpdate(marker, updateComment);

            //accession numbers end up in the name tables, so a regen will make the accession
            //search in markerselect work for this new sequence.
            markerRepository.runMarkerNameFastSearchUpdate(marker);

            transaction.commit();
            return dbLinkDTO;
        } catch (Exception e) {
            logger.error("Failure to add internal protein sequence", e);
            transaction.rollback();
            throw new BlastDatabaseAccessException("Failed to add nucleotide sequence.", e);
        }
    }

    /**
     * @param dbLink
     * @param referenceDatabaseDTOs
     * @return Return true if dblink referenceDatabase part of the list, or no referenceDatabases passed in.
     * @throws DBLinkNotFoundException If dblink not found, throws exception instead of returning false.
     */
    protected boolean referenceDBAllowed(DBLink dbLink, List<ReferenceDatabaseDTO> referenceDatabaseDTOs) throws DBLinkNotFoundException {
        if (referenceDatabaseDTOs == null || referenceDatabaseDTOs.size() == 0) {
            return true;
        }

        boolean referenceDBFound = false;
        ReferenceDatabaseDTO referenceDatabaseDTO;
        for (Iterator<ReferenceDatabaseDTO> iter = referenceDatabaseDTOs.iterator();
             iter.hasNext() && referenceDBFound == false;
                ) {
            referenceDatabaseDTO = iter.next();
            if (true == dbLink.getReferenceDatabase().getZdbID().equalsIgnoreCase(referenceDatabaseDTO.getZdbID())) {
                return true;
            }
        }


        // nothing is found so throw exception
        throw new DBLinkNotFoundException(
                "Can not add dblinks with reference database not contained in list: " +
                        dbLink.getReferenceDatabase().getForeignDB().getDbName() + "-" +
                        dbLink.getReferenceDatabase().getZdbID());
    }

    /**
     * This method creates a dblink based on an existing accession(1) or dblink(2).  Unfortunately, we can not guarantee
     * that an accession exists for every dblink.
     *
     * @param dbLinkDTO DBLinkDTO to ad dDBLink from.
     * @return Returns the saved version of the DBLink.
     * @throws DBLinkNotFoundException
     */
    public DBLinkDTO addDBLink(DBLinkDTO dbLinkDTO, List<ReferenceDatabaseDTO> referenceDatabaseDTOs) throws DBLinkNotFoundException {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();

        DBLink dbLink = DTOConversionService.convertToDBLink(dbLinkDTO);

        Accession accession = RepositoryFactory.getSequenceRepository().getAccessionByAlternateKey(dbLink.getAccessionNumber(), dbLink.getReferenceDatabase());
        if (accession == null) {
            // check to see that there is a dblink
            List<DBLink> dbLinks = RepositoryFactory.getSequenceRepository().getDBLinks(dbLink.getAccessionNumber(), dbLink.getReferenceDatabase());
            if (dbLinks.size() > 0) {
                DBLink refDbLink = dbLinks.get(0);
                referenceDBAllowed(refDbLink, referenceDatabaseDTOs);
                accession = new Accession();
                accession.setNumber(refDbLink.getAccessionNumber());
                accession.setReferenceDatabase(refDbLink.getReferenceDatabase());
                dbLink.setReferenceDatabase(refDbLink.getReferenceDatabase());
                session.save(accession);
            } else {
                String message = "Not found: accession[" + dbLink.getAccessionNumber() + "] ";
                if (dbLink.getReferenceDatabase() != null) {
                    message += "referenceDB[" + dbLink.getReferenceDatabase().getForeignDB().getDbName() +
                            "-" + dbLink.getReferenceDatabase().getForeignDBDataType().getDataType() + "]";
                }
                throw new DBLinkNotFoundException(message);
            }
        } else {
            dbLink.setReferenceDatabase(accession.getReferenceDatabase());
            referenceDBAllowed(dbLink, referenceDatabaseDTOs);
            if (dbLinkDTO.getLength() == null) {
                dbLink.setLength(accession.getLength());
            }
            // if it is set, though
            else {
                accession.setLength(dbLinkDTO.getLength());
                session.update(accession);
            }
        }


        session.save(dbLink);
        session.flush();
        dbLinkDTO.setZdbID(dbLink.getZdbID());

        // set publicationattribution
        if (StringUtils.isNotEmpty(dbLinkDTO.getPublicationZdbID())) {
            Set<PublicationAttribution> publications = new HashSet<PublicationAttribution>();
            infrastructureRepository.insertPublicAttribution(dbLinkDTO.getZdbID(),
                    dbLinkDTO.getPublicationZdbID());
            PublicationAttribution publicationAttribQuery = new PublicationAttribution();
            publicationAttribQuery.setPublication(getPublicationRepository().getPublication(dbLinkDTO.getPublicationZdbID()));
            PublicationAttribution publicationAttribution = infrastructureRepository.getPublicationAttribution(publicationAttribQuery);
            publications.add(publicationAttribution);
            dbLink.setPublications(publications);
        }

        session.update(dbLink);
        session.flush();

        Marker marker = (Marker) session.get(Marker.class, dbLink.getDataZdbID());

        List<DBLinkDTO> dbLinkDTOs = DTOConversionService.convertToDBLinkDTOs(dbLink, marker.getZdbID(), marker.getAbbreviation());
        // if it fails, it will automatically roll-back and automatically throws exception up
        transaction.commit();

        return dbLinkDTOs.get(0);
    }


    /**
     * In this method, we insert a characterized attribution if none exist, otherwise we enter a standard
     *
     * @param sequenceDTO The dblink to attribute.
     */
    public SequenceDTO addSequenceAttribution(SequenceDTO sequenceDTO) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        String dblinkZdbID = sequenceDTO.getZdbID();
        String dblinkDataZdbID = sequenceDTO.getDataZdbID();

        DBLink dbLink = null;
        if (dblinkZdbID == null && sequenceDTO.getDataName() != null && sequenceDTO.getDataZdbID() != null) {
            dbLink = RepositoryFactory.getSequenceRepository().getDBLinkByAlternateKey(sequenceDTO.getName(),
                    dblinkDataZdbID, DTOConversionService.convertToReferenceDatabase(sequenceDTO.getReferenceDatabaseDTO()));
            if (dbLink == null) {
                throw new RuntimeException("Faild to get a dblink for dblinkdto: " + sequenceDTO);
            }
            dblinkZdbID = dbLink.getZdbID();
            sequenceDTO.setZdbID(dblinkZdbID);
        }
        List<PublicationAttribution> publicationAttributions = infrastructureRepository.getPublicationAttributions(dblinkZdbID);
        if (publicationAttributions.size() > 0) {
            infrastructureRepository.insertPublicAttribution(dblinkZdbID, sequenceDTO.getPublicationZdbID());
            sequenceDTO.setAttributionType(RecordAttribution.SourceType.STANDARD.toString());
        } else {
            infrastructureRepository.insertPublicAttribution(dblinkZdbID, sequenceDTO.getPublicationZdbID(), RecordAttribution.SourceType.FIRST_CURATED_SEQUENCE_PUB);
            sequenceDTO.setAttributionType(RecordAttribution.SourceType.FIRST_CURATED_SEQUENCE_PUB.toString());
        }

        session.flush();

        // if it fails, it will automatically roll-back and automatically throws exception up
        transaction.commit();
        return sequenceDTO;
    }

    public DBLinkDTO addDBLinkAttribution(DBLinkDTO dbLinkDTO) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        String dblinkZdbID = dbLinkDTO.getZdbID();
        String dblinkDataZdbID = dbLinkDTO.getDataZdbID();

        DBLink dbLink = null;
        if (dblinkZdbID != null) {
            dbLink = RepositoryFactory.getSequenceRepository().getDBLinkByID(dblinkZdbID);
        } else if (dbLinkDTO.getDataName() != null && dbLinkDTO.getDataZdbID() != null) {
            dbLink = RepositoryFactory.getSequenceRepository().getDBLinkByAlternateKey(dbLinkDTO.getName(),
                    dblinkDataZdbID, DTOConversionService.convertToReferenceDatabase(dbLinkDTO.getReferenceDatabaseDTO()));
            if (dbLink == null) {
                throw new RuntimeException("Faild to get a dblink for dblinkdto: " + dbLinkDTO);
            }
            dblinkZdbID = dbLink.getZdbID();
            dbLinkDTO.setZdbID(dblinkZdbID);
        }

        infrastructureRepository.insertPublicAttribution(dblinkZdbID, dbLinkDTO.getPublicationZdbID());
        session.flush();

        DTOConversionService.convertToDBLinkDTO(dbLink, dbLinkDTO.getDataZdbID(), dbLinkDTO.getDataName(), dbLinkDTO.getPublicationZdbID());
        // if it fails, it will automatically roll-back and automatically throws exception up
        transaction.commit();
        return dbLinkDTO;
    }

    public DBLinkDTO removeDBLink(DBLinkDTO dbLinkDTO) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        DBLink dbLink = (DBLink) session.get(DBLink.class, dbLinkDTO.getZdbID());
        if (dbLink == null && dbLinkDTO.getDataZdbID() != null && dbLinkDTO.getName() != null) {
            dbLink = RepositoryFactory.getSequenceRepository().
                    getDBLink(dbLinkDTO.getDataZdbID(),
                            dbLinkDTO.getName(), dbLinkDTO.getReferenceDatabaseDTO().getName());
        }
        if (dbLink == null) {
            throw new RuntimeException("Failed to find dblink: " + dbLinkDTO.toString());
        }

        // remove attributions
        infrastructureRepository.deleteRecordAttributionsForData(dbLink.getZdbID());

        // remove objects
        session.delete(dbLink);

        session.flush();

        // if it fails, it will automatically roll-back and automatically throws exception up
        transaction.commit();
        return dbLinkDTO;
    }

    public DBLinkDTO removeDBLinkAttribution(DBLinkDTO dbLinkDTO) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        // need to get the marker for the dblink
        DBLink dbLink = (DBLink) session.get(DBLink.class, dbLinkDTO.getZdbID());

        dbLinkDTO.setZdbID(dbLink.getZdbID());
        infrastructureRepository.deleteRecordAttribution(dbLink.getZdbID(), dbLinkDTO.getPublicationZdbID());
        session.flush();

        // if it fails, it will automatically roll-back and automatically throws exception up
        transaction.commit();

        return dbLinkDTO;
    }

    public DBLinkDTO updateDBLinkLength(DBLinkDTO dbLinkDTO) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();

        Publication pub = publicationRepository.getPublication(dbLinkDTO.getPublicationZdbID());
        Marker marker = markerRepository.getMarkerByID(dbLinkDTO.getDataZdbID());
        DBLink dbLink = (DBLink) session.get(DBLink.class, dbLinkDTO.getZdbID());

        String updateComment = "Updating " + dbLink.getAccessionNumber()
                + " length, changing from " + dbLink.getLength()
                + " to " + dbLinkDTO.getLength()
                + " with attribution: " + pub.getZdbID();
        InfrastructureService.insertUpdate(marker, updateComment);

        List<Accession> accessions = RepositoryFactory.getSequenceRepository().getAccessionsByNumber(dbLinkDTO.getName());
        for (Accession accession : accessions) {
            logger.debug("updating " + accession.getNumber() + " (id:" + accession.getID() + ") length from "
                    + accession.getLength() + " to " + dbLinkDTO.getLength());
            accession.setLength(dbLinkDTO.getLength());
            session.update(accession);

        }
        session.flush();
        dbLink.setLength(dbLinkDTO.getLength());
        session.refresh(dbLink);
        logger.debug("resulting length for " + dbLink.getZdbID() + " is: " + dbLink.getLength());
/*        dbLink.setLength(dbLinkDTO.getLength());
        session.refresh(dbLink);*/


        // if it fails, it will automatically roll-back and automatically throws exception up
        transaction.commit();

        dbLink = RepositoryFactory.getSequenceRepository().getDBLinkByID(dbLink.getZdbID());

        return DTOConversionService.convertToDBLinkDTO(dbLink, marker.getZdbID(), marker.getName(), pub.getZdbID());
    }

    public MarkerDTO getMarkerForName(String name) {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(name);
        if (marker == null) {
            return null;
        }

        return DTOConversionService.convertToMarkerDTO(marker);
    }


    public MarkerDTO getGeneForZdbID(String zdbID) {
        Marker marker = markerRepository.getMarkerByID(zdbID);
        MarkerDTO geneDTO = new MarkerDTO();

        // get simple attributes
        geneDTO.setZdbID(marker.getZdbID());
        geneDTO.setName(marker.getAbbreviation());
        geneDTO.setName(marker.getName());
        geneDTO.setMarkerType(marker.getMarkerType().getType().name());


        geneDTO.setSuppliers(MarkerService.getSuppliers(marker));

        geneDTO.setRecordAttributions(MarkerService.getDirectAttributions(marker));

        geneDTO.setCuratorNotes(DTOMarkerService.getCuratorNoteDTOs(marker));
        geneDTO.setPublicNote(DTOMarkerService.getPublicNoteDTO(marker));

        geneDTO.setAliasAttributes(DTOMarkerService.getMarkerAliasDTOs(marker));

        // get related genes
        // todo: this is the wrong method for this
        geneDTO.setRelatedGeneAttributes(DTOMarkerService.getRelatedGenesMarkerDTOs(marker));

        // todo: this is the wrong method for this
        geneDTO.setSupportingSequenceLinks(DTOMarkerService.getSupportingSequenceDTOs(marker));

        return geneDTO;
    }

    public MarkerDTO getGeneOnlyForZdbID(String zdbID) {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        if (marker == null) {
            logger.error("marker: " + zdbID + " is NULL");
            return null;
        }
//        if(marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)){
//            logger.error("marker: " + zdbID + " is not a gene: "+ marker.toString() + " for");
//            return null ;
//        }

        return DTOConversionService.convertToMarkerDTO(marker);
    }

    // get all existing supplier names

    public List<String> getAllSupplierNames() {
        if (suppliers == null) {
            Session session = HibernateUtil.currentSession();

            String hqlLab = " select ms.organization.name from MarkerSupplier ms group by ms.organization.name order by ms.organization.name ";
            Query queryLab = session.createQuery(hqlLab);
            suppliers = (List<String>) queryLab.list();
            return suppliers;
        } else {
            List<String> returnList = Arrays.asList(new String[suppliers.size()]);
            java.util.Collections.copy(returnList, suppliers);
            new SupplierCacheThread().start();
            return returnList;
        }
    }

    private class SupplierCacheThread extends Thread {

        @Override
        public void run() {
            suppliers = null;
            getAllSupplierNames();
        }
    }

    // re-cache on the back-end

    public void addMarkerSupplier(String name, String markerZdbID) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(Organization.class);
        criteria.add(Restrictions.eq("name", name));
        Organization organization = (Organization) criteria.uniqueResult();
        if (organization == null) {
            throw new RuntimeException("orginziation not found: " + name);
        }
        Marker marker = (Marker) session.get(Marker.class, markerZdbID);

        Transaction transaction = session.beginTransaction();
        MarkerSupplier markerSupplier = new MarkerSupplier();
        markerSupplier.setMarker(marker);
        markerSupplier.setOrganization(organization);

        InfrastructureService.insertUpdate(marker, "Added supplier, ");

        session.save(markerSupplier);
        session.flush();
        session.refresh(markerSupplier);
        InfrastructureService.insertUpdate(marker, "Added supplier: " + markerSupplier.getOrganization().getName());
        transaction.commit();
    }

    public void removeMarkerSupplier(String name, String markerZdbID) {
        Session session = HibernateUtil.currentSession();

        Marker marker = (Marker) session.get(Marker.class, markerZdbID);

        String hql = " from MarkerSupplier ms where ms.marker.zdbID = :markerZdbID and ms.organization.name = :supplierName";
        Query query = session.createQuery(hql);
        query.setString("markerZdbID", markerZdbID);
        query.setString("supplierName", name);
        MarkerSupplier markerSupplier = (MarkerSupplier) query.uniqueResult();

        if (markerSupplier != null) {
            Transaction transaction = session.beginTransaction();
            session.delete(markerSupplier);
            session.flush();
            transaction.commit();
        }

        InfrastructureService.insertUpdate(marker, "Removed supplier: " + name);

    }

    public String getWebDriverPath() {
        return "/" + ZfinProperties.getWebDriver();
    }

    public List<ConstructRelationshipDTO> getConstructMarkerRelationshipsForPub(String publicationZdbID) {

        List<ConstructRelationshipDTO> constructRelnDTOs = new ArrayList<ConstructRelationshipDTO>();
        List<ConstructRelationship> constructMarkerRelationships = constructRepository.getConstructRelationshipsByPublication(publicationZdbID);
        if (CollectionUtils.isNotEmpty(constructMarkerRelationships)) {
            for (ConstructRelationship markerRelationship : constructMarkerRelationships) {
                constructRelnDTOs.add(DTOConversionService.convertToConstructRelationshipDTO(markerRelationship));
            }
        }


        return constructRelnDTOs;
    }


    public MarkerDTO addRelatedMarker(MarkerDTO markerDTO) throws TermNotFoundException {
        HibernateUtil.createTransaction();

        Marker firstMarker = markerRepository.getMarkerByID(markerDTO.getZdbID());
        Marker secondMarker = markerRepository.getMarkerByAbbreviation(markerDTO.getName());

        if (secondMarker == null || firstMarker == null) {
            throw new TermNotFoundException(markerDTO.getName(), markerDTO.getMarkerRelationshipType());
        }
        if (true == markerDTO.isZdbIDThenAbbrev()) {
            MarkerService.addMarkerRelationship(firstMarker, secondMarker, markerDTO.getPublicationZdbID(), MarkerRelationship.Type.getType(markerDTO.getMarkerRelationshipType()));
        //    ExpressionExperiment2 expExpt=RepositoryFactory.getExpressionRepository().get
        } else {
            MarkerService.addMarkerRelationship(secondMarker, firstMarker, markerDTO.getPublicationZdbID(), MarkerRelationship.Type.getType(markerDTO.getMarkerRelationshipType()));
        }

        logger.debug("addMarkerRelationship, first marker: " + firstMarker.getAbbreviation()
                + " second marker: " + secondMarker.getAbbreviation());

        HibernateUtil.flushAndCommitCurrentSession();

        // always return the abbreviation
        MarkerDTO dto = DTOConversionService.convertToMarkerDTO(secondMarker);
        dto.setPublicationZdbID(markerDTO.getPublicationZdbID());
        dto.setDataZdbID(firstMarker.getZdbID());
        return dto;
    }

    public MarkerDTO addRelatedMarkerAttribution(MarkerDTO markerDTO) {
        HibernateUtil.createTransaction();
        Marker firstMarker = markerRepository.getMarkerByID(markerDTO.getZdbID());
        Marker secondMarker = markerRepository.getMarkerByAbbreviation(markerDTO.getName());
        MarkerRelationship.Type type = MarkerRelationship.Type.getType(markerDTO.getMarkerRelationshipType());
        if (true == markerDTO.isZdbIDThenAbbrev()) {
            MarkerService.addMarkerRelationshipAttribution(firstMarker, secondMarker, markerDTO.getPublicationZdbID(), type);
        } else {
            MarkerService.addMarkerRelationshipAttribution(secondMarker, firstMarker, markerDTO.getPublicationZdbID(), type);
        }
        HibernateUtil.flushAndCommitCurrentSession();

        MarkerDTO dto = DTOConversionService.convertToMarkerDTO(secondMarker);
        dto.setPublicationZdbID(markerDTO.getPublicationZdbID());
        dto.setDataZdbID(firstMarker.getZdbID());
        return dto;
    }

    public void removeRelatedMarker(MarkerDTO markerDTO) {
        HibernateUtil.createTransaction();
        Marker firstMarker = markerRepository.getMarkerByID(markerDTO.getZdbID());
        Marker secondMarker = markerRepository.getMarkerByAbbreviation(markerDTO.getName());
        MarkerRelationship.Type type = MarkerRelationship.Type.getType(markerDTO.getMarkerRelationshipType());

        if (true == markerDTO.isZdbIDThenAbbrev()) {
            MarkerService.deleteMarkerRelationship(firstMarker, secondMarker, type);
        } else {
            MarkerService.deleteMarkerRelationship(secondMarker, firstMarker, type);
        }

        HibernateUtil.flushAndCommitCurrentSession();
    }

    public List<ConstructDTO> getConstructsForPub(String pubZdbId) {
        //Publication pub=publicationRepository.getPublication(pubZdbId);
        List<ConstructDTO> constructDTOs = new ArrayList<ConstructDTO>();
        List<ConstructCuration> constructs = markerRepository.getConstructsForAttribution(pubZdbId);
        if (CollectionUtils.isNotEmpty(constructs)) {
            for (ConstructCuration m : constructs) {

                if (m.getZdbID().contains("CONSTRCT")) {
                    constructDTOs.add(DTOConversionService.convertToConstructDTO(m));

                }
            }
        }

        return constructDTOs;
    }

    public void removeRelatedMarkerAttribution(MarkerDTO markerDTO) {
        HibernateUtil.createTransaction();
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        Marker firstMarker = markerRepository.getMarkerByID(markerDTO.getZdbID());
        Marker secondMarker = markerRepository.getMarkerByAbbreviation(markerDTO.getName());
        MarkerRelationship.Type type = MarkerRelationship.Type.getType(markerDTO.getMarkerRelationshipType());

        if (true == markerDTO.isZdbIDThenAbbrev()) {
            MarkerService.deleteMarkerRelationshipAttribution(firstMarker, secondMarker, markerDTO.getPublicationZdbID(), type);
        } else {
            MarkerService.deleteMarkerRelationshipAttribution(secondMarker, firstMarker, markerDTO.getPublicationZdbID(), type);
        }

        HibernateUtil.flushAndCommitCurrentSession();
    }

    /**
     * If a clone tries to associate an existing dblink, then it should fail.
     *
     * @param dbLinkDTO DBlink to validate.
     * @return Any sort of warning strings.
     */
    public String validateDBLink(DBLinkDTO dbLinkDTO) {
        List<DBLink> dbLinks = RepositoryFactory.getSequenceRepository().getDBLinksForAccession(dbLinkDTO.getName());
        if (dbLinks == null || dbLinks.size() == 0) {
            return null;
        } else {
            List<String> zdbIDs = new ArrayList<String>();
            for (DBLink dbLink : dbLinks) {
                zdbIDs.add(dbLink.getDataZdbID());
            }
            if (zdbIDs.size() > 0) {
                return "Sequence already associated with another marker: " + zdbIDs.toString();
            } else {
                return null;
            }
        }
    }

    @Override
    public NoteDTO addExternalNote(NoteDTO noteDTO) {
        HibernateUtil.createTransaction();
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(noteDTO.getDataZdbID());
        AntibodyExternalNote antibodyExternalNote = markerRepository.addAntibodyExternalNote(antibody, noteDTO.getNoteData(), noteDTO.getPublicationZdbID());
        noteDTO.setZdbID(antibodyExternalNote.getZdbID());
        infrastructureRepository.insertUpdatesTable(antibody, "notes", "");
        HibernateUtil.flushAndCommitCurrentSession();
        return noteDTO;
    }

    @Override
    public void editExternalNote(NoteDTO noteDTO) {
        HibernateUtil.createTransaction();
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(noteDTO.getDataZdbID());
        markerRepository.editAntibodyExternalNote(noteDTO.getZdbID(), noteDTO.getNoteData());
        infrastructureRepository.insertUpdatesTable(antibody, "updated notes", "");
        HibernateUtil.flushAndCommitCurrentSession();
    }

    @Override
    public void removeExternalNote(NoteDTO noteDTO) {
        logger.info("remove external note: " + noteDTO.getNoteData() + " - " + noteDTO.getZdbID());
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(noteDTO.getDataZdbID());
        Set<AntibodyExternalNote> dataNotes = antibody.getExternalNotes();
        for (AntibodyExternalNote dataNote : dataNotes) {
            if (dataNote.getZdbID().equals(noteDTO.getZdbID())) {
                HibernateUtil.createTransaction();

                infrastructureRepository.deleteActiveDataByZdbID(noteDTO.getZdbID());
                infrastructureRepository.insertUpdatesTable(antibody, "deleted notes", "");
                HibernateUtil.flushAndCommitCurrentSession();
                return;
            }
        }
        logger.error("note not found with zdbID: " + noteDTO.getZdbID());
    }

    @Override
    public void updateMarkerHeaders(MarkerDTO markerDTO) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        Marker gene = (Marker) session.get(Marker.class, markerDTO.getZdbID());

        // set name

        if (!gene.getName().equals(markerDTO.getName())) {
            String oldName = gene.getName();

            gene.setAbbreviation(markerDTO.getName());

            InfrastructureService.insertUpdate(gene, "Name", oldName, gene.getName());
            //run regen script
            markerRepository.runMarkerNameFastSearchUpdate(gene);
        }


        session.update(gene);
        session.flush();

        // if it fails, it will automatically roll-back and automatically throws exception up
        transaction.commit();
    }


    public List<ReferenceDatabaseDTO> getGeneDBLinkAddReferenceDatabases(String markerZdbID) {
        if (dblinkGeneAddReferenceDatabases.size() > 0) {
            return dblinkGeneAddReferenceDatabases;
        }

        dblinkGeneAddReferenceDatabases.clear();
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerZdbID);
        List<ReferenceDatabase> referenceDatabases = new ArrayList<ReferenceDatabase>();
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM_AND_EFG)) {
            referenceDatabases.add(RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.GENOMIC,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.Type.ZEBRAFISH
            ));

            referenceDatabases.add(RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.RNA,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.Type.ZEBRAFISH
            ));

        }

        if (false == referenceDatabases.isEmpty()) {
            dblinkGeneAddReferenceDatabases.addAll(DTOConversionService.convertToReferenceDatabaseDTOs(referenceDatabases));
        }

        return dblinkGeneAddReferenceDatabases;
    }

    @Override
    public void addAttributionForMarkerName(String markerAbbrev, String pubZdbID) throws TermNotFoundException, DuplicateEntryException {
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(markerAbbrev);
        if (m == null) {
            throw new TermNotFoundException(markerAbbrev, "Marker");
        }
        String markerZdbID = m.getZdbID();
        if (infrastructureRepository.getRecordAttribution(markerZdbID, pubZdbID, RecordAttribution.SourceType.STANDARD) != null) {
            throw new DuplicateEntryException(m.getAbbreviation() + " is already attributed.");
        }
        HibernateUtil.createTransaction();
        infrastructureRepository.insertRecordAttribution(markerZdbID, pubZdbID);
        infrastructureRepository.insertUpdatesTable(markerZdbID, "record attribution", "", pubZdbID, "Added direct attribution");
        HibernateUtil.flushAndCommitCurrentSession();
    }

    @Override
    public void addAttributionForFeatureName(String featureAbbrev, String pubZdbID) throws TermNotFoundException, DuplicateEntryException {
        Feature f = RepositoryFactory.getFeatureRepository().getFeatureByAbbreviation(featureAbbrev);
        if (f == null) {
            throw new TermNotFoundException(featureAbbrev, "Feature");
        }
        String featureZdbID = f.getZdbID();
        if (infrastructureRepository.getRecordAttribution(featureZdbID, pubZdbID, RecordAttribution.SourceType.STANDARD) != null) {
            throw new DuplicateEntryException(f.getAbbreviation() + " is already attributed as " + f.getName());
        }
        HibernateUtil.createTransaction();
        infrastructureRepository.insertRecordAttribution(featureZdbID, pubZdbID);
        infrastructureRepository.insertUpdatesTable(featureZdbID, "record attribution", pubZdbID, "Added direct attribution");
        HibernateUtil.flushAndCommitCurrentSession();
    }


    public void addConstructMarkerRelationShip(ConstructRelationshipDTO constructRelationshipDTO) {
        ConstructRelationship constructRelationship = new ConstructRelationship();
        MarkerRelationship markerRelationship = new MarkerRelationship();
        ConstructDTO constructDTO = constructRelationshipDTO.getConstructDTO();
        ConstructCuration construct = constructRepository.getConstructByID(constructDTO.getZdbID());
        constructRelationship.setConstruct(construct);

        MarkerDTO markerDTO = constructRelationshipDTO.getMarkerDTO();
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerDTO.getZdbID());
        constructRelationship.setMarker(marker);

        constructRelationship.setType(ConstructRelationship.Type.getType(constructRelationshipDTO.getRelationshipType()));
        markerRelationship.setFirstMarker(markerRepository.getMarkerByID(constructDTO.getZdbID()));
        markerRelationship.setSecondMarker(marker);
        markerRelationship.setType(MarkerRelationship.Type.getType(constructRelationshipDTO.getRelationshipType()));
        HibernateUtil.createTransaction();
        HibernateUtil.currentSession().save(constructRelationship);
        HibernateUtil.currentSession().save(markerRelationship);
        infrastructureRepository.insertPublicAttribution(constructRelationship.getZdbID(), constructRelationshipDTO.getPublicationZdbID());
//        infrastructureRepository.insertUpdatesTable(markerRelationship.getZdbID(), "ConstructMarkerRelationship", markerRelationship.toString(), "Created construct marker relationship");
        HibernateUtil.flushAndCommitCurrentSession();
        //InformixUtil.runInformixProcedure("regen_construct_marker", construct.getZdbID() + "");
    }

    @Override
    public void checkDeattributionRules(String entityID, String publicationID)
            throws DeAttributionException {
        if (ActiveData.Type.GENO.equals(ActiveData.getType(entityID))) {
            long count = getMutantRepository().getFishCountByGenotype(entityID, publicationID);
            if (count > 0)
                throw new DeAttributionException("Cannot remove attribution as there are  " + count + " fish using this genotype");
            count = getMutantRepository().getInferredFromCountByGenotype(entityID, publicationID);
            if (count > 0)
                throw new DeAttributionException("Cannot remove attribution as there are  " + count + " inferred-from usage in GO annotation for this genotype");
            count = getInfrastructureRepository().getDistinctPublicationsByData(entityID);
            if (count == 1)
                throw new DeAttributionException("It's the last attribution. Please use the delete genotype feature");


        }
        if (ActiveData.Type.FISH.equals(ActiveData.getType(entityID))) {
            Fish fish = getMutantRepository().getFish(entityID);
            List<DiseaseAnnotationModel> diseaseAnnotationList = getPhenotypeRepository().getHumanDiseaseModelsByFish(entityID);
            if (CollectionUtils.isNotEmpty(diseaseAnnotationList)) {
                throw new DeAttributionException("Cannot remove attribution as there is human disease data associated to this fish");
            }
            long phenotypeStatementsCount = getMutantRepository().getPhenotypeByFishAndPublication(fish, publicationID);
            if (phenotypeStatementsCount > 0) {
                throw new DeAttributionException("Cannot remove attribution as there is phenotype data associated to this fish");
            }
            long count = getMutantRepository().getFishExperimentCountByGenotype(fish, publicationID);
            if (count > 0)
                throw new DeAttributionException("Cannot remove attribution as there are  " + count + " fish experiments using this fish");
            long fishExpressionResultsCount = getExpressionRepository().getExpressionResultsByFishAndPublication(fish, publicationID);
            if (fishExpressionResultsCount > 0) {
                throw new DeAttributionException("Cannot remove attribution as there is expression data associated to this fish");
            }
            long fishExpressionExpCount = getExpressionRepository().getExpressionExperimentByFishAndPublication(fish, publicationID);
            if (fishExpressionExpCount > 0) {
                throw new DeAttributionException("Cannot remove attribution as there are " + fishExpressionExpCount + " expression experiment data associated to this fish");
            }
            count = getInfrastructureRepository().getDistinctPublicationsByData(entityID);
            if (count == 1)
                throw new DeAttributionException("It's the last attribution. Please use the delete fish feature");
        }
    }


}
