package org.zfin.gwt.curation.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAlias;
import org.zfin.feature.FeatureAssay;
import org.zfin.feature.FeatureMarkerRelationship;
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
import org.zfin.people.FeatureSource;
import org.zfin.people.Lab;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 */
public class FeatureRPCServiceImpl extends RemoteServiceServlet implements FeatureRPCService {

    private transient Logger logger = Logger.getLogger(FeatureRPCServiceImpl.class);
    private final MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private final PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private static FeatureRepository featureRepository = RepositoryFactory.getFeatureRepository();
    private List<Lab> labsOfOrigin = null;


    private final static String MESSAGE_UNSPECIFIED_FEATURE = "An unspecified feature name must have a valid gene abbreviation." ;

    public FeatureDTO getFeature(String featureZdbID) {
        Feature feature = (Feature) HibernateUtil.currentSession().get(Feature.class,featureZdbID);
        return DTOConversionService.convertToFeatureDTO(feature);
    }

    private void checkDupes(FeatureDTO featureDTO) throws DuplicateEntryException{
        // retrieve
        Feature existingFeature = featureRepository.getFeatureByAbbreviation(featureDTO.getAbbreviation()) ;

        // if there is a feature that already has this name, but it is not this feature
        if(existingFeature!=null && false==existingFeature.getZdbID().equals(featureDTO.getZdbID())){
            throw new DuplicateEntryException("Feature exists for this abbreviation: "+ featureDTO.getAbbreviation()) ;
        }

        if(featureDTO.getLabPrefix()!=null && featureDTO.getLineNumber()!=null){
            existingFeature = featureRepository.getFeatureByPrefixAndLineNumber(featureDTO.getLabPrefix(),featureDTO.getLineNumber()) ;
        }

        // if there is a feature that already has this name, but it is not this feature
        if(existingFeature!=null && false==existingFeature.getZdbID().equals(featureDTO.getZdbID())){
            throw new DuplicateEntryException("Feature exists with this prefix ["+featureDTO.getLabPrefix() +"] and line number ["+featureDTO.getLineNumber()+"] : "+ existingFeature.getAbbreviation() + "->"+existingFeature.getZdbID()) ;
        }

        // if there is a feature that already has this name, but it is not this feature
        if(existingFeature!=null && false==existingFeature.getZdbID().equals(featureDTO.getZdbID())){
            throw new DuplicateEntryException("Feature exists for this abbreviation: "+ featureDTO.getAbbreviation()) ;
        }
    }

