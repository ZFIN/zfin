package org.zfin.gwt.marker.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.marker.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.infrastructure.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerService;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.Species;
import org.zfin.people.MarkerSupplier;
import org.zfin.people.Organization;
import org.zfin.people.Person;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.MountedWublastBlastService;

import java.util.*;

/**
 */
public class MarkerRPCServiceImpl extends RemoteServiceServlet implements MarkerRPCService {

    private transient PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private transient MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private transient InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private transient Logger logger = Logger.getLogger(MarkerRPCServiceImpl.class);

    private List<String> suppliers = null;

    private transient List<ReferenceDatabaseDTO> dblinkGeneAddReferenceDatabases = new ArrayList<ReferenceDatabaseDTO>();

    public PublicationDTO getPublicationAbstract(String zdbID) {

        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            return null;
        }
//
        PublicationDTO publicationAbstractDTO = new PublicationDTO();
        publicationAbstractDTO.setZdbID(publication.getZdbID());
        publicationAbstractDTO.setTitle(publication.getTitle());
        publicationAbstractDTO.setAuthors(publication.getAuthors());
        publicationAbstractDTO.setDoi(publication.getDoi());
        publicationAbstractDTO.setAbstractText(publication.getAbstractText());
        publicationAbstractDTO.setAccession(publication.getAccessionNumber());
        publicationAbstractDTO.setCitation(publication.getCitation());
        publicationAbstractDTO.setMiniRef(publication.getShortAuthorList());


