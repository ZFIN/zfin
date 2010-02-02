package org.zfin.gwt.marker.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.marker.ui.CloneRPCService;
import org.zfin.gwt.marker.ui.NoteBox;
import org.zfin.gwt.root.dto.*;
import org.zfin.infrastructure.*;
import org.zfin.marker.*;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.Species;
import org.zfin.people.MarkerSupplier;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 */
public class CloneRPCServiceImpl extends RemoteServiceServlet implements CloneRPCService {

    private transient Logger logger = Logger.getLogger(CloneRPCServiceImpl.class);
    private final MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    // cached types
    private transient CloneTypesDTO cloneTypesDTO = null;
    private transient List<ReferenceDatabaseDTO> dblinkCloneAddReferenceDatabases = new ArrayList<ReferenceDatabaseDTO>();

    public CloneDTO getCloneForZdbID(String zdbID) {
        Clone clone = markerRepository.getCloneById(zdbID);
        CloneDTO cloneDTO = new CloneDTO();

        // get simple attributes
        cloneDTO.setZdbID(clone.getZdbID());
        cloneDTO.setName(clone.getName());
        cloneDTO.setMarkerType(clone.getMarkerType().getType().name());

        // get clone table data
        cloneDTO.setRating(clone.getRating());
        if (clone.getProblem() != null) {
            cloneDTO.setProblemType(clone.getProblem().toString());
        }
        cloneDTO.setCloneComments(clone.getCloneComments());
        cloneDTO.setCloningSite(clone.getCloningSite());
        cloneDTO.setDigest(clone.getDigest());
        cloneDTO.setPolymerase(clone.getPolymeraseName());
        cloneDTO.setInsertSize(clone.getInsertSize());
        cloneDTO.setPcrAmplification(clone.getPcrAmplification());

        // set clone vector properties
        Vector cloneVector = clone.getVector();
        if (cloneVector != null) {
            cloneDTO.setVectorName(cloneVector.getName());
        }

        // set probe library types
        ProbeLibrary probeLibrary = clone.getProbeLibrary();
        if (probeLibrary != null) {
            cloneDTO.setProbeLibraryName(probeLibrary.getName());
        }


        Set<MarkerSupplier> markerSuppliers = clone.getSuppliers();
        List<String> supplierList = new ArrayList<String>();
        for (MarkerSupplier markerSupplier : markerSuppliers) {
            supplierList.add(markerSupplier.getOrganization().getName());
        }
        cloneDTO.setSuppliers(supplierList);


        // get direct attributions
        ActiveData activeData = new ActiveData();
        activeData.setZdbID(zdbID);
        List<RecordAttribution> recordAttributions = RepositoryFactory.getInfrastructureRepository().getRecordAttributions(activeData);
        List<String> attributions = new ArrayList<String>();
        for (RecordAttribution recordAttribution : recordAttributions) {
            attributions.add(recordAttribution.getSourceZdbID());
        }
        cloneDTO.setRecordAttributions(attributions);

        // get notes
        List<NoteDTO> curatorNotes = new ArrayList<NoteDTO>();
        Set<DataNote> dataNotes = clone.getDataNotes();
        for (DataNote dataNote : dataNotes) {
            NoteDTO noteDTO = new NoteDTO();
            noteDTO.setNoteData(dataNote.getNote());
            noteDTO.setZdbID(dataNote.getZdbID());
//            noteDTO.setDataZdbID(dataNote.getDataZdbID());
            noteDTO.setDataZdbID(clone.getZdbID());
            noteDTO.setEditMode(NoteBox.EditMode.PRIVATE.name());
            curatorNotes.add(noteDTO);
        }
        cloneDTO.setCuratorNotes(curatorNotes);

        NoteDTO publicNoteDTO = new NoteDTO();
        publicNoteDTO.setNoteData(clone.getPublicComments());
        publicNoteDTO.setZdbID(clone.getZdbID());
        publicNoteDTO.setDataZdbID(clone.getZdbID());
        publicNoteDTO.setEditMode(NoteBox.EditMode.PUBLIC.name());
        cloneDTO.setPublicNote(publicNoteDTO);

        // get alias's
        Set<MarkerAlias> aliases = clone.getAliases();
        List<RelatedEntityDTO> aliasRelatedEntities = new ArrayList<RelatedEntityDTO>();
        if (aliases != null) {
            for (MarkerAlias alias : aliases) {
                Set<PublicationAttribution> publicationAttributions = alias.getPublications();
                aliasRelatedEntities.addAll(DTOService.createRelatedEntitiesForPublications(clone.getZdbID(), alias.getAlias(), publicationAttributions));
            }
        }
        cloneDTO.setAliasAttributes(aliasRelatedEntities);

        // get related genes
        Set<MarkerRelationship> markerRelationships = clone.getFirstMarkerRelationships();
        logger.debug("# of marker relationships: " + markerRelationships.size());
        List<MarkerDTO> relatedGenes = new ArrayList<MarkerDTO>();
        for (MarkerRelationship markerRelationship : markerRelationships) {
            if (
                    markerRelationship.getSecondMarker().isInTypeGroup(Marker.TypeGroup.GENE)
                // todo: should use a different type
//                  &&
//                   markerRelationship.getType().equals(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT)
                    ) {
                Marker gene = markerRelationship.getSecondMarker();
//                relatedGenes.addAll(DTOHelper.createAttributesForPublication(gene.getAbbreviation(),markerRelationship.getPublications())) ;
                relatedGenes.addAll(DTOService.createLinksForPublication(DTOService.createMarkerDTOFromMarker(gene), markerRelationship.getPublications()));
            }
        }
        logger.debug("# of related genes: " + relatedGenes.size());

        cloneDTO.setRelatedGeneAttributes(relatedGenes);

        // get sequences
        List<ReferenceDatabase> referenceDatabases = RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(
                DisplayGroup.GroupName.DBLINK_ADDING_ON_CLONE_EDIT);
        List<MarkerDBLink> dbLinks = RepositoryFactory.getSequenceRepository().getDBLinksForMarker(clone, (ReferenceDatabase[]) referenceDatabases.toArray(new ReferenceDatabase[referenceDatabases.size()]));
        List<DBLinkDTO> dbLinkDTOList = new ArrayList<DBLinkDTO>();
        for (MarkerDBLink markerDBLink : dbLinks) {
            DBLinkDTO dbLinkDTO = new DBLinkDTO();
            dbLinkDTO.setDataZdbID(markerDBLink.getDataZdbID());
            dbLinkDTO.setDbLinkZdbID(markerDBLink.getZdbID());
            dbLinkDTO.setName(markerDBLink.getAccessionNumber());
            Publication publication = markerDBLink.getSinglePublication();
            if (publication != null) {
                dbLinkDTO.setPublicationZdbID(publication.getZdbID());
            }
            dbLinkDTO.setLength(markerDBLink.getLength());

            ReferenceDatabase referenceDatabase = markerDBLink.getReferenceDatabase();
            ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO();
            referenceDatabaseDTO.setName(referenceDatabase.getForeignDB().getDbName().toString());
            referenceDatabaseDTO.setType(referenceDatabase.getForeignDBDataType().getDataType().toString());
            referenceDatabaseDTO.setSuperType(referenceDatabase.getForeignDBDataType().getSuperType().toString());
            referenceDatabaseDTO.setZdbID(referenceDatabase.getZdbID());

            dbLinkDTO.setReferenceDatabaseDTO(referenceDatabaseDTO);
            dbLinkDTOList.addAll(DTOService.createDBLinkDTOsFromMarkerDBLink(markerDBLink));
//            markerDbLinkDTOList.add(markerDbLinkDTO);
        }
        cloneDTO.setSupportingSequenceLinks(dbLinkDTOList);

        Clone.ProblemType[] problemTypeEnums = Clone.ProblemType.values();
        List<String> problemTypes = new ArrayList<String>();
        for (Clone.ProblemType problemTypeEnum : problemTypeEnums) {
            problemTypes.add(problemTypeEnum.toString());
        }
        cloneDTO.setProblemTypes(problemTypes);


        return cloneDTO;
    }