    /**
     * Here, we edit everything but the notes (done in-line) and the alias (also done in-line).
     * @param featureDTO
     * @return
     * @throws DuplicateEntryException
     */
    public FeatureDTO editFeatureDTO(FeatureDTO featureDTO) throws DuplicateEntryException , ValidationException{

        checkDupes(featureDTO);
        validateUnspecified(featureDTO);

        Feature feature = (Feature) HibernateUtil.currentSession().get(Feature.class,featureDTO.getZdbID()) ;
//        String oldFtrValue = feature.getAbbreviation();
        FeatureTypeEnum oldFeatureType = feature.getType();


        HibernateUtil.createTransaction();
        feature.setType(featureDTO.getFeatureType());

        if(featureDTO.getPublicNote()!=null){
            feature.setPublicComments(featureDTO.getPublicNote().getNoteData());
        }
        if (oldFeatureType==featureDTO.getFeatureType()){
            List<RecordAttribution> recordAttributions= infrastructureRepository.getRecAttribforFtrType(feature.getZdbID());
            if (recordAttributions.size()!=0)
                if (!recordAttributions.get(0).getSourceZdbID().equals(featureDTO.getPublicationZdbID())){
                    infrastructureRepository.removeRecordAttributionForType(recordAttributions.get(0).getSourceZdbID(),feature.getZdbID());
                    infrastructureRepository.insertUpdatesTable(feature.getZdbID(),"Feature type attribution",oldFeatureType.name(),featureDTO.getFeatureType().toString(), recordAttributions.get(0).getSourceZdbID());
                    infrastructureRepository.insertPublicAttribution(featureDTO.getZdbID(),featureDTO.getPublicationZdbID(),RecordAttribution.SourceType.FEATURE_TYPE) ;
                }
        }

        feature.setAbbreviation(featureDTO.getAbbreviation());
        feature.setName(featureDTO.getName());
//        if (!oldFtrValue.equals(featureDTO.getAbbreviation())) {
//            addFeatureAlias(oldFtrValue, featureDTO.getZdbID());
//            HibernateUtil.createTransaction();
//        }
        feature.setDominantFeature(featureDTO.getDominant());
        feature.setKnownInsertionSite(featureDTO.getKnownInsertionSite());
        if (featureDTO.getKnownInsertionSite()) {
            feature.setTransgenicSuffix(featureDTO.getTransgenicSuffix());
        }
        if (featureDTO.getLineNumber() != null) {
            feature.setLineNumber(featureDTO.getLineNumber());
        }
        if (StringUtils.isNotEmpty(featureDTO.getLabPrefix())) {
            feature.setFeaturePrefix(featureRepository.getFeatureLabPrefixID(featureDTO.getLabPrefix()));
        }
        FeatureAssay featureAssay = featureRepository.getFeatureAssay(featureDTO.getZdbID());
        if (featureDTO.getMutagen() != null) {
            featureAssay.setMutagen(Mutagen.getType(featureDTO.getMutagen()));
        }
        if (featureDTO.getMutagee() != null) {
            featureAssay.setMutagee(Mutagee.getType(featureDTO.getMutagee()));
        }

        // get labs of origin for feature
        Lab existingLabOfOrigin = featureRepository.getLabByFeature(feature);
        if(featureDTO.getLabOfOrigin()!=null && existingLabOfOrigin!=null){
            if(false==featureDTO.getLabOfOrigin().equals(existingLabOfOrigin.getZdbID())){
                 featureRepository.setLabOfOriginForFeature(existingLabOfOrigin,feature);
            }
        }
        else
        if(featureDTO.getLabOfOrigin()==null && existingLabOfOrigin!=null){
            featureRepository.deleteLabOfOriginForFeature(feature);
        }
        else
        if(featureDTO.getLabOfOrigin()!=null && existingLabOfOrigin==null){
            featureRepository.addLabOfOriginForFeature(feature,featureDTO.getLabOfOrigin());
        }
        HibernateUtil.flushAndCommitCurrentSession();
        return getFeature(featureDTO.getZdbID());
    }


    /**
     * Returns the current list of labs, always recaching the in the background.
     * @return A string of available labs.
     */
    public List<LabDTO> getLabsOfOriginWithPrefix() {
        if (labsOfOrigin == null) {
            labsOfOrigin = featureRepository.getLabsOfOriginWithPrefix();
            return DTOConversionService.convertToLabDTO(labsOfOrigin);
        } else {
            List<Lab> returnList = Arrays.asList(new Lab[labsOfOrigin.size()]);
            java.util.Collections.copy(returnList, labsOfOrigin);
            new SupplierCacheThread().start();
            return DTOConversionService.convertToLabDTO(returnList);
        }
    }

