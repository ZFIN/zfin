package org.zfin.gwt.curation.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.zfin.ExternalNote;
import org.zfin.Species;
import org.zfin.feature.*;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.FeatureService;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.curation.ui.FeatureRPCService;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.gwt.root.ui.ValidationException;
import org.zfin.gwt.root.util.NullpointerException;
import org.zfin.infrastructure.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.FeatureLocation;
import org.zfin.marker.Marker;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.profile.FeatureSource;
import org.zfin.profile.Organization;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;
import org.zfin.sequence.*;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.zebrashare.repository.ZebrashareRepository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.*;

public class FeatureRPCServiceImpl extends RemoteServiceServlet implements FeatureRPCService {

    private final static String MESSAGE_UNSPECIFIED_FEATURE = "An unspecified feature name must have a valid gene abbreviation.";
    private static InfrastructureRepository infrastructureRepository = getInfrastructureRepository();
    private static FeatureRepository featureRepository = getFeatureRepository();
    private static ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();
    private static OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    private static ZebrashareRepository zebrashareRepository = RepositoryFactory.getZebrashareRepository();
    private final MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private final PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private final SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
    private transient Logger logger = LogManager.getLogger(FeatureRPCServiceImpl.class);
    private List<Organization> labsOfOrigin = null;

    public FeatureDTO getFeature(String featureZdbID) {
        Feature feature = (Feature) HibernateUtil.currentSession().get(Feature.class, featureZdbID);
        return DTOConversionService.convertToFeatureDTO(feature);
    }

    private void checkDupes(FeatureDTO featureDTO) throws DuplicateEntryException {
        // retrieve
        Feature existingFeature = featureRepository.getFeatureByAbbreviation(featureDTO.getAbbreviation());

        // if there is a feature that already has this name, but it is not this feature
        if (existingFeature != null && false == existingFeature.getZdbID().equals(featureDTO.getZdbID())) {
            throw new DuplicateEntryException("Feature exists for this abbreviation: " + featureDTO.getAbbreviation());
        }


        if (featureDTO.getLabPrefix() != null && featureDTO.getLineNumber() != null) {
            existingFeature = featureRepository.getFeatureByPrefixAndLineNumber(featureDTO.getLabPrefix(), featureDTO.getLineNumber());
        }

        // if there is a feature that already has this name, but it is not this feature
        if (existingFeature != null && false == existingFeature.getZdbID().equals(featureDTO.getZdbID())) {
            throw new DuplicateEntryException("Feature exists with this prefix [" + featureDTO.getLabPrefix() + "] and line number [" + featureDTO.getLineNumber() + "] : " + existingFeature.getAbbreviation() + "->" + existingFeature.getZdbID());
        }

        // if there is a feature that already has this name, but it is not this feature
        if (existingFeature != null && false == existingFeature.getZdbID().equals(featureDTO.getZdbID())) {
            throw new DuplicateEntryException("Feature exists for this abbreviation: " + featureDTO.getAbbreviation());
        }
    }

    private void checkDupesinTrackingTable(FeatureDTO featureDTO) throws DuplicateEntryException {
        String featureInTrackingTable = featureRepository.getFeatureByAbbreviationInTrackingTable(featureDTO.getAbbreviation());


        if (featureInTrackingTable != null) {
            throw new DuplicateEntryException("Feature exists in the tracking table for this abbreviation: " + featureDTO.getAbbreviation());
        }

    }

    private void updateFeatureLocation(FeatureLocation fl, FeatureDTO dto){
        fl.setFtrChromosome(dto.getFeatureChromosome());
        fl.setFtrAssembly(dto.getFeatureAssembly());
        fl.setFtrStartLocation(dto.getFeatureStartLoc());
        fl.setFtrEndLocation(dto.getFeatureEndLoc());
        // convert code into TermID and then get GenericTerm
        fl.setFtrLocEvidence(ontologyRepository.getTermByZdbID(FeatureService.getFeatureGenomeLocationEvidenceCodeTerm(dto.getEvidence())));
        HibernateUtil.currentSession().save(fl);
        infrastructureRepository.insertPublicAttribution(fl.getZdbID(), dto.getPublicationZdbID(), RecordAttribution.SourceType.STANDARD);
    }

