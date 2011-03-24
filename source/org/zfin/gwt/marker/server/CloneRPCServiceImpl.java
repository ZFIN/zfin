package org.zfin.gwt.marker.server;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.marker.ui.CloneRPCService;
import org.zfin.gwt.root.dto.CloneDTO;
import org.zfin.gwt.root.dto.CloneTypesDTO;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.server.DTOMarkerService;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.infrastructure.InfrastructureService;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.ProbeLibrary;
import org.zfin.marker.Vector;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class CloneRPCServiceImpl extends ZfinRemoteServiceServlet implements CloneRPCService {

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

        cloneDTO.setSuppliers(MarkerService.getSuppliers(clone));

        // get direct attributions
        cloneDTO.setRecordAttributions(MarkerService.getDirectAttributions(clone));

        // get notes
        cloneDTO.setCuratorNotes(DTOMarkerService.getCuratorNoteDTOs(clone));

        cloneDTO.setPublicNote(DTOMarkerService.getPublicNoteDTO(clone));

        // get alias's
        cloneDTO.setAliasAttributes(DTOMarkerService.getMarkerAliasDTOs(clone));

        // get related genes
        cloneDTO.setRelatedGeneAttributes(DTOMarkerService.getRelatedGenesMarkerDTOs(clone));

        // get sequences
        cloneDTO.setSupportingSequenceLinks(DTOMarkerService.getSupportingSequenceDTOs(clone));

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
            DTOMarkerService.insertMarkerUpdate(clone, "digest", clone.getDigest(), cloneDTO.getDigest());
            clone.setDigest(cloneDTO.getDigest());

            DTOMarkerService.insertMarkerUpdate(clone, "PCR Amplification", clone.getPcrAmplification(), cloneDTO.getPcrAmplification());
            clone.setPcrAmplification(cloneDTO.getPcrAmplification());

            DTOMarkerService.insertMarkerUpdate(clone, "Polymerase Name", clone.getPolymeraseName(), cloneDTO.getPolymerase());
            clone.setPolymeraseName(cloneDTO.getPolymerase());

            DTOMarkerService.insertMarkerUpdate(clone, "Insert Size", clone.getInsertSize(), cloneDTO.getInsertSize());
            clone.setInsertSize(cloneDTO.getInsertSize());

            DTOMarkerService.insertMarkerUpdate(clone, "Clone Comments", clone.getCloneComments(), cloneDTO.getCloneComments());
            clone.setCloneComments(cloneDTO.getCloneComments());

            DTOMarkerService.insertMarkerUpdate(clone, "Rating", clone.getRating(), cloneDTO.getRating());
            clone.setRating(cloneDTO.getRating());

            DTOMarkerService.insertMarkerUpdate(clone, "Cloning Site", clone.getCloningSite(), cloneDTO.getCloningSite());
            clone.setCloningSite(cloneDTO.getCloningSite());

            // set vector
            String cloneVectorName = (clone.getVector() == null ? null : clone.getVector().getName());
            DTOMarkerService.insertMarkerUpdate(clone, "Vector Name", cloneVectorName, cloneDTO.getVectorName());
            if (cloneDTO.getVectorName() != null) {
                Vector vector = (Vector) session.get(Vector.class, cloneDTO.getVectorName());
                clone.setVector(vector);
            } else {
                clone.setVector(null);
            }

            String cloneProbeLibraryName = (clone.getProbeLibrary() == null ? null : clone.getProbeLibrary().getName());
            DTOMarkerService.insertMarkerUpdate(clone, "Probe Library", cloneProbeLibraryName, cloneDTO.getProbeLibraryName());
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
            dblinkCloneAddReferenceDatabases.add(DTOConversionService.convertToReferenceDatabaseDTO(referenceDatabase));
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
        if (false == StringUtils.equals(cloneProblemTypeString, cloneDTO.getProblemType())) {
            DTOMarkerService.insertMarkerUpdate(clone, "Problem Type", cloneProblemTypeString, cloneDTO.getProblemType());
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