        return publicationAbstractDTO;
    }

    public NoteDTO addCuratorNote(NoteDTO noteDTO) {
        logger.info("adding curator note: " + noteDTO.getDataZdbID() + " - " + noteDTO.getNoteData());
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        Marker marker = markerRepository.getMarkerByID(noteDTO.getDataZdbID());
        Person person = Person.getCurrentSecurityUser();
        DataNote dataNote = markerRepository.addMarkerDataNote(marker, noteDTO.getNoteData(), person);
        InfrastructureService.insertUpdate(marker, "added curator note: " + noteDTO);
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
                dataNote.setNote(noteDTO.getNoteData());
                InfrastructureService.insertUpdate(marker, "updated note: " + noteDTO);
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
        String newNote = noteDTO.getNoteData();
        if (!StringUtils.equals(newNote, oldNote)) {
            marker.setPublicComments(noteDTO.getNoteData());
            InfrastructureService.insertUpdate(marker, "Public Note", oldNote, newNote);
            HibernateUtil.currentSession().update(marker);
            HibernateUtil.flushAndCommitCurrentSession();
        }
    }

    // self-attribution

    public void addMarkerAttribution(String markerZdbID, String pubZdbID) {
        HibernateUtil.createTransaction();
        Marker marker = markerRepository.getMarkerByID(markerZdbID);
        RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(marker.getZdbID(), pubZdbID);
        InfrastructureService.insertUpdate(marker, "added direct attribution: " + pubZdbID);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    public void removeMarkerAttribution(String markerZdbID, String pubZdbID) {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        Marker marker = markerRepository.getMarkerByID(markerZdbID);
        RepositoryFactory.getInfrastructureRepository().deleteRecordAttribution(marker.getZdbID(), pubZdbID);
        InfrastructureService.insertUpdate(marker, "removed direct attribution: " + pubZdbID);
        HibernateUtil.flushAndCommitCurrentSession();
    }


    //

    /**
     * Add alias attribute.  If there is failure, we want the exception to propagate back
     * to the client.
     *
     * @param relatedEntityDTO bindle of data with data id, name & pub for attribution
     */
    public RelatedEntityDTO addDataAliasRelatedEntity(RelatedEntityDTO relatedEntityDTO) {

        Marker marker = markerRepository.getMarkerByID(relatedEntityDTO.getDataZdbID());
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        Publication publication = null;
        if (StringUtils.isNotEmpty(relatedEntityDTO.getPublicationZdbID())) {
            publication = (Publication) session.get(Publication.class, relatedEntityDTO.getPublicationZdbID());
        }
        DataAlias dataAlias = markerRepository.getSpecificDataAlias(marker, relatedEntityDTO.getName());
        if(dataAlias==null){
            markerRepository.addMarkerAlias(marker, relatedEntityDTO.getName(), publication);
        }
        else{
            if(false==publication.getZdbID().equals(dataAlias.getSinglePublication())){
                return addDataAliasAttribution(relatedEntityDTO) ;
            }
        }

        dataAlias = markerRepository.getSpecificDataAlias(marker, relatedEntityDTO.getName());
        //no need to handle the updates table here, since the repository method takes care of it

        HibernateUtil.flushAndCommitCurrentSession();
        relatedEntityDTO.setName(dataAlias.getAlias());
        relatedEntityDTO.setDataZdbID(marker.getZdbID());
        return relatedEntityDTO;
    }

    /**
     * @param relatedEntityDTO bindle of data with data id, name & pub for attribution
     */
    public RelatedEntityDTO addDataAliasAttribution(RelatedEntityDTO relatedEntityDTO) {
        logger.debug(relatedEntityDTO.toString());
        String aliasName = relatedEntityDTO.getName();

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
        String aliasName = relatedEntityDTO.getName();
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

            List<DBLinkDTO> dbLinkDTOs = DTOService.createDBLinkDTOsFromMarkerDBLink((MarkerDBLink) sequence.getDbLink());
            DBLinkDTO dbLinkDTO = dbLinkDTOs.get(0);
            dbLinkDTO.setPublicationZdbID(pubZdbID);

            InfrastructureService.insertUpdate(marker, "Added protein sequence: '" +
                    sequence.getDbLink().getAccessionNumber() + "' with attribution: '" + referenceZdbID + "'");

            //accession numbers end up in the name tables, so a regen will make the accession
            //search in markerselect work for this new sequence.
            markerRepository.runMarkerNameFastSearchUpdate(marker);

            transaction.commit();
            return dbLinkDTO;
        }
        catch (Exception e) {
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

            List<DBLinkDTO> dbLinkDTOs = DTOService.createDBLinkDTOsFromMarkerDBLink((MarkerDBLink) sequence.getDbLink());
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
        }
        catch (Exception e) {
            logger.error("Failure to add internal protein sequence", e);
            transaction.rollback();
            throw new BlastDatabaseAccessException("Failed to add nucleotide sequence.", e);
        }
    }

    public List<DBLinkDTO> getMarkerDBLinksForAccession(String accession) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(MarkerDBLink.class);
        criteria.add(Restrictions.eq("accessionNumber", accession));
        List<MarkerDBLink> dbLinks = criteria.list();
        List<DBLinkDTO> dbLinkDTOs = new ArrayList<DBLinkDTO>();

        for (MarkerDBLink markerDBLink : dbLinks) {
            DBLinkDTO dbLinkDTO = new DBLinkDTO();
            dbLinkDTO.setDbLinkZdbID(markerDBLink.getZdbID());
            dbLinkDTO.setLength(markerDBLink.getLength());
            dbLinkDTO.setDataZdbID(markerDBLink.getMarker().getZdbID());
            dbLinkDTO.setName(markerDBLink.getAccessionNumber());
            if (markerDBLink.getPublicationCount() == 1) {
                dbLinkDTO.setPublicationZdbID(markerDBLink.getSinglePublication().getZdbID());
            }

            List<String> recordAttributions = new ArrayList<String>();
            Set<PublicationAttribution> publicationAttributions = markerDBLink.getPublications();
            for (PublicationAttribution publicationAttribution : publicationAttributions) {
                recordAttributions.add(publicationAttribution.getPublication().getZdbID());
            }
            dbLinkDTO.setRecordAttributions(recordAttributions);


            ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO();
            ReferenceDatabase referenceDatabase = markerDBLink.getReferenceDatabase();
            referenceDatabaseDTO.setName(referenceDatabase.getForeignDB().getDbName().toString());
            referenceDatabaseDTO.setType(referenceDatabase.getForeignDBDataType().getDataType().toString());
            referenceDatabaseDTO.setSuperType(referenceDatabase.getForeignDBDataType().getSuperType().toString());
            referenceDatabaseDTO.setZdbID(referenceDatabase.getZdbID());
            dbLinkDTO.setReferenceDatabaseDTO(referenceDatabaseDTO);
            dbLinkDTOs.add(dbLinkDTO);
        }
        return dbLinkDTOs;
    }


    // MarkerDBLink methods

    public DBLinkDTO getDBLink(String zdbID) {
        Session session = HibernateUtil.currentSession();
        MarkerDBLink markerDBLink = (MarkerDBLink) session.get(MarkerDBLink.class, zdbID);

        DBLinkDTO DBLinkDTO = new DBLinkDTO();
        DBLinkDTO.setDbLinkZdbID(markerDBLink.getZdbID());
        DBLinkDTO.setLength(markerDBLink.getLength());
        DBLinkDTO.setDataZdbID(markerDBLink.getMarker().getZdbID());
        DBLinkDTO.setName(markerDBLink.getAccessionNumber());
        if (markerDBLink.getPublicationCount() == 1) {
            DBLinkDTO.setPublicationZdbID(markerDBLink.getSinglePublication().getZdbID());
        }

        List<String> recordAttributions = new ArrayList<String>();
        Set<PublicationAttribution> publicationAttributions = markerDBLink.getPublications();
        for (PublicationAttribution publicationAttribution : publicationAttributions) {
            recordAttributions.add(publicationAttribution.getPublication().getZdbID());
        }
        DBLinkDTO.setRecordAttributions(recordAttributions);


        ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO();
        ReferenceDatabase referenceDatabase = markerDBLink.getReferenceDatabase();
        referenceDatabaseDTO.setName(referenceDatabase.getForeignDB().getDbName().toString());
        referenceDatabaseDTO.setType(referenceDatabase.getForeignDBDataType().getDataType().toString());
        referenceDatabaseDTO.setSuperType(referenceDatabase.getForeignDBDataType().getSuperType().toString());
        referenceDatabaseDTO.setZdbID(referenceDatabase.getZdbID());
        DBLinkDTO.setReferenceDatabaseDTO(referenceDatabaseDTO);

        return DBLinkDTO;
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

        DBLink dbLink = DTOService.createDBLinkFromDBLinkDTO(dbLinkDTO);

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
        dbLinkDTO.setDbLinkZdbID(dbLink.getZdbID());

        // set publicationattribution
        if (StringUtils.isNotEmpty(dbLinkDTO.getPublicationZdbID())) {
            Set<PublicationAttribution> publications = new HashSet<PublicationAttribution>();
            PublicationAttribution publicationAttribution =
                    RepositoryFactory.getInfrastructureRepository().insertPublicAttribution(dbLinkDTO.getDbLinkZdbID(),
                            dbLinkDTO.getPublicationZdbID());
            publications.add(publicationAttribution);
            dbLink.setPublications(publications);
        }

        session.update(dbLink);
        session.flush();

        Marker marker = (Marker) session.get(Marker.class, dbLink.getDataZdbID());

        List<DBLinkDTO> dbLinkDTOs = DTOService.createDBLinkDTOsFromDBLink(dbLink, marker.getZdbID(), marker.getAbbreviation());
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
        String dblinkZdbID = sequenceDTO.getDbLinkZdbID();
        String dblinkDataZdbID = sequenceDTO.getDataZdbID();

        DBLink dbLink = null;
        if (dblinkZdbID == null && sequenceDTO.getDataName() != null && sequenceDTO.getDataZdbID() != null) {
            dbLink = RepositoryFactory.getSequenceRepository().getDBLinkByAlternateKey(sequenceDTO.getName(),
                    dblinkDataZdbID, DTOService.getReferenceDatabase(sequenceDTO.getReferenceDatabaseDTO()));
            if (dbLink == null) {
                throw new RuntimeException("Faild to get a dblink for dblinkdto: " + sequenceDTO);
            }
            dblinkZdbID = dbLink.getZdbID();
            sequenceDTO.setDbLinkZdbID(dblinkZdbID);
        }
        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        List<PublicationAttribution> publicationAttributions = infrastructureRepository.getPublicationAttributions(dblinkZdbID);
        if (publicationAttributions.size() > 0) {
            RepositoryFactory.getInfrastructureRepository().insertPublicAttribution(dblinkZdbID, sequenceDTO.getPublicationZdbID());
            sequenceDTO.setAttributionType(RecordAttribution.SourceType.STANDARD.toString());
        } else {
            RepositoryFactory.getInfrastructureRepository().insertPublicAttribution(dblinkZdbID, sequenceDTO.getPublicationZdbID(), RecordAttribution.SourceType.FIRST_CURATED_SEQUENCE_PUB);
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
        String dblinkZdbID = dbLinkDTO.getDbLinkZdbID();
        String dblinkDataZdbID = dbLinkDTO.getDataZdbID();

        DBLink dbLink = null;
        if (dblinkZdbID != null) {
            dbLink = RepositoryFactory.getSequenceRepository().getDBLinkByID(dblinkZdbID);
        } else if (dbLinkDTO.getDataName() != null && dbLinkDTO.getDataZdbID() != null) {
            dbLink = RepositoryFactory.getSequenceRepository().getDBLinkByAlternateKey(dbLinkDTO.getName(),
                    dblinkDataZdbID, DTOService.getReferenceDatabase(dbLinkDTO.getReferenceDatabaseDTO()));
            if (dbLink == null) {
                throw new RuntimeException("Faild to get a dblink for dblinkdto: " + dbLinkDTO);
            }
            dblinkZdbID = dbLink.getZdbID();
            dbLinkDTO.setDbLinkZdbID(dblinkZdbID);
        }

        RepositoryFactory.getInfrastructureRepository().insertPublicAttribution(dblinkZdbID, dbLinkDTO.getPublicationZdbID());
        session.flush();

        DTOService.createDBLinkDTOFromDBLinkForPub(dbLink, dbLinkDTO.getDataZdbID(), dbLinkDTO.getDataName(), dbLinkDTO.getPublicationZdbID());
        // if it fails, it will automatically roll-back and automatically throws exception up
        transaction.commit();
        return dbLinkDTO;
    }

    public DBLinkDTO removeDBLink(DBLinkDTO dbLinkDTO) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        DBLink dbLink = (DBLink) session.get(DBLink.class, dbLinkDTO.getDbLinkZdbID());
        if (dbLink == null && dbLinkDTO.getDataZdbID() != null && dbLinkDTO.getName() != null) {
            dbLink = RepositoryFactory.getSequenceRepository().
                    getDBLink(dbLinkDTO.getDataZdbID(),
                            dbLinkDTO.getName(), dbLinkDTO.getReferenceDatabaseDTO().getName());
        }
        if (dbLink == null) {
            throw new RuntimeException("Failed to find dblink: " + dbLinkDTO.toString());
        }

        // remove attributions
        RepositoryFactory.getInfrastructureRepository().deleteRecordAttributionsForData(dbLink.getZdbID());

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
        DBLink dbLink = (DBLink) session.get(DBLink.class, dbLinkDTO.getDbLinkZdbID());
//                RepositoryFactory.getSequenceRepository().
//                        getDBLink(dbLinkDTO.getDbLinkZdbID(),
//                                dbLinkDTO.getName(), dbLinkDTO.getReferenceDatabaseDTO().getName());

        dbLinkDTO.setDbLinkZdbID(dbLink.getZdbID());
        RepositoryFactory.getInfrastructureRepository().deleteRecordAttribution(dbLink.getZdbID(), dbLinkDTO.getPublicationZdbID());
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
        DBLink dbLink = (DBLink) session.get(DBLink.class, dbLinkDTO.getDbLinkZdbID());

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

        return DTOService.createDBLinkDTOFromDBLinkForPub(dbLink, marker.getZdbID(), marker.getName(), pub.getZdbID());
    }

    public MarkerDTO getMarkerForName(String name) {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(name);
        if (marker == null) {
            return null;
        }

        return DTOService.createMarkerDTOFromMarker(marker);
    }


    public MarkerDTO getGeneForZdbID(String zdbID) {
        Marker marker = markerRepository.getMarkerByID(zdbID);
        MarkerDTO geneDTO = new MarkerDTO();

        // get simple attributes
        geneDTO.setZdbID(marker.getZdbID());
        geneDTO.setName(marker.getName());
        geneDTO.setMarkerType(marker.getMarkerType().getType().name());


        geneDTO.setSuppliers(DTOService.getSuppliers(marker));

        geneDTO.setRecordAttributions(DTOService.getDirectAttributions(marker));

        geneDTO.setCuratorNotes(DTOService.getCuratorNotes(marker));
        geneDTO.setPublicNote(DTOService.getPublicNote(marker));

        geneDTO.setAliasAttributes(DTOService.getAliases(marker));

        // get related genes
        // todo: this is the wrong method for this
        geneDTO.setRelatedGeneAttributes(DTOService.getRelatedGenes(marker));

        // todo: this is the wrong method for this
        geneDTO.setSupportingSequenceLinks(DTOService.getSupportingSequences(marker));

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

        return DTOService.createMarkerDTOFromMarker(marker);
    }

    // get all existing supplier names

    public List<String> getAllSupplierNames() {
        if (suppliers == null) {
            Session session = HibernateUtil.currentSession();

            String hqlLab = " select ms.organization.name from MarkerSupplier ms group by ms.organization.name order by ms.organization.name ";
            Query queryLab = session.createQuery(hqlLab);

            suppliers = (List<String>) queryLab.list();
        }
        return suppliers;
    }

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

    public MarkerDTO addRelatedMarker(MarkerDTO markerDTO) throws TermNotFoundException {
        HibernateUtil.createTransaction();

        Marker firstMarker = markerRepository.getMarkerByID(markerDTO.getZdbID());
        Marker secondMarker = markerRepository.getMarkerByAbbreviation(markerDTO.getName());

        if (secondMarker == null || firstMarker == null) {
            throw new TermNotFoundException(markerDTO.getName(), markerDTO.getMarkerRelationshipType());
        }
        if (true == markerDTO.isZdbIDThenAbbrev()) {
            MarkerService.addMarkerRelationship(firstMarker, secondMarker, markerDTO.getPublicationZdbID(), MarkerRelationship.Type.getType(markerDTO.getMarkerRelationshipType()));
        } else {
            MarkerService.addMarkerRelationship(secondMarker, firstMarker, markerDTO.getPublicationZdbID(), MarkerRelationship.Type.getType(markerDTO.getMarkerRelationshipType()));
        }

        logger.debug("addMarkerRelationship, first marker: " + firstMarker.getAbbreviation()
                + " second marker: " + secondMarker.getAbbreviation());

        HibernateUtil.flushAndCommitCurrentSession();

        // always return the abbreviation
        MarkerDTO dto = DTOService.createMarkerDTOFromMarker(secondMarker);
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

        MarkerDTO dto = DTOService.createMarkerDTOFromMarker(secondMarker);
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
        AntibodyExternalNote antibodyExternalNote = markerRepository.addAntibodyExternalNote(antibody,noteDTO.getNoteData(),noteDTO.getPublicationZdbID());
        noteDTO.setZdbID(antibodyExternalNote.getZdbID());
        infrastructureRepository.insertUpdatesTable(antibody,"notes","",Person.getCurrentSecurityUser());
        HibernateUtil.flushAndCommitCurrentSession();
        return noteDTO;
    }

    @Override
    public void editExternalNote(NoteDTO noteDTO) {
        HibernateUtil.createTransaction();
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(noteDTO.getDataZdbID());
        markerRepository.editAntibodyExternalNote(noteDTO.getZdbID(), noteDTO.getNoteData());
        infrastructureRepository.insertUpdatesTable(antibody, "updated notes", "", Person.getCurrentSecurityUser());
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
                infrastructureRepository.insertUpdatesTable(antibody,"deleted notes", "",Person.getCurrentSecurityUser());
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
//            clone.setName(markerDTO.getName());

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
        if(dblinkGeneAddReferenceDatabases.size()>0){
            return dblinkGeneAddReferenceDatabases ;
        }
        
        dblinkGeneAddReferenceDatabases.clear();
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerZdbID);
        List<ReferenceDatabase> referenceDatabases = new ArrayList<ReferenceDatabase>();
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM_AND_EFG)) {
            referenceDatabases.add(RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.GENOMIC,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.ZEBRAFISH
            ));

            referenceDatabases.add(RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.RNA,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.ZEBRAFISH
            ));

        }

        if (false==referenceDatabases.isEmpty()) {
            dblinkGeneAddReferenceDatabases.addAll(DTOService.convertReferenceDTOs(referenceDatabases));
        }

        return dblinkGeneAddReferenceDatabases;
    }
}