    public CloneDTO updateCloneData(CloneDTO cloneDTO) {
        Clone clone = markerRepository.getCloneById(cloneDTO.getZdbID());
        logger.info("got clone: " + clone.getZdbID());

        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        try {
            DTOService.handleUpdatesTable(clone, "digest", clone.getDigest(), cloneDTO.getDigest());
            clone.setDigest(cloneDTO.getDigest());

            DTOService.handleUpdatesTable(clone, "PCR Amplification", clone.getPcrAmplification(), cloneDTO.getPcrAmplification());
            clone.setPcrAmplification(cloneDTO.getPcrAmplification());

            DTOService.handleUpdatesTable(clone, "Polymerase Name", clone.getPolymeraseName(), cloneDTO.getPolymerase());
            clone.setPolymeraseName(cloneDTO.getPolymerase());

            DTOService.handleUpdatesTable(clone, "Insert Size", clone.getInsertSize(), cloneDTO.getInsertSize());
            clone.setInsertSize(cloneDTO.getInsertSize());

            DTOService.handleUpdatesTable(clone, "Clone Comments", clone.getCloneComments(), cloneDTO.getCloneComments());
            clone.setCloneComments(cloneDTO.getCloneComments());

            DTOService.handleUpdatesTable(clone, "Rating", clone.getRating(), cloneDTO.getRating());
            clone.setRating(cloneDTO.getRating());

            DTOService.handleUpdatesTable(clone, "Cloning Site", clone.getCloningSite(), cloneDTO.getCloningSite());
            clone.setCloningSite(cloneDTO.getCloningSite());

            // set vector
            String cloneVectorName = (clone.getVector() == null ? null : clone.getVector().getName());
            DTOService.handleUpdatesTable(clone, "Vector Name", cloneVectorName, cloneDTO.getVectorName());
            if (cloneDTO.getVectorName() != null) {
                Vector vector = (Vector) session.get(Vector.class, cloneDTO.getVectorName());
                clone.setVector(vector);
            } else {
                clone.setVector(null);
            }

            String cloneProbeLibraryName = (clone.getProbeLibrary() == null ? null : clone.getProbeLibrary().getName());
            DTOService.handleUpdatesTable(clone, "Probe Library", cloneProbeLibraryName, cloneDTO.getProbeLibraryName());
            Criteria criteria = session.createCriteria(ProbeLibrary.class);
            criteria.add(Restrictions.eq("name", cloneDTO.getProbeLibraryName()));
            ProbeLibrary probeLibrary = (ProbeLibrary) criteria.uniqueResult();
            clone.setProbeLibrary(probeLibrary);


            session.update(clone);
            session.flush();
            logger.info("updated clone: " + clone);

            transaction.commit();
        }
        catch (Exception e) {
            transaction.rollback();
            logger.error(e);
        }

//        return cloneDTO;
        return getCloneForZdbID(cloneDTO.getZdbID());
    }