    public List<FeatureDTO> getFeaturesForPub(String pubZdbId) {
        //Publication pub=pubRepository.getPublication(dto.getPublicationZdbID());
        List<FeatureDTO> featureDTOs = new ArrayList<FeatureDTO>();
        List<Feature> features = featureRepository.getFeatureForAttribution(pubZdbId);
        if (CollectionUtils.isNotEmpty(features)) {
            for (Feature f : features) {

                featureDTOs.add(DTOConversionService.convertToFeatureDTO(f));

            }
        }
        Collections.sort(featureDTOs, new Comparator<FeatureDTO>(){
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

    public void addFeatureAlias(String name, String ftrZdbID,String pubZdbID) {
        Feature feature = (Feature) HibernateUtil.currentSession().get(Feature.class, ftrZdbID);
        HibernateUtil.createTransaction();
        FeatureAlias featureAlias = new FeatureAlias();
        featureAlias.setFeature(feature);
        featureAlias.setAlias(DTOConversionService.escapeString(name));
        String groupName = DataAliasGroup.Group.ALIAS.toString();
        DataAliasGroup group = infrastructureRepository.getDataAliasGroupByName(groupName);
        featureAlias.setAliasGroup(group);  //default for database, hibernate tries to insert null
        HibernateUtil.currentSession().save(featureAlias);
        infrastructureRepository.insertPublicAttribution(featureAlias.getZdbID(), pubZdbID, RecordAttribution.SourceType.STANDARD);
        HibernateUtil.flushAndCommitCurrentSession();


    }

    public void removeFeatureAlias(String name, String featureZdbID) {
        Feature feature = featureRepository.getFeatureByID(featureZdbID);
        FeatureAlias featureAlias = mutantRepository.getSpecificDataAlias(feature, name);
        HibernateUtil.createTransaction();
        featureRepository.deleteFeatureAlias(feature, featureAlias);
        HibernateUtil.flushAndCommitCurrentSession();
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
        return DTOConversionService.convertToFeaturePrefixDTO(featureRepository.getLabPrefixes(labName)) ;
//        Session session = HibernateUtil.currentSession();
//        String hqlLab1 = " select fp.featurePrefix from LabFeaturePrefix lfp, Lab lb, FeaturePrefix fp where lb.name=:labName and lb.zdbID=lfp.labZdbID and lfp.labPkID=fp.featurePkID ";
//        Query queryLab = session.createQuery(hqlLab1);
//        queryLab.setParameter("labName", labName);
//
//        List<String> labPrefix = (List<String>) queryLab.list();
//        if (labPrefix == null) {
//            return null;
//        }
//        return labPrefix;
    }

    public FeatureDTO createFeature(FeatureDTO featureDTO) throws DuplicateEntryException , ValidationException{

        DTOConversionService.escapeFeatureDTO(featureDTO);
        checkDupes(featureDTO);
        validateUnspecified(featureDTO);

        HibernateUtil.createTransaction();
        Person person = Person.getCurrentSecurityUser();
        Feature feature = DTOConversionService.convertToFeature(featureDTO);
        HibernateUtil.currentSession().save(feature);
        currentSession().flush();
        currentSession().refresh(feature);

        Publication publication = pubRepository.getPublication(featureDTO.getPublicationZdbID());
        String attributionZdbID = publication.getZdbID();
        RecordAttribution ra = new RecordAttribution();
        ra.setDataZdbID(feature.getZdbID());
        ra.setSourceZdbID(attributionZdbID);
        ra.setSourceType(RecordAttribution.SourceType.STANDARD);
        currentSession().save(ra);
        //add another record for "Feature type" source type attribution

        RecordAttribution recatt = new RecordAttribution();
        recatt.setDataZdbID(feature.getZdbID());
        recatt.setSourceZdbID(attributionZdbID);
        recatt.setSourceType(RecordAttribution.SourceType.FEATURE_TYPE);

        currentSession().save(recatt);

        /* if (publication != null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setSourceZdbID(attributionZdbID);
            pa.setDataZdbID(feature.getZdbID());
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            pa.setPublication(publication);
            Set<PublicationAttribution> pubattr = new HashSet<PublicationAttribution>();
            pubattr.add(pa);
            feature.setPublications(pubattr);
            currentSession().save(pa);
        }*/
        if (StringUtils.isNotEmpty(featureDTO.getAlias())) {
            FeatureAlias featureAlias = new FeatureAlias();
            featureAlias.setFeature(feature);
            String groupName = DataAliasGroup.Group.ALIAS.toString();
            DataAliasGroup group = infrastructureRepository.getDataAliasGroupByName(groupName);
            featureAlias.setAliasGroup(group);  //default for database, hibernate tries to insert null
            featureAlias.setAlias(featureDTO.getAlias());
            featureAlias.setAliasLowerCase(featureDTO.getAlias().toLowerCase());
//            featureAlias.setAliasLowerCase("tests");
            if (feature.getAliases() == null) {
                Set<FeatureAlias> featureAliases = new HashSet<FeatureAlias>();
                featureAliases.add(featureAlias);
                feature.setAliases(featureAliases);
            } else
                feature.getAliases().add(featureAlias);
            currentSession().save(featureAlias);
            //now handle alias attribution

            if (publication != null) {
                PublicationAttribution pa = new PublicationAttribution();
                pa.setSourceZdbID(attributionZdbID);
                pa.setDataZdbID(featureAlias.getZdbID());
                pa.setSourceType(RecordAttribution.SourceType.STANDARD);
                pa.setPublication(publication);
                Set<PublicationAttribution> pubattr = new HashSet<PublicationAttribution>();
                pubattr.add(pa);

                featureAlias.setPublications(pubattr);
                currentSession().save(pa);

            }

        }

        if (featureDTO.getCuratorNotes()!=null && featureDTO.getCuratorNotes().size()>0) {
            for(NoteDTO noteDTO: featureDTO.getCuratorNotes()){
                DataNote dnote = new DataNote();
                dnote.setDataZdbID(feature.getZdbID());

                dnote.setCurator(person);
                dnote.setDate(new Date());
                dnote.setNote(noteDTO.getNoteData());

                Set<DataNote> dataNotes = feature.getDataNotes();
                if (dataNotes == null) {
                    dataNotes = new HashSet<DataNote>();
                    dataNotes.add(dnote);
                    feature.setDataNotes(dataNotes);
                } else dataNotes.add(dnote);


                HibernateUtil.currentSession().save(dnote);
            }


        }

        if (featureDTO.getLabOfOrigin() != null) {
            Lab lab = (Lab) HibernateUtil.currentSession().get(Lab.class,featureDTO.getLabOfOrigin()) ;
            if (lab== null) {
                throw new RuntimeException("lab not found: " + featureDTO.getLabOfOrigin());
            }
            FeatureSource featureSource = new FeatureSource();
            featureSource.setOrganization(lab);
            featureSource.setDataZdbID(feature.getZdbID());

            Set<FeatureSource> featureSources = feature.getSources();
            if (featureSources == null) {
                featureSources = new HashSet<FeatureSource>();
                featureSources.add(featureSource);
                feature.setSources(featureSources);
            } else featureSources.add(featureSource);


            HibernateUtil.currentSession().save(featureSource);

        }


        FeatureAssay featureAssay = new FeatureAssay();
        featureAssay.setFeatzdbID(feature.getZdbID());
        if (featureDTO.getMutagen() == null) {
            featureAssay.setMutagen(Mutagen.NOT_SPECIFIED);
        }
        else{
            featureAssay.setMutagen(Mutagen.getType(featureDTO.getMutagen()));
        }
        if (featureDTO.getMutagee() == null) {
            featureAssay.setMutagee(Mutagee.NOT_SPECIFIED);
        }
        else{
            featureAssay.setMutagee(Mutagee.getType(featureDTO.getMutagee()));
        }
        feature.setFeatureAssay(featureAssay);
        HibernateUtil.currentSession().save(featureAssay);
        feature.setFeatureAssay(featureAssay);
        HibernateUtil.currentSession().update(feature);

        HibernateUtil.flushAndCommitCurrentSession();

        return getFeature(feature.getZdbID());
    }

    private void validateUnspecified(FeatureDTO featureDTO) throws ValidationException {
        if(featureDTO.getFeatureType()==FeatureTypeEnum.UNSPECIFIED){
            Marker m = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(featureDTO.getOptionalName()) ;
            if(m==null){
                throw new ValidationException("["+featureDTO.getOptionalName() + "] not found.  "
                        + MESSAGE_UNSPECIFIED_FEATURE
                ) ;
            }
            else
            if (false==m.getZdbID().startsWith("ZDB-GENE-")){
                throw new ValidationException("["+featureDTO.getOptionalName() + "] must be a gene.  "
                        + MESSAGE_UNSPECIFIED_FEATURE
                ) ;
            }
            if (RepositoryFactory.getInfrastructureRepository().getRecordAttribution(m.getZdbID(),featureDTO.getPublicationZdbID(), RecordAttribution.SourceType.STANDARD)
                    ==null){
                throw new ValidationException("The gene ["+featureDTO.getOptionalName() + "] must be attributed to this pub ["+featureDTO.getPublicationZdbID()  + "]. "
                        + MESSAGE_UNSPECIFIED_FEATURE) ;
            }
        }
    }


    public void deleteFeatureMarkerRelationship(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO){

        HibernateUtil.createTransaction();
        String zdbID = featureMarkerRelationshipDTO.getZdbID();
        infrastructureRepository.insertUpdatesTable(zdbID, "Feature", zdbID, "",
                "deleted feature/marker relationship between: "
                        +featureMarkerRelationshipDTO.getFeatureDTO().getAbbreviation()
                        +" and "
                        +featureMarkerRelationshipDTO.getMarkerDTO().getName()
                        +" of type "
                        +featureMarkerRelationshipDTO.getRelationshipType()
        );
        infrastructureRepository.deleteActiveDataByZdbID(zdbID);
        HibernateUtil.flushAndCommitCurrentSession();
    }




    public List<String> getRelationshipTypesForFeatureType(FeatureTypeEnum ftrTypeDisplay){
        return featureRepository.getRelationshipTypesForFeatureType(ftrTypeDisplay);
    }


    public List<MarkerDTO> getMarkersForFeatureRelationAndSource(String featureTypeName, String publicationZdbID){
        List<Marker> markers = featureRepository.getMarkersForFeatureRelationAndSource(featureTypeName,publicationZdbID);
        List<MarkerDTO> markerDTOs = new ArrayList<MarkerDTO>() ;
        for(Marker m : markers){
            markerDTOs.add(DTOConversionService.convertToMarkerDTO(m)) ;
        }
        return markerDTOs;
    }

    @Override
    public void addFeatureMarkerRelationShip(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO) {
        FeatureMarkerRelationship featureMarkerRelationship = new FeatureMarkerRelationship() ;

        FeatureDTO featureDTO = featureMarkerRelationshipDTO.getFeatureDTO() ;
        Feature feature = featureRepository.getFeatureByID(featureDTO.getZdbID()) ;
        featureMarkerRelationship.setFeature(feature);

        MarkerDTO markerDTO = featureMarkerRelationshipDTO.getMarkerDTO() ;
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerDTO.getZdbID()) ;
        featureMarkerRelationship.setMarker(marker);

        featureMarkerRelationship.setType(FeatureMarkerRelationshipTypeEnum.getType(featureMarkerRelationshipDTO.getRelationshipType()));

        HibernateUtil.createTransaction();
        HibernateUtil.currentSession().save(featureMarkerRelationship) ;
        infrastructureRepository.insertPublicAttribution(featureMarkerRelationship.getZdbID(), featureMarkerRelationshipDTO.getPublicationZdbID()) ;
        infrastructureRepository.insertUpdatesTable(featureMarkerRelationship.getZdbID(),"FeatureMarkerRelationship",featureMarkerRelationship.toString(),"Created feature marker relationship");
        HibernateUtil.flushAndCommitCurrentSession();
    }

    @Override
    public void editPublicNote(NoteDTO noteDTO) {
        HibernateUtil.createTransaction();
        Feature feature = featureRepository.getFeatureByID(noteDTO.getDataZdbID());
        String oldNote = feature.getPublicComments();
        String newNote = noteDTO.getNoteData();
        if (!StringUtils.equals(newNote, oldNote)) {
            feature.setPublicComments(noteDTO.getNoteData());
            infrastructureRepository.insertUpdatesTable(feature.getZdbID(), "Public Note", oldNote, newNote);
            HibernateUtil.currentSession().update(feature);
            HibernateUtil.flushAndCommitCurrentSession();
        }
    }

    @Override
    public NoteDTO addCuratorNote(NoteDTO noteDTO) {
        logger.info("adding curator note: " + noteDTO.getDataZdbID() + " - " + noteDTO.getNoteData());
        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        Feature feature = featureRepository.getFeatureByID(noteDTO.getDataZdbID());
        Person person = Person.getCurrentSecurityUser();

        // allows debugging
//        if(person==null){
//            person = (Person) HibernateUtil.currentSession().createCriteria(Person.class).setMaxResults(1).uniqueResult();
//        }
        DataNote dataNote = featureRepository.addFeatureDataNote(feature, noteDTO.getNoteData(), person);
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
}
