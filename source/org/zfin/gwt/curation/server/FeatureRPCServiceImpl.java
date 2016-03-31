package org.zfin.gwt.curation.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.Species;
import org.zfin.feature.*;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.curation.ui.FeatureRPCService;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.gwt.root.ui.ValidationException;
import org.zfin.infrastructure.DataAliasGroup;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.profile.FeatureSource;
import org.zfin.profile.Organization;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.util.ZfinStringUtils;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.*;

/**
 */
public class FeatureRPCServiceImpl extends RemoteServiceServlet implements FeatureRPCService {

    private transient Logger logger = Logger.getLogger(FeatureRPCServiceImpl.class);
    private final MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private final PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private final SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
    private static InfrastructureRepository infrastructureRepository = getInfrastructureRepository();
    private static FeatureRepository featureRepository = getFeatureRepository();
    private static ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();
    private List<Organization> labsOfOrigin = null;


    private final static String MESSAGE_UNSPECIFIED_FEATURE = "An unspecified feature name must have a valid gene abbreviation.";
    private ProfileService profileService = new ProfileService();

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

    /**
     * Here, we edit everything but the notes (done in-line) and the alias (also done in-line).
     *
     * @param featureDTO
     * @return
     * @throws DuplicateEntryException
     */
    public FeatureDTO editFeatureDTO(FeatureDTO featureDTO) throws DuplicateEntryException, ValidationException {

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
        if (featureDTO.getPublicNote() != null) {
            feature.setPublicComments(featureDTO.getPublicNote().getNoteData());
        }
        if (oldFeatureType == featureDTO.getFeatureType()) {
            List<RecordAttribution> recordAttributions = infrastructureRepository.getRecAttribforFtrType(feature.getZdbID());
            if (recordAttributions.size() != 0) {
                if (!recordAttributions.get(0).getSourceZdbID().equals(featureDTO.getPublicationZdbID())) {
                    infrastructureRepository.removeRecordAttributionForType(recordAttributions.get(0).getSourceZdbID(), feature.getZdbID());
                    infrastructureRepository.insertUpdatesTable(feature.getZdbID(), "Feature type attribution", oldFeatureType.name(), featureDTO.getFeatureType().toString(), recordAttributions.get(0).getSourceZdbID());
                    infrastructureRepository.insertPublicAttribution(featureDTO.getZdbID(), featureDTO.getPublicationZdbID(), RecordAttribution.SourceType.FEATURE_TYPE);
                }
            }
        } else {
            RecordAttribution recordAttributions = infrastructureRepository.getRecordAttribution(feature.getZdbID(), featureDTO.getPublicationZdbID(), RecordAttribution.SourceType.FEATURE_TYPE);
            if (recordAttributions == null) {
                List<RecordAttribution> recordAttribution = infrastructureRepository.getRecAttribforFtrType(feature.getZdbID());
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
        String existingFeatureAbbrev = feature.getAbbreviation();
       /* if (existingFeatureAbbrev != featureDTO.getAbbreviation())   {
        feature.setAbbreviation(featureDTO.getAbbreviation());
        }*/
        if (existingFeature == null) {
            feature.setAbbreviation(featureDTO.getAbbreviation());
        }
        feature.setName(featureDTO.getName());


        feature.setDominantFeature(featureDTO.getDominant());
        feature.setKnownInsertionSite(featureDTO.getKnownInsertionSite());
       /* if (featureDTO.getKnownInsertionSite()) {
            feature.setTransgenicSuffix(featureDTO.getTransgenicSuffix());
        }*/
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

        // get labs of origin for feature
        Organization existingLabOfOrigin = featureRepository.getLabByFeature(feature);
        if (featureDTO.getLabOfOrigin() != null && existingLabOfOrigin != null) {
            if (false == featureDTO.getLabOfOrigin().equals(existingLabOfOrigin.getZdbID())) {
                Organization newLabOfOrigin = profileRepository.getOrganizationByZdbID(featureDTO.getLabOfOrigin());
                featureRepository.setLabOfOriginForFeature(newLabOfOrigin, feature);
            }
        } /*else if (featureDTO.getLabOfOrigin() == null && existingLabOfOrigin != null) {
            featureRepository.deleteLabOfOriginForFeature(feature);*/ else if (featureDTO.getLabOfOrigin() != null && existingLabOfOrigin == null) {
            featureRepository.addLabOfOriginForFeature(feature, featureDTO.getLabOfOrigin());
        } else {
            throw new ValidationException("Feature cannot be saved without lab of origin");
        }
        HibernateUtil.currentSession().update(feature);
        if (!StringUtils.equals(oldFtrName, newFtrName)) {

            FeatureAlias featureAlias = mutantRepository.getSpecificDataAlias(feature, oldFtrName);
            if (featureAlias != null) {
                infrastructureRepository.insertPublicAttribution(featureAlias.getZdbID(), featureDTO.getPublicationZdbID(), RecordAttribution.SourceType.STANDARD);
            }
        }
        //currentSession().flush();
        //currentSession().refresh(feature);
        HibernateUtil.flushAndCommitCurrentSession();

        return getFeature(featureDTO.getZdbID());
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

    public List<FeatureDTO> getFeaturesForPub(String pubZdbId) {
        Publication pub = pubRepository.getPublication(pubZdbId);
        List<FeatureDTO> featureDTOs = new ArrayList<FeatureDTO>();
        List<Feature> features = featureRepository.getFeaturesForStandardAttribution(pub);
        if (CollectionUtils.isNotEmpty(features)) {
            for (Feature f : features) {

                featureDTOs.add(DTOConversionService.convertToFeatureDTO(f));

            }
        }
        Collections.sort(featureDTOs, new Comparator<FeatureDTO>() {
            @Override
            public int compare(FeatureDTO o1, FeatureDTO o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });

        return featureDTOs;
    }


    public List<FeatureMarkerRelationshipDTO> getFeaturesMarkerRelationshipsForPub(String publicationZdbID) {

        List<FeatureMarkerRelationshipDTO> featureDTOs = new ArrayList<FeatureMarkerRelationshipDTO>();
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
        featureAlias.setAlias(ZfinStringUtils.escapeHighUnicode(name));
        String groupName = DataAliasGroup.Group.ALIAS.toString();
        DataAliasGroup group = infrastructureRepository.getDataAliasGroupByName(groupName);
        featureAlias.setAliasGroup(group);  //default for database, hibernate tries to insert null
        HibernateUtil.currentSession().save(featureAlias);
        infrastructureRepository.insertPublicAttribution(featureAlias.getZdbID(), pubZdbID, RecordAttribution.SourceType.STANDARD);

        HibernateUtil.flushAndCommitCurrentSession();
        mutantRepository.runFeatureNameFastSearchUpdate(feature);


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
            if (feature == null)
                throw new ValidationException("no feature found");
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
            if (feature == null)
                throw new ValidationException("no feature found");
            DBLink featureDBLink = getSequenceRepository().getDBLink(featureZdbID, sequence);
            infrastructureRepository.deleteRecordAttributionsForData(featureDBLink.getZdbID());
            session.delete(featureDBLink);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            logger.error(e);
            HibernateUtil.rollbackTransaction();
        }
    }


    private class SupplierCacheThread extends Thread {

        @Override
        public void run() {
            labsOfOrigin = null;
            getLabsOfOriginWithPrefix();

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

    public FeatureDTO createFeature(FeatureDTO featureDTO) throws DuplicateEntryException, ValidationException {

        DTOConversionService.escapeFeatureDTO(featureDTO);
        checkDupes(featureDTO);
        checkDupesinTrackingTable(featureDTO);
        validateUnspecified(featureDTO);

        FeatureDTO newFeatureDTO;
        HibernateUtil.createTransaction();
        try {
            Publication publication = getPublicationRepository().getPublication(featureDTO.getPublicationZdbID());
            if (publication == null)
                throw new ValidationException("Could not find publication for: " + featureDTO.getPublicationZdbID());

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


            if (StringUtils.isNotEmpty(featureDTO.getFeatureSequence())) {
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
                    } else featureSources.add(featureSource);
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
        // only upper case letters and numerals
        if (!sequence.matches("[A-Z0-9]*"))
            throw new ValidationException("Invalid accession number / sequence: Only upper case letters and numerals allowed");
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
            } else if (false == m.getZdbID().startsWith("ZDB-GENE-")) {
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
    public void addFeatureMarkerRelationShip(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO) {
        FeatureMarkerRelationship featureMarkerRelationship = new FeatureMarkerRelationship();

        FeatureDTO featureDTO = featureMarkerRelationshipDTO.getFeatureDTO();
        Feature feature = featureRepository.getFeatureByID(featureDTO.getZdbID());
        featureMarkerRelationship.setFeature(feature);

        MarkerDTO markerDTO = featureMarkerRelationshipDTO.getMarkerDTO();
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerDTO.getZdbID());
        featureMarkerRelationship.setMarker(marker);

        featureMarkerRelationship.setType(FeatureMarkerRelationshipTypeEnum.getType(featureMarkerRelationshipDTO.getRelationshipType()));

        HibernateUtil.createTransaction();
        HibernateUtil.currentSession().save(featureMarkerRelationship);
        infrastructureRepository.insertPublicAttribution(featureMarkerRelationship.getZdbID(), featureMarkerRelationshipDTO.getPublicationZdbID());
        infrastructureRepository.insertUpdatesTable(featureMarkerRelationship.getZdbID(), "FeatureMarkerRelationship", featureMarkerRelationship.toString(), "Created feature marker relationship");
        HibernateUtil.flushAndCommitCurrentSession();
    }

    @Override
    public void editPublicNote(NoteDTO noteDTO) {
        HibernateUtil.createTransaction();
        Feature feature = featureRepository.getFeatureByID(noteDTO.getDataZdbID());
        // new note
        if (noteDTO.getZdbID() == null) {
            FeatureNote note = new FeatureNote();
            note.setNote(noteDTO.getNoteData());
            note.setFeature(feature);
            feature.getExternalNotes().add(note);
        } else {
            for (FeatureNote note : feature.getExternalNotes()) {
                if (note.getZdbID().equals(noteDTO.getZdbID())) {
                    String oldNote = note.getNote();
                    String newNote = noteDTO.getNoteData();
                    if (!StringUtils.equals(newNote, oldNote)) {
                        note.setNote(newNote);
                        infrastructureRepository.insertUpdatesTable(feature.getZdbID(), "Public Note", oldNote, newNote);
                    }
                }
            }
        }
        HibernateUtil.flushAndCommitCurrentSession();
    }

    @Override
    public NoteDTO addCuratorNote(NoteDTO noteDTO) {
        logger.info("adding curator note: " + noteDTO.getDataZdbID() + " - " + noteDTO.getNoteData());
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        Feature feature = featureRepository.getFeatureByID(noteDTO.getDataZdbID());
        DataNote dataNote = featureRepository.addFeatureDataNote(feature, noteDTO.getNoteData());
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
}