    /**
     * Here, we edit everything but the notes (done in-line) and the alias (also done in-line).
     *
     * @param featureDTO FeatureDTO
     * @return updated FeatureDTO
     * @throws DuplicateEntryException
     */
    public FeatureDTO editFeatureDTO(FeatureDTO featureDTO) throws DuplicateEntryException, ValidationException {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        dateFormat.setLenient(false);
        Date entryDate;

        checkDupes(featureDTO);
        validateUnspecified(featureDTO);

        Feature feature = (Feature) HibernateUtil.currentSession().get(Feature.class, featureDTO.getZdbID());
        FeatureTypeEnum oldFeatureType = feature.getType();
        String oldFtrName = feature.getName();

        String newFtrName = featureDTO.getName();
        HibernateUtil.createTransaction();
        feature.setType(featureDTO.getFeatureType());
        if (featureDTO.getFeatureType() != FeatureTypeEnum.UNSPECIFIED) {
            feature.setUnspecifiedFeature(false);
        }
        if (oldFeatureType != featureDTO.getFeatureType()) {
            RecordAttribution recordAttributions = infrastructureRepository.getRecordAttribution(feature.getZdbID(), featureDTO.getPublicationZdbID(), RecordAttribution.SourceType.FEATURE_TYPE);
            if (recordAttributions == null) {
                List<RecordAttribution> recordAttribution = infrastructureRepository.getRecordAttributionsForType(feature.getZdbID(), RecordAttribution.SourceType.FEATURE_TYPE);
                if (recordAttribution.size() != 0) {

                    infrastructureRepository.removeRecordAttributionForType(recordAttribution.get(0).getSourceZdbID(), feature.getZdbID());
                    infrastructureRepository.insertUpdatesTable(feature.getZdbID(), "Feature type attribution", oldFeatureType.name(), featureDTO.getFeatureType().toString(), recordAttribution.get(0).getSourceZdbID());
                    infrastructureRepository.insertPublicAttribution(featureDTO.getZdbID(), featureDTO.getPublicationZdbID(), RecordAttribution.SourceType.FEATURE_TYPE);
                } else {
                    infrastructureRepository.insertUpdatesTable(feature.getZdbID(), "Feature type attribution", oldFeatureType.name(), featureDTO.getFeatureType().toString(), featureDTO.getPublicationZdbID());
                    infrastructureRepository.insertPublicAttribution(featureDTO.getZdbID(), featureDTO.getPublicationZdbID(), RecordAttribution.SourceType.FEATURE_TYPE);
                }
            }
        }

        Feature existingFeature = featureRepository.getFeatureByAbbreviation(featureDTO.getAbbreviation());


        if (existingFeature == null) {
            feature.setAbbreviation(featureDTO.getAbbreviation());
        }
        feature.setName(featureDTO.getName());


        feature.setDominantFeature(featureDTO.getDominant());
        feature.setKnownInsertionSite(featureDTO.getKnownInsertionSite());
        feature.setTransgenicSuffix(featureDTO.getTransgenicSuffix());
        if (featureDTO.getLineNumber() != null) {
            feature.setLineNumber(featureDTO.getLineNumber());
        }
        if (StringUtils.isNotEmpty(featureDTO.getLabPrefix())) {
            feature.setFeaturePrefix(featureRepository.getFeatureLabPrefixID(featureDTO.getLabPrefix()));
        }
        FeatureAssay featureAssay = featureRepository.getFeatureAssay(feature);
        if (featureDTO.getMutagen() != null) {
            featureAssay.setMutagen(Mutagen.getType(featureDTO.getMutagen()));
        }
        if (featureDTO.getMutagee() != null) {
            featureAssay.setMutagee(Mutagee.getType(featureDTO.getMutagee()));
        }

        if (org.zfin.gwt.root.util.StringUtils.isNotEmpty(featureDTO.getAssemblyInfoDate())) {
            try {
                entryDate = dateFormat.parse(featureDTO.getAssemblyInfoDate());
            } catch (ParseException e) {
                throw new ValidationException("Incorrect date format, please check");
            }
            feature.setFtrAssemblyInfoDate(entryDate);
        } else {
            feature.setFtrAssemblyInfoDate(null);
        }

//update FeatureLocation information
        FeatureLocation fgl = featureRepository.getLocationByFeature(feature);
        if (fgl == null) {
            fgl = new FeatureLocation();
            fgl.setFeature(feature);
            if (StringUtils.isNotEmpty(featureDTO.getFeatureChromosome())) {
                updateFeatureLocation(fgl, featureDTO);
            }
        }
        else{
            if (featureLocationNeedsUpdate(featureDTO, fgl))
            {
                updateFeatureLocation(fgl, featureDTO);
            }
        }



        // get labs of origin for feature
        Organization existingLabOfOrigin = featureRepository.getLabByFeature(feature);
        if (featureDTO.getLabOfOrigin() != null && existingLabOfOrigin != null) {
            if (false == featureDTO.getLabOfOrigin().equals(existingLabOfOrigin.getZdbID())) {
                Organization newLabOfOrigin = profileRepository.getOrganizationByZdbID(featureDTO.getLabOfOrigin());
                featureRepository.setLabOfOriginForFeature(newLabOfOrigin, feature);
            }
        } else if (featureDTO.getLabOfOrigin() != null && existingLabOfOrigin == null) {
            featureRepository.addLabOfOriginForFeature(feature, featureDTO.getLabOfOrigin());
        } else {
            throw new ValidationException("Feature cannot be saved without lab of origin");
        }
        FeatureDnaMutationDetail detail = feature.getFeatureDnaMutationDetail();
        if (featureDTO.getDnaChangeDTO() != null) {

            if (detail == null) {
                detail = new FeatureDnaMutationDetail();
                detail.setFeature(feature);
                feature.setFeatureDnaMutationDetail(detail);
            }
            FeatureDnaMutationDetail oldDetail = detail.clone();
            String accessionNumber = featureDTO.getDnaChangeDTO().getSequenceReferenceAccessionNumber();
            if (StringUtils.isNotEmpty(accessionNumber)) {
                /*if (isValidAccession(accessionNumber, "DNA") == null) {
                    throw new ValidationException("DNA accession Number not found: " + accessionNumber);
                }*/
            } else {
                detail.setDnaSequenceReferenceAccessionNumber(null);
                detail.setReferenceDatabase(null);

            }
            DTOConversionService.updateDnaMutationDetailWithDTO(detail, featureDTO.getDnaChangeDTO());
            if (feature.getType().equals(FeatureTypeEnum.INDEL)) {
                if (detail.getNumberRemovedBasePair() == detail.getNumberAddedBasePair()) {
                    if (detail.getNumberRemovedBasePair() > 1) {
                        feature.setType(FeatureTypeEnum.MNV);
                    }
                }
            }
            if (!detail.equals(oldDetail)) {
                infrastructureRepository.insertMutationDetailAttribution(detail.getZdbID(), featureDTO.getPublicationZdbID());
            }
        } else {
            if (detail != null) {
                //This also will delete any FDMD record (in rec attribution as well)
                infrastructureRepository.deleteActiveDataByZdbID(detail.getZdbID());
            }

        }
        FeatureProteinMutationDetail proteinDetail = feature.getFeatureProteinMutationDetail();
        if (featureDTO.getProteinChangeDTO() != null) {

            if (proteinDetail == null) {
                proteinDetail = new FeatureProteinMutationDetail();
                proteinDetail.setFeature(feature);
                feature.setFeatureProteinMutationDetail(proteinDetail);
            }
            FeatureProteinMutationDetail oldDetail = proteinDetail.clone();
            String accessionNumber = featureDTO.getProteinChangeDTO().getSequenceReferenceAccessionNumber();
            if (StringUtils.isNotEmpty(accessionNumber)) {
                /*if (isValidAccession(accessionNumber, "Protein") == null) {
                    throw new ValidationException("Protein accession Number not found: " + accessionNumber);
                }*/
            } else {
                proteinDetail.setProteinSequenceReferenceAccessionNumber(null);
                proteinDetail.setReferenceDatabase(null);

            }
            DTOConversionService.updateProteinMutationDetailWithDTO(proteinDetail, featureDTO.getProteinChangeDTO());
            if (proteinDetail.getZdbID() == null) {
                HibernateUtil.currentSession().save(proteinDetail);
            }
            if (!proteinDetail.equals(oldDetail)) {
                infrastructureRepository.insertMutationDetailAttribution(proteinDetail.getZdbID(), featureDTO.getPublicationZdbID());
            }
        } else {
            // remove existing record
            if (proteinDetail != null) {
                featureRepository.deleteFeatureProteinMutationDetail(proteinDetail);
                //          infrastructureRepository.deleteMutationDetailAttribution(proteinDetail.getZdbID(), featureDTO.getPublicationZdbID());
            }

        }

        FeatureGenomicMutationDetail fgmd = feature.getFeatureGenomicMutationDetail();
        if (featureDTO.getFgmdChangeDTO() != null) {

            if (fgmd == null) {
                fgmd = new FeatureGenomicMutationDetail();
                fgmd.setFeature(feature);
                feature.setFeatureGenomicMutationDetail(fgmd);
            }
            FeatureGenomicMutationDetail oldDetail = fgmd.clone();


            DTOConversionService.updateFeatureGenomicMutationDetailWithDTO(fgmd, featureDTO.getFgmdChangeDTO());
            if (fgmd.getZdbID() == null) {
                HibernateUtil.currentSession().save(fgmd);
            }
            if (feature.getType().equals(FeatureTypeEnum.INDEL)) {
                if (feature.getFeatureGenomicMutationDetail() != null) {
                    if (feature.getFeatureGenomicMutationDetail().getFgmdSeqRef() != null) {
                        if (fgmd.getFgmdSeqRef().length() == fgmd.getFgmdSeqVar().length()) {
                            if (fgmd.getFgmdSeqRef().length() > 1) {
                                feature.setType(FeatureTypeEnum.MNV);
                            }
                        }
                    }
                }
            }

        } else {
            // remove existing record
            if (fgmd != null) {
                featureRepository.deleteFeatureGenomicMutationDetail(fgmd);
            }

        }


        Set<FeatureTranscriptMutationDetail> addTranscriptAttribution = new HashSet<>();
        if (featureDTO.getTranscriptChangeDTOSet() != null) {
            Set<FeatureTranscriptMutationDetail> detailSet = feature.getFeatureTranscriptMutationDetailSet();
            if (detailSet != null) {
                Iterator<FeatureTranscriptMutationDetail> iterator = detailSet.iterator();
                while (iterator.hasNext()) {
                    FeatureTranscriptMutationDetail ftmdDetail = iterator.next();
                    boolean exists = false;
                    for (MutationDetailTranscriptChangeDTO dto : featureDTO.getTranscriptChangeDTOSet()) {
                        if (dto.getZdbID() != null && dto.getZdbID().equals(ftmdDetail.getZdbID())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        iterator.remove();
                        infrastructureRepository.removeRecordAttributionForTranscript(featureDTO.getPublicationZdbID(), ftmdDetail.getZdbID());
                    }
                }
            }
            for (MutationDetailTranscriptChangeDTO dto : featureDTO.getTranscriptChangeDTOSet()) {
                // since we never update a transcript record we can check if the zdbID:
                // if it exists then the record exists in the database and the record has not changed.
                // if it is empty then it is a new record
                if (StringUtils.isEmpty(dto.getZdbID())) {
                    FeatureTranscriptMutationDetail newDetail = DTOConversionService.convertToTranscriptMutationDetail(null, dto);
                    newDetail.setFeature(feature);
                    feature.addMutationDetailTranscript(newDetail);
                    addTranscriptAttribution.add(newDetail);
                }
            }

        }
        try {

            featureRepository.update(feature, addTranscriptAttribution, featureDTO.getPublicationZdbID());

        } catch (ConstraintViolationException e) {

            String message = "FEATURE_TRACKING table violation: feature with name " + feature.getName() + " already exists in tracking table";
            DuplicateEntryException duplicateEntryException = new DuplicateEntryException(message);
            e.printStackTrace();
            throw duplicateEntryException;


        }
        if (!StringUtils.equals(oldFtrName, newFtrName)) {

            FeatureAlias featureAlias = mutantRepository.getSpecificDataAlias(feature, oldFtrName);
            if (featureAlias != null) {
                infrastructureRepository.insertPublicAttribution(featureAlias.getZdbID(), featureDTO.getPublicationZdbID(), RecordAttribution.SourceType.STANDARD);
            }
        }
        HibernateUtil.flushAndCommitCurrentSession();

        return getFeature(featureDTO.getZdbID());
    }

    private boolean featureLocationNeedsUpdate(FeatureDTO featureDTO, FeatureLocation fgl) {
        //null safe
        return !(
                    StringUtils.equals(featureDTO.getFeatureChromosome(), fgl.getFtrChromosome()) &&
                    StringUtils.equals(featureDTO.getFeatureAssembly(), fgl.getFtrAssembly()) &&
                    Objects.equals(featureDTO.getFeatureStartLoc(), fgl.getFtrStartLocation()) &&
                    Objects.equals(featureDTO.getFeatureEndLoc(), fgl.getFtrEndLocation())
                );
    }


    /**
     * Returns the current list of labs, always reaching the in the background.
     *
     * @return A string of available labs.
     */
    public List<OrganizationDTO> getLabsOfOriginWithPrefix() {
        if (labsOfOrigin == null) {
            labsOfOrigin = featureRepository.getLabsOfOriginWithPrefix();
            return DTOConversionService.convertToOrganizationDTO(labsOfOrigin);
        } else {
            List<Organization> returnList = Arrays.asList(new Organization[labsOfOrigin.size()]);
            java.util.Collections.copy(returnList, labsOfOrigin);
            new SupplierCacheThread().start();
            return DTOConversionService.convertToOrganizationDTO(returnList);
        }
    }

    public List<FeatureDTO> getFeaturesForPub(String publicationId) {
        List<FeatureDTO> featureDTOs = new ArrayList<>();
        List<Feature> features = featureRepository.getFeaturesByPublication(publicationId);
        if (CollectionUtils.isNotEmpty(features)) {
            for (Feature f : features) {
                featureDTOs.add(DTOConversionService.convertToFeatureDTO(f, false));
            }
        }
        Collections.sort(featureDTOs, Comparator.comparing(o -> o.getName().toLowerCase()));
        return featureDTOs;
    }

    public List<FeatureDTO> getZebrashareFeaturesForPub(String pubID) {
        List<FeatureDTO> featureDTOs = new ArrayList<>();
        List<Feature> features = zebrashareRepository.getZebraShareFeatureForPub(pubID);
        if (CollectionUtils.isNotEmpty(features)) {
            for (Feature f : features) {
                featureDTOs.add(DTOConversionService.convertToFeatureDTO(f, false));
            }
        }
        Collections.sort(featureDTOs, Comparator.comparing(o -> o.getName().toLowerCase()));
        return featureDTOs;
    }


    public List<FeatureMarkerRelationshipDTO> getFeatureMarkerRelationshipsForPub(String publicationZdbID) {

        List<FeatureMarkerRelationshipDTO> featureDTOs = new ArrayList<>();
        List<FeatureMarkerRelationship> featureMarkerRelationships = featureRepository.getFeatureRelationshipsByPublication(publicationZdbID);
        if (CollectionUtils.isNotEmpty(featureMarkerRelationships)) {
            for (FeatureMarkerRelationship featureMarkerRelationship : featureMarkerRelationships) {
                featureDTOs.add(DTOConversionService.convertToFeatureMarkerRelationshipDTO(featureMarkerRelationship));
            }
        }
        return featureDTOs;
    }

    public void addFeatureAlias(String name, String ftrZdbID, String pubZdbID) {
        Feature feature = (Feature) HibernateUtil.currentSession().get(Feature.class, ftrZdbID);
        HibernateUtil.createTransaction();
        FeatureAlias featureAlias = new FeatureAlias();
        featureAlias.setFeature(feature);
        featureAlias.setAlias(name);
        String groupName = DataAliasGroup.Group.ALIAS.toString();
        DataAliasGroup group = infrastructureRepository.getDataAliasGroupByName(groupName);
        featureAlias.setAliasGroup(group);  //default for database, hibernate tries to insert null
        HibernateUtil.currentSession().save(featureAlias);
        infrastructureRepository.insertPublicAttribution(featureAlias.getZdbID(), pubZdbID, RecordAttribution.SourceType.STANDARD);

        HibernateUtil.flushAndCommitCurrentSession();
    }

    public void addFeatureSequence(String sequence, String ftrZdbID, String pubZdbID) throws ValidationException {
        Feature feature = (Feature) HibernateUtil.currentSession().get(Feature.class, ftrZdbID);
        HibernateUtil.createTransaction();
        try {
            saveFeatureSequence(sequence, getPublicationRepository().getPublication(pubZdbID), feature);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ValidationException e) {
            HibernateUtil.rollbackTransaction();
            logger.info("Error during Creation ", e);
            throw e;
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            logger.error("Error during Creation ", e);
            throw new ValidationException("Error during Creation: " + e.getMessage());
        }

    }

    public void removeFeatureAlias(String name, String featureZdbID) {
        try {
            Session session = HibernateUtil.currentSession();
            session.beginTransaction();
            Feature feature = featureRepository.getFeatureByID(featureZdbID);
            if (feature == null) {
                throw new ValidationException("no feature found");
            }
            FeatureAlias featureAlias = mutantRepository.getSpecificDataAlias(feature, name);
            featureRepository.deleteFeatureAlias(feature, featureAlias);
            // infrastructureRepository.deleteRecordAttributionsForData(featureDBLink.getZdbID());
            //session.delete(featureAlias);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            logger.error(e);
            HibernateUtil.rollbackTransaction();
        }
    }



        /*Feature feature = featureRepository.getFeatureByID(featureZdbID);
        FeatureAlias featureAlias = mutantRepository.getSpecificDataAlias(feature, name);
        HibernateUtil.createTransaction();
        featureRepository.deleteFeatureAlias(feature, featureAlias);
//        HibernateUtil.flushAndCommitCurrentSession();
    }*/


    public void removeFeatureSequence(String sequence, String featureZdbID) {

        try {
            Session session = HibernateUtil.currentSession();
            session.beginTransaction();
            Feature feature = featureRepository.getFeatureByID(featureZdbID);
            if (feature == null) {
                throw new ValidationException("no feature found");
            }
            DBLink featureDBLink = getSequenceRepository().getDBLink(featureZdbID, sequence);
            infrastructureRepository.deleteRecordAttributionsForData(featureDBLink.getZdbID());
            session.delete(featureDBLink);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            logger.error(e);
            HibernateUtil.rollbackTransaction();
        }
    }

    public void deleteFeature(String zdbID) {
        HibernateUtil.createTransaction();
        infrastructureRepository.deleteActiveDataByZdbID(zdbID);
        infrastructureRepository.insertUpdatesTable(zdbID, "Feature", zdbID, "", "delete this annotation via Feature curation tab");
        HibernateUtil.flushAndCommitCurrentSession();
    }

    public List<FeaturePrefixDTO> getPrefix(String labName) {
        return DTOConversionService.convertToFeaturePrefixDTO(featureRepository.getLabPrefixes(labName));
    }

    public String getNextZFLineNum() {
        String nextLineNum = featureRepository.getNextZFLineNum();
        return nextLineNum;
    }

    public FeatureDTO createFeature(FeatureDTO featureDTO) throws DuplicateEntryException, ValidationException {

        DTOConversionService.escapeFeatureDTO(featureDTO);
        checkDupes(featureDTO);
        checkDupesinTrackingTable(featureDTO);
        validateUnspecified(featureDTO);

        FeatureDTO newFeatureDTO;
        HibernateUtil.createTransaction();
        try {
            Publication publication = getPublicationRepository().getPublication(featureDTO.getPublicationZdbID());
            if (publication == null) {
                throw new ValidationException("Could not find publication for: " + featureDTO.getPublicationZdbID());
            }

            Feature feature = DTOConversionService.convertToFeature(featureDTO);

            if (StringUtils.isNotEmpty(featureDTO.getAlias())) {
                FeatureAlias featureAlias = new FeatureAlias();
                featureAlias.setFeature(feature);
                String groupName = DataAliasGroup.Group.ALIAS.toString();
                DataAliasGroup group = infrastructureRepository.getDataAliasGroupByName(groupName);
                featureAlias.setAliasGroup(group);  //default for database, hibernate tries to insert null
                featureAlias.setAlias(featureDTO.getAlias());
                featureAlias.setAliasLowerCase(featureDTO.getAlias().toLowerCase());
                if (feature.getAliases() == null) {
                    Set<FeatureAlias> featureAliases = new HashSet<>();
                    feature.setAliases(featureAliases);
                }
                feature.getAliases().add(featureAlias);
            }


            getFeatureRepository().saveFeature(feature, publication);
            if (CollectionUtils.isNotEmpty(featureDTO.getPublicNoteList())) {

                HashSet<FeatureNote> featureNoteSet = new HashSet<>(featureDTO.getPublicNoteList().size());
                feature.setExternalNotes(featureNoteSet);
            }
            if (CollectionUtils.isNotEmpty(featureDTO.getPublicNoteList())) {
                for (NoteDTO note : featureDTO.getPublicNoteList()) {


                    FeatureNote featureNote = new FeatureNote();
                    featureNote.setFeature(feature);
                    featureNote.setNote(note.getNoteData());
                    if (note.getPublicationZdbID() != null) {
                        featureNote.setPublication(getPublicationRepository().getPublication(note.getPublicationZdbID()));
                    }
                    if (note.getNoteType().equals(ExternalNote.Type.VARIANT.toString())) {
                        if (feature.getFeatureGenomicMutationDetail() != null) {
                            featureNote.setTag(ExternalNote.Type.VARIANT.toString() + feature.getFeatureGenomicMutationDetail().getZdbID());
                        } else {
                            featureNote.setTag(ExternalNote.Type.VARIANT.toString());
                        }
                    } else {
                        featureNote.setTag(ExternalNote.Type.FEATURE.toString());
                    }

                    feature.getExternalNotes().add(featureNote);
                }
            }

            if (StringUtils.isNotEmpty((featureDTO.getFeatureChromosome()))) {
                FeatureLocation fgl = new FeatureLocation();
                fgl.setFeature(feature);
                fgl.setFtrChromosome(featureDTO.getFeatureChromosome());

                fgl.setFtrAssembly(featureDTO.getFeatureAssembly());
                fgl.setFtrStartLocation(featureDTO.getFeatureStartLoc());
                fgl.setFtrEndLocation(featureDTO.getFeatureEndLoc());
                // convert code into TermID and then get GenericTerm
                fgl.setFtrLocEvidence(ontologyRepository.getTermByZdbID(FeatureService.getFeatureGenomeLocationEvidenceCodeTerm(featureDTO.getEvidence())));
                HibernateUtil.currentSession().save(fgl);

                PublicationAttribution pa = new PublicationAttribution();
                pa.setSourceZdbID(publication.getZdbID());
                pa.setDataZdbID(fgl.getZdbID());
                pa.setSourceType(RecordAttribution.SourceType.STANDARD);
                pa.setPublication(publication);
                Set<PublicationAttribution> pubattr = new HashSet<>();
                pubattr.add(pa);

                currentSession().save(pa);


            }


            /*if (StringUtils.isNotEmpty(featureDTO.getFgmdSeqRef())||(StringUtils.isNotEmpty(featureDTO.getFgmdSeqVar()))) {
                FeatureGenomicMutationDetail fgmd = new FeatureGenomicMutationDetail();
                fgmd.setFeature(feature);
                fgmd.setFgmdSeqRef(featureDTO.getFgmdSeqRef().toUpperCase());
                fgmd.setFgmdSeqVar(featureDTO.getFgmdSeqVar().toUpperCase());
                fgmd.setFgmdVarStrand("+");
                HibernateUtil.currentSession().save(fgmd);
                PublicationAttribution pa1 = new PublicationAttribution();
                pa1.setSourceZdbID(publication.getZdbID());
                pa1.setDataZdbID(fgmd.getZdbID());
                pa1.setSourceType(RecordAttribution.SourceType.STANDARD);
                pa1.setPublication(publication);
                Set<PublicationAttribution> pubattr1 = new HashSet<>();
                pubattr1.add(pa1);
                currentSession().save(pa1);
            }*/


            if (StringUtils.isNotEmpty(featureDTO.getFeatureSequence())) {
                /*ReferenceDatabase referenceDatabase = FeatureService.getForeignDbMutationDetailDna(featureDTO.getFeatureSequence());
                if (referenceDatabase == null)
                    throw new NullpointerException("Accession number not found in Genbank, RefSeq or Ensembl: " + featureDTO.getFeatureSequence());*/
                saveFeatureSequence(featureDTO.getFeatureSequence(), publication, feature);
            }

            if (featureDTO.getCuratorNotes() != null && featureDTO.getCuratorNotes().size() > 0) {
                for (NoteDTO noteDTO : featureDTO.getCuratorNotes()) {
                    DataNote dnote = new DataNote();
                    dnote.setDataZdbID(feature.getZdbID());
                    dnote.setDate(new Date());
                    dnote.setNote(noteDTO.getNoteData());
                    featureRepository.addFeatureDataNote(feature, dnote.getNote());
                }
            }
            if (featureDTO.getFeatureType() != FeatureTypeEnum.UNSPECIFIED) {
                if (featureDTO.getLabOfOrigin() != null) {
                    Organization lab = (Organization) HibernateUtil.currentSession().get(Organization.class, featureDTO.getLabOfOrigin());
                    if (lab == null) {
                        throw new RuntimeException("lab not found: " + featureDTO.getLabOfOrigin());
                    }
                    FeatureSource featureSource = new FeatureSource();
                    featureSource.setOrganization(lab);
                    featureSource.setDataZdbID(feature.getZdbID());

                    Set<FeatureSource> featureSources = feature.getSources();
                    if (featureSources == null) {
                        featureSources = new HashSet<>();
                        featureSources.add(featureSource);
                        feature.setSources(featureSources);
                    } else {
                        featureSources.add(featureSource);
                    }
                    HibernateUtil.currentSession().save(featureSource);
                } else {
                    throw new ValidationException("Feature cannot be saved without lab of origin");
                }
            }

            FeatureAssay featureAssay = new FeatureAssay();
            featureAssay.setFeature(feature);
            if (featureDTO.getMutagen() == null) {
                featureAssay.setMutagen(Mutagen.NOT_SPECIFIED);
            } else {
                List<String> allowedMutagens = featureRepository.getMutagensForFeatureType(feature.getType());
                if (allowedMutagens.indexOf(featureDTO.getMutagen()) == -1) {
                    throw new ValidationException("Invalid mutagen for feature type" + feature.getType().getDisplay());
                } else {
                    featureAssay.setMutagen(Mutagen.getType(featureDTO.getMutagen()));
                }
            }
            if (featureDTO.getMutagee() == null) {
                featureAssay.setMutagee(Mutagee.NOT_SPECIFIED);
            } else {
                featureAssay.setMutagee(Mutagee.getType(featureDTO.getMutagee()));
            }
            feature.setFeatureAssay(featureAssay);
            HibernateUtil.currentSession().save(featureAssay);
            feature.setFeatureAssay(featureAssay);
            HibernateUtil.currentSession().update(feature);
            HibernateUtil.flushAndCommitCurrentSession();
            newFeatureDTO = getFeature(feature.getZdbID());

            Map<FieldName, Object> solrDoc = new HashMap<>(12);
            solrDoc.put(FieldName.ID, feature.getZdbID());
            solrDoc.put(FieldName.CATEGORY, Category.MUTANT.getName());
            solrDoc.put(FieldName.TYPE, feature.getType().getTypeDisplay());
            solrDoc.put(FieldName.NAME, feature.getName());
            solrDoc.put(FieldName.PROPER_NAME, feature.getName());
            solrDoc.put(FieldName.FULL_NAME, feature.getName());
            solrDoc.put(FieldName.NAME_SORT, feature.getNameOrder());
            solrDoc.put(FieldName.ALIAS, feature.getAbbreviation());
            solrDoc.put(FieldName.URL, "/" + feature.getZdbID());
            if (feature.getAliases() != null) {
                List<String> aliases = feature.getAliases().stream()
                        .map(DataAlias::getAlias)
                        .collect(Collectors.toList());
                solrDoc.put(FieldName.ALIAS, aliases);
            }
            solrDoc.put(FieldName.DATE, new Date());
            solrDoc.put(FieldName.MUTAGEN, feature.getFeatureAssay().getMutagen().toString());
            SolrService.addDocument(solrDoc);

        } catch (ValidationException e) {
            HibernateUtil.rollbackTransaction();
            logger.info("Error during Creation ", e);
            throw new ValidationException("Error during Creation: " + e.getMessage());
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            logger.error("Error during Creation ", e);
            throw new ValidationException("Error during Creation: " + e.getMessage());
        }

        return newFeatureDTO;
    }

    private void saveFeatureSequence(String sequence, Publication publication, Feature feature) throws ValidationException {
        ReferenceDatabase referenceDatabase = FeatureService.getForeignDbMutationDetailDna(sequence);
        if (referenceDatabase == null) {
            throw new NullpointerException("Accession number not found in Genbank, RefSeq or Ensembl: " + sequence);
        }
        FeatureDBLink featureDBLink = new FeatureDBLink();
        featureDBLink.setFeature(feature);
        featureDBLink.setAccessionNumber(sequence);

        featureDBLink.setAccessionNumberDisplay(sequence);
        ReferenceDatabase genBankRefDB = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.GENOMIC, ForeignDBDataType.SuperType.SEQUENCE, Species.Type.ZEBRAFISH);
        featureDBLink.setReferenceDatabase(genBankRefDB);

        currentSession().save(featureDBLink);
        //now handle alias attribution

        if (publication != null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setSourceZdbID(publication.getZdbID());
            pa.setDataZdbID(featureDBLink.getZdbID());
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            pa.setPublication(publication);
            Set<PublicationAttribution> pubattr = new HashSet<>();
            pubattr.add(pa);

            featureDBLink.setPublications(pubattr);
            currentSession().save(pa);

        }
    }