    // get clone types

    public CloneTypesDTO getCloneTypes() {
        if (cloneTypesDTO != null) {
            return cloneTypesDTO;
        }
        cloneTypesDTO = new CloneTypesDTO();

        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        // set polymerase names
        cloneTypesDTO.setPolymeraseNames(markerRepository.getPolymeraseNames());

        // set vector names
        cloneTypesDTO.setVectorNames(markerRepository.getVectorNames());

        // set probeLibraries
        cloneTypesDTO.setProbeLibraries(markerRepository.getProbeLibraryNames());

        // set digests
        cloneTypesDTO.setDigests(markerRepository.getDigests());

        // set digests
        cloneTypesDTO.setCloneSites(markerRepository.getCloneSites());

        return cloneTypesDTO;
    }

    public List<ReferenceDatabaseDTO> getCloneDBLinkAddReferenceDatabases(String markerZdbID) {
        dblinkCloneAddReferenceDatabases.clear();
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerZdbID);
        ReferenceDatabase referenceDatabase = null;
        if (marker.isInTypeGroup(Marker.TypeGroup.CLONE)) {
            referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.GENOMIC,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.ZEBRAFISH
            );
        } else if (marker.isInTypeGroup(Marker.TypeGroup.CDNA_AND_EST)) {
            referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.RNA,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.ZEBRAFISH
            );
        }

        if (referenceDatabase != null) {
            dblinkCloneAddReferenceDatabases.add(DTOService.convertReferenceDTO(referenceDatabase));
        }

        return dblinkCloneAddReferenceDatabases;
    }

    // clone update
    public void updateCloneHeaders(CloneDTO cloneDTO) {
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        HibernateUtil.createTransaction();
        Clone clone = (Clone) session.get(Clone.class, cloneDTO.getZdbID());

        // set name

        if (!clone.getName().equals(cloneDTO.getName())) {
            String oldName = clone.getName();

            clone.setAbbreviation(cloneDTO.getName());
            clone.setName(cloneDTO.getName());

            InfrastructureService.insertUpdate(clone, "Name", oldName, clone.getName());
            //run regen script
            markerRepository.runMarkerNameFastSearchUpdate(clone);
        }


        String cloneProblemTypeString = (clone.getProblem() == null ? null : clone.getProblem().toString());
        if(false==StringUtils.equals(cloneProblemTypeString,cloneDTO.getProblemType())){
            DTOService.handleUpdatesTable(clone, "Problem Type", cloneProblemTypeString, cloneDTO.getProblemType());
            if (cloneDTO.getProblemType() == null) {
                clone.setProblem(null);
            } else {
                clone.setProblem(Clone.ProblemType.getProblemType(cloneDTO.getProblemType()));
            }
        }


        session.update(clone);
        session.flush();

        // if it fails, it will automatically roll-back and automatically throws exception up
        transaction.commit();

    }



}