    private void validateUnspecified(FeatureDTO featureDTO) throws ValidationException {
        if (featureDTO.getFeatureType() == FeatureTypeEnum.UNSPECIFIED) {
            Marker m = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(featureDTO.getOptionalName());
            if (m == null) {
                throw new ValidationException("[" + featureDTO.getOptionalName() + "] not found.  "
                        + MESSAGE_UNSPECIFIED_FEATURE
                );
            } else if (false == m.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                throw new ValidationException("[" + featureDTO.getOptionalName() + "] must be a gene.  "
                        + MESSAGE_UNSPECIFIED_FEATURE
                );
            }
            if (getInfrastructureRepository().getRecordAttribution(m.getZdbID(), featureDTO.getPublicationZdbID(), RecordAttribution.SourceType.STANDARD)
                    == null) {
                throw new ValidationException("The gene [" + featureDTO.getOptionalName() + "] must be attributed to this pub [" + featureDTO.getPublicationZdbID() + "]. "
                        + MESSAGE_UNSPECIFIED_FEATURE);
            }
        }
    }

    public void deleteFeatureMarkerRelationship(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO) {

        HibernateUtil.createTransaction();
        String zdbID = featureMarkerRelationshipDTO.getZdbID();
        infrastructureRepository.insertUpdatesTable(zdbID, "Feature", zdbID, "",
                "deleted feature/marker relationship between: "
                        + featureMarkerRelationshipDTO.getFeatureDTO().getAbbreviation()
                        + " and "
                        + featureMarkerRelationshipDTO.getMarkerDTO().getName()
                        + " of type "
                        + featureMarkerRelationshipDTO.getRelationshipType()
        );
        infrastructureRepository.deleteActiveDataByZdbID(zdbID);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    public List<String> getRelationshipTypesForFeatureType(FeatureTypeEnum ftrTypeDisplay) {
        return featureRepository.getRelationshipTypesForFeatureType(ftrTypeDisplay);
    }

    public List<MarkerDTO> getMarkersForFeatureRelationAndSource(String featureTypeName, String publicationZdbID) {
        List<Marker> markers = featureRepository.getMarkersForFeatureRelationAndSource(featureTypeName, publicationZdbID);
        List<MarkerDTO> markerDTOs = new ArrayList<MarkerDTO>();
        for (Marker m : markers) {
            markerDTOs.add(DTOConversionService.convertToMarkerDTO(m));
        }
        return markerDTOs;
    }

    @Override
    public List<FeatureMarkerRelationshipDTO> addFeatureMarkerRelationShip(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO, String publicationID) {

        FeatureDTO featureDTO = featureMarkerRelationshipDTO.getFeatureDTO();
        HibernateUtil.createTransaction();
        Feature feature = featureRepository.getFeatureByID(featureDTO.getZdbID());
        FeatureMarkerRelationship featureMarkerRelationship = new FeatureMarkerRelationship();
        featureMarkerRelationship.setFeature(feature);

        MarkerDTO markerDTO = featureMarkerRelationshipDTO.getMarkerDTO();
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerDTO.getZdbID());
        featureMarkerRelationship.setMarker(marker);

        featureMarkerRelationship.setType(FeatureMarkerRelationshipTypeEnum.getType(featureMarkerRelationshipDTO.getRelationshipType()));

        HibernateUtil.currentSession().save(featureMarkerRelationship);
        infrastructureRepository.insertPublicAttribution(featureMarkerRelationship.getZdbID(), featureMarkerRelationshipDTO.getPublicationZdbID());
        infrastructureRepository.insertUpdatesTable(featureMarkerRelationship.getZdbID(), "FeatureMarkerRelationship", featureMarkerRelationship.toString(), "Created feature marker relationship");
        //add attribution to coding sequence of related construct
        if (feature.getType().equals(FeatureTypeEnum.TRANSGENIC_INSERTION)) {

            List<Marker> codingSeq = RepositoryFactory.getMarkerRepository().getCodingSequence(marker);
            for (Marker codingGene : codingSeq) {
                if (infrastructureRepository.getRecordAttribution(codingGene.zdbID, featureMarkerRelationshipDTO.getPublicationZdbID(), RecordAttribution.SourceType.STANDARD) == null) {
                    infrastructureRepository.insertRecordAttribution(codingGene.zdbID, featureMarkerRelationshipDTO.getPublicationZdbID());
                    infrastructureRepository.insertUpdatesTable(codingGene.zdbID, "record attribution", featureMarkerRelationshipDTO.getPublicationZdbID(), "Added direct attribution to related construct");
                }
            }
        }
        HibernateUtil.flushAndCommitCurrentSession();
        HibernateUtil.closeSession();
        List<FeatureMarkerRelationshipDTO> dtos = getFeatureMarkerRelationshipsForPub(publicationID);
        return dtos;
    }

    @Override
    public FeatureDTO editPublicNote(NoteDTO noteDTO) {
        HibernateUtil.createTransaction();
        Feature feature = featureRepository.getFeatureByID(noteDTO.getDataZdbID());
        // new note
        if (noteDTO.getZdbID() == null) {
            FeatureNote note = new FeatureNote();
            note.setNote(noteDTO.getNoteData());
            note.setFeature(feature);

            if (noteDTO.getNoteTag().equals(ExternalNote.Type.FEATURE.toString())) {
                note.setTag(ExternalNote.Type.FEATURE.toString());
            }
            if (noteDTO.getNoteTag().equals(ExternalNote.Type.VARIANT.toString())) {
                if (feature.getFeatureGenomicMutationDetail() != null) {
                    note.setTag(ExternalNote.Type.VARIANT.toString() + feature.getFeatureGenomicMutationDetail().getZdbID());
                } else {
                    note.setTag(ExternalNote.Type.VARIANT.toString());
                }
            }

            note.setPublication(getPublicationRepository().getPublication(noteDTO.getPublicationZdbID()));
            feature.addExternalNote(note);
        } else {
            for (FeatureNote note : feature.getExternalNotes()) {
                if (note.getZdbID().equals(noteDTO.getZdbID())) {
                    String oldNote = note.getNote();
                    String newNote = noteDTO.getNoteData();
                    if (!StringUtils.equals(newNote, oldNote)) {
                        note.setNote(newNote);
                        infrastructureRepository.insertUpdatesTable(feature.getZdbID(), "Public Note", oldNote, newNote);
                    }
                    String oldNoteType = note.getType();
                    String newNoteType = noteDTO.getNoteType();
                    String newNoteTag = noteDTO.getNoteType();
                    String oldNoteTag = note.getTag();
                    if (!StringUtils.equals(newNoteTag, oldNoteTag)) {
                        note.setType(newNoteType);
                        note.setTag(newNoteTag);
                        infrastructureRepository.insertUpdatesTable(feature.getZdbID(), "Public Note Type", oldNoteType, newNoteType);
                    }
                }
            }
        }
        HibernateUtil.flushAndCommitCurrentSession();
        return DTOConversionService.convertToFeatureDTO(feature);

    }

    @Override
    public CuratorNoteDTO addCuratorNote(CuratorNoteDTO noteDTO) {
        logger.info("adding curator note: " + noteDTO.getDataZdbID() + " - " + noteDTO.getNoteData());
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        Feature feature = featureRepository.getFeatureByID(noteDTO.getDataZdbID());
        DataNote dataNote = featureRepository.addFeatureDataNote(feature, noteDTO.getNoteData());
        noteDTO.setCurator(DTOConversionService.convertToPersonDTO(dataNote.getCurator()));
        infrastructureRepository.insertUpdatesTable(feature.getZdbID(), "curator note", dataNote.getNote(), "added note");
        transaction.commit();
        noteDTO.setZdbID(dataNote.getZdbID());
        return noteDTO;
    }

    /**
     * @param noteDTO NoteDTO
     */
    @Override
    public void editCuratorNote(NoteDTO noteDTO) {
        logger.info("adding curator note: " + noteDTO.getDataZdbID() + " - " + noteDTO.getNoteData());
        Feature feature = featureRepository.getFeatureByID(noteDTO.getDataZdbID());
        Set<DataNote> dataNotes = feature.getDataNotes();
        for (DataNote dataNote : dataNotes) {
            if (dataNote.getZdbID().equals(noteDTO.getZdbID())) {
                infrastructureRepository.insertUpdatesTable(feature.getZdbID(), "updated note", dataNote.getNote(), noteDTO.getNoteData());
                dataNote.setNote(noteDTO.getNoteData());
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
    @Override
    public void removeCuratorNote(NoteDTO noteDTO) {
        logger.info("remove curator note: " + noteDTO.getNoteData() + " - " + noteDTO.getZdbID());
        Feature feature = featureRepository.getFeatureByID(noteDTO.getDataZdbID());
        Set<DataNote> dataNotes = feature.getDataNotes();
        for (DataNote dataNote : dataNotes) {
            if (dataNote.getZdbID().equals(noteDTO.getZdbID())) {
                HibernateUtil.createTransaction();
                infrastructureRepository.insertUpdatesTable(feature.getZdbID(), "removed curator note", dataNote.getNote(), noteDTO.getNoteData());
                HibernateUtil.currentSession().delete(dataNote);
                HibernateUtil.flushAndCommitCurrentSession();
                return;
            }
        }
        logger.error("note not found with zdbID: " + noteDTO.getZdbID());
    }

    public List<String> getMutagensForFeatureType(FeatureTypeEnum ftrType) {
        return featureRepository.getMutagensForFeatureType(ftrType);
    }

    @Override
    public void removePublicNote(NoteDTO noteDTO) {
        logger.info("remove public note: " + noteDTO.getNoteData() + " - " + noteDTO.getZdbID());
        HibernateUtil.createTransaction();
        Feature feature = featureRepository.getFeatureByID(noteDTO.getDataZdbID());
        Set<FeatureNote> featureNotes = feature.getExternalNotes();
        for (FeatureNote featureNote : featureNotes) {
            if (featureNote.getZdbID().equals(noteDTO.getZdbID())) {
                infrastructureRepository.deleteActiveDataByZdbID(featureNote.getZdbID());
                infrastructureRepository.insertUpdatesTable(feature.getZdbID(), "removed public note", featureNote.getNote(), noteDTO.getNoteData());
                HibernateUtil.flushAndCommitCurrentSession();
                return;
            }
        }
        HibernateUtil.rollbackTransaction();
        logger.error("note not found with zdbID: " + noteDTO.getZdbID());
    }

    @Override
    public PersonDTO getCuratorInfo() {
        return DTOConversionService.convertToPersonDTO(ProfileService.getCurrentSecurityUser());
    }

    @Override
    public List<MutationDetailControlledVocabularyTermDTO> getDnaChangeList() {
        List<DnaMutationTerm> list = getDnaMutationTermRepository().getControlledVocabularyTermList();
        List<MutationDetailControlledVocabularyTermDTO> dtoList = new ArrayList<>(list.size());
        for (MutationDetailControlledVocabularyTerm controlledVocab : list) {
            dtoList.add(DTOConversionService.convertMutationDetailedControlledVocab(controlledVocab));
        }
        return dtoList;
    }

    @Override
    public List<MutationDetailControlledVocabularyTermDTO> getDnaLocalizationChangeList() {
        List<GeneLocalizationTerm> list = getGeneLocalizationTermRepository().getControlledVocabularyTermList();
        List<MutationDetailControlledVocabularyTermDTO> dtoList = new ArrayList<>(list.size());
        for (MutationDetailControlledVocabularyTerm controlledVocab : list) {
            dtoList.add(DTOConversionService.convertMutationDetailedControlledVocab(controlledVocab));
        }
        return dtoList;
    }

    @Override
    public List<MutationDetailControlledVocabularyTermDTO> getProteinConsequenceList() {
        List<ProteinConsequence> list = getProteinConsequenceTermRepository().getControlledVocabularyTermList();
        List<MutationDetailControlledVocabularyTermDTO> dtoList = new ArrayList<>(list.size());
        for (MutationDetailControlledVocabularyTerm controlledVocab : list) {
            dtoList.add(DTOConversionService.convertMutationDetailedControlledVocab(controlledVocab));
        }
        return dtoList;
    }

    @Override
    public List<MutationDetailControlledVocabularyTermDTO> getTranscriptConsequenceList() {
        List<TranscriptConsequence> list = getTranscriptTermRepository().getControlledVocabularyTermList();
        List<MutationDetailControlledVocabularyTermDTO> dtoList = new ArrayList<>(list.size());
        for (MutationDetailControlledVocabularyTerm controlledVocab : list) {
            dtoList.add(DTOConversionService.convertMutationDetailedControlledVocab(controlledVocab));
        }
        return dtoList;
    }

    @Override
    public List<MutationDetailControlledVocabularyTermDTO> getAminoAcidList() {
        List<AminoAcidTerm> list = getAminoAcidTermRepository().getControlledVocabularyTermList();
        List<MutationDetailControlledVocabularyTermDTO> dtoList = new ArrayList<>(list.size());
        for (MutationDetailControlledVocabularyTerm controlledVocab : list) {
            dtoList.add(DTOConversionService.convertMutationDetailedControlledVocab(controlledVocab));
        }
        return dtoList;
    }

    @Override
    public String isValidAccession(String accessionNumber, String type) {
        if (type.equals("DNA")) {
            ReferenceDatabase referenceDatabase = FeatureService.getForeignDbMutationDetailDna(accessionNumber);
            return referenceDatabase != null ? referenceDatabase.getForeignDB().getDbName().toString() : null;
        } else {
            ReferenceDatabase referenceDatabase = FeatureService.getForeignDbMutationDetailProtein(accessionNumber);
            return referenceDatabase != null ? referenceDatabase.getForeignDB().getDbName().toString() : null;
        }
    }

    private class SupplierCacheThread extends Thread {

        @Override
        public void run() {
            labsOfOrigin = null;
            getLabsOfOriginWithPrefix();
            HibernateUtil.closeSession();
        }
    }
}
