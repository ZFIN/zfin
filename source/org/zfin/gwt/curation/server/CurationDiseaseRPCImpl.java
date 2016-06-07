package org.zfin.gwt.curation.server;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.zfin.ExternalNote;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.DiseaseAnnotationDTO;
import org.zfin.gwt.curation.dto.DiseaseAnnotationModelDTO;
import org.zfin.gwt.curation.ui.CurationDiseaseRPC;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.*;


public class CurationDiseaseRPCImpl extends ZfinRemoteServiceServlet implements CurationDiseaseRPC {

    private static Logger LOG = Logger.getLogger(CurationDiseaseRPCImpl.class);

    @Override
    public List<GenotypeDTO> getGenotypeList(String publicationID) {
        List<Genotype> genotypeList = getMutantRepository().getGenotypesForAttribution(publicationID);
        if (CollectionUtils.isEmpty(genotypeList))
            return null;
        List<GenotypeDTO> genotypeDTOList = new ArrayList<>(genotypeList.size());
        for (Genotype genotype : genotypeList)
            genotypeDTOList.add(DTOConversionService.convertToGenotypeDTOShallow(genotype));
        return genotypeDTOList;
    }

    @Override
    public List<FeatureDTO> getFeatureList(String publicationID) {
        List<Feature> featureList = getFeatureRepository().getFeaturesByPublication(publicationID);
        if (CollectionUtils.isEmpty(featureList))
            return null;
        List<FeatureDTO> featureDTOList = new ArrayList<>(featureList.size());
        for (Feature feature : featureList) {
            // do not include features that do not have a feature_marker_relationship
            if (CollectionUtils.isNotEmpty(feature.getFeatureMarkerRelations()))
                featureDTOList.add(DTOConversionService.convertToFeatureDTO(feature));
        }
        return featureDTOList;
    }

    @Override
    public List<GenotypeDTO> searchGenotypes(String publicationID, String featureID, String genotypeID) {
        Feature feature = null;
        if (featureID != null)
            feature = getFeatureRepository().getFeatureByID(featureID);
        Genotype background = null;
        if (genotypeID != null)
            background = getMutantRepository().getGenotypeByID(genotypeID);
        Publication publication = getPublicationRepository().getPublication(publicationID);
        List<Genotype> genotypeList = getMutantRepository().getGenotypesByFeatureAndBackground(feature, background, publication);
        List<GenotypeDTO> genotypeDTOList = new ArrayList<>(genotypeList.size());
        for (Genotype genotype : genotypeList)
            genotypeDTOList.add(DTOConversionService.convertToPureGenotypeDTOs(genotype));
        return genotypeDTOList;
    }

    @Override
    public GenotypeDTO addGenotypeToPublication(String publicationID, String genotypeID) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        GenotypeDTO genotypeDTO = null;
        try {
            Publication publication = getPublicationRepository().getPublication(publicationID);
            if (publication == null)
                throw new TermNotFoundException("No publication with ID: " + publicationID + " found");
            Genotype genotype = getMutantRepository().getGenotypeByID(genotypeID);
            if (genotype == null)
                throw new TermNotFoundException("No genotype with ID: " + genotypeID + " found");
            getInfrastructureRepository().insertPublicAttribution(genotype, publication);
            HibernateUtil.flushAndCommitCurrentSession();
            genotypeDTO = DTOConversionService.convertToGenotypeDTO(genotype);
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return genotypeDTO;
    }

    @Override
    public List<GenotypeDTO> savePublicNote(String publicationID, ExternalNoteDTO externalNoteDTO) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        try {
            String noteID = externalNoteDTO.getZdbID();
            ExternalNote note = getInfrastructureRepository().getExternalNoteByID(noteID);
            if (note == null)
                throw new TermNotFoundException("No note with ID: " + noteID + " found");
            note.setNote(externalNoteDTO.getNoteData());
            note.setType("genotype");
            HibernateUtil.currentSession().saveOrUpdate(note);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return getGenotypeList(publicationID);
    }

    @Override
    public List<GenotypeDTO> createPublicNote(String publicationID, GenotypeDTO genotypeDTO, String text) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        try {
            Publication publication = getPublicationRepository().getPublication(publicationID);
            if (publication == null)
                throw new TermNotFoundException("No publication with ID: " + publicationID + " found");
            String genotypeID = genotypeDTO.getZdbID();
            Genotype genotype = getMutantRepository().getGenotypeByID(genotypeID);
            if (genotype == null)
                throw new TermNotFoundException("No genotype with ID: " + genotypeID + " found");
            GenotypeExternalNote note = new GenotypeExternalNote();
            note.setGenotype(genotype);
            note.setNote(text);
            note.setPublication(publication);
            genotype.addExternalNote(note);
            HibernateUtil.currentSession().save(note);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return getGenotypeList(publicationID);
    }

    @Override
    public List<GenotypeDTO> deletePublicNote(String publicationID, ExternalNoteDTO extNote) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        try {
            String noteID = extNote.getZdbID();
            getInfrastructureRepository().deleteActiveDataByZdbID(noteID);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return getGenotypeList(publicationID);
    }

    @Override
    public List<GenotypeDTO> saveCuratorNote(String publicationID, CuratorNoteDTO curatorNoteDTO) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        try {
            String noteID = curatorNoteDTO.getZdbID();
            DataNote note = getInfrastructureRepository().getDataNoteByID(noteID);
            if (note == null)
                throw new TermNotFoundException("No note with ID: " + noteID + " found");
            note.setNote(curatorNoteDTO.getNoteData());
            HibernateUtil.currentSession().saveOrUpdate(note);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return getGenotypeList(publicationID);
    }

    @Override
    public List<GenotypeDTO> deleteCuratorNote(String publicationID, CuratorNoteDTO note) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        try {
            String noteID = note.getZdbID();
            getInfrastructureRepository().deleteActiveDataByZdbID(noteID);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return getGenotypeList(publicationID);
    }

    @Override
    public List<GenotypeDTO> createCuratorNote(String publicationID, GenotypeDTO genotypeDTO, String text) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        try {
            Publication publication = getPublicationRepository().getPublication(publicationID);
            if (publication == null)
                throw new TermNotFoundException("No publication with ID: " + publicationID + " found");
            String genotypeID = genotypeDTO.getZdbID();
            Genotype genotype = getMutantRepository().getGenotypeByID(genotypeID);
            if (genotype == null)
                throw new TermNotFoundException("No genotype with ID: " + genotypeID + " found");
            DataNote note = new DataNote();
            note.setDataZdbID(genotypeID);
            note.setNote(text);
            getInfrastructureRepository().saveDataNote(note, publication);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return getGenotypeList(publicationID);
    }

    @Override
    public List<ZygosityDTO> getZygosityLists() {
        List<Zygosity> zygosityList = getMutantRepository().getListOfZygosity();
        List<ZygosityDTO> dotList = new ArrayList<>(zygosityList.size());
        for (Zygosity zygosity : zygosityList)
            dotList.add(DTOConversionService.convertToZygosityDTO(zygosity));
        return dotList;
    }

    @Override
    public GenotypeCreationReportDTO createGenotypeFish(String publicationID, List<GenotypeFeatureDTO> genotypeFeatureDTOList, List<GenotypeDTO> genotypeBackgroundDTOList, Set<RelatedEntityDTO> strSet)
            throws TermNotFoundException {
        Genotype genotype;
        HibernateUtil.createTransaction();
        GenotypeCreationReportDTO report = new GenotypeCreationReportDTO();
        try {
            Publication publication = getPublicationRepository().getPublication(publicationID);
            if (publication == null)
                throw new TermNotFoundException("No publication with ID: " + publicationID + " found");

            List<Genotype> genotypeBackgroundList = new ArrayList<>(3);
            if (genotypeBackgroundDTOList != null) {
                for (GenotypeDTO genotypeBackgroundDTO : genotypeBackgroundDTOList) {
                    if (genotypeBackgroundDTO != null && genotypeBackgroundDTO.getZdbID() != null) {
                        Genotype genotypeBackground = getMutantRepository().getGenotypeByID(genotypeBackgroundDTO.getZdbID());
                        if (genotypeBackground == null)
                            throw new TermNotFoundException("No genotype with ID: " + genotypeBackgroundDTO.getZdbID() + " found");
                        genotypeBackgroundList.add(genotypeBackground);
                    }
                }
            }
            genotype = GenotypeService.createGenotype(genotypeFeatureDTOList, genotypeBackgroundList);

            // check if the genotype already exists
            Genotype existentGenotype = getMutantRepository().getGenotypeByHandle(genotype.getHandle());
            if (existentGenotype != null) {
                genotype = existentGenotype;
                PublicationAttribution attribution = new PublicationAttribution();
                attribution.setPublication(publication);
                attribution.setDataZdbID(existentGenotype.getZdbID());
                PublicationAttribution publicationAttribution = getInfrastructureRepository().getPublicationAttribution(attribution);
                if (publicationAttribution == null) {
                    getInfrastructureRepository().insertPublicAttribution(existentGenotype.getZdbID(), publicationID);
                    report.setReportMessage("Imported Genotype " + genotype.getHandle());
                }
            } else {
                getMutantRepository().saveGenotype(genotype, publicationID);
                report.setReportMessage("Created Genotype " + genotype.getHandle());
            }
            FishDTO fishDTO = new FishDTO();
            fishDTO.setStrList((new ArrayList<>(strSet)));
            GenotypeDTO genoDTO = new GenotypeDTO();
            genoDTO.setZdbID(genotype.getZdbID());
            genoDTO.setName(genotype.getName());
            fishDTO.setGenotypeDTO(genoDTO);
            createFish(publication, fishDTO, report);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
            String message = e.getMessage();
            message += "\r\n";
            message += e.getCause().getMessage();
            throw new TermNotFoundException(message);
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        report.setGenotypeDTO(DTOConversionService.convertToPureGenotypeDTOs(genotype));
        return report;
    }


    public List<TermDTO> getHumanDiseaseList(String publicationID) {
        List<GenericTerm> diseaseList = getPhenotypeRepository().getHumanDiseases(publicationID);

        List<TermDTO> dtoList = new ArrayList<>(diseaseList.size());
        for (GenericTerm term : diseaseList)
            dtoList.add(DTOConversionService.convertToTermDTO(term));
        return dtoList;
    }

    @Override
    public List<DiseaseAnnotationDTO> getHumanDiseaseModelList(String publicationID) throws TermNotFoundException {
        return PhenotypeService.getDiseaseModelDTOs(publicationID);
    }

    @Override
    public List<DiseaseAnnotationDTO> addHumanDiseaseAnnotation(DiseaseAnnotationDTO diseaseAnnotationDTO) throws TermNotFoundException {

        DiseaseAnnotation diseaseAnnotation = null;
        DiseaseAnnotationModel dam = null;
        try {
            HibernateUtil.createTransaction();
            diseaseAnnotation = DTOConversionService.convertToDiseaseFromDiseaseDTO(diseaseAnnotationDTO);


            if (diseaseAnnotationDTO.getFish() != null && diseaseAnnotationDTO.getFish().getZdbID() != null) {
                getMutantRepository().createDiseaseModel(diseaseAnnotation);
                dam = DTOConversionService.convertToDiseaseModelFromDiseaseDTO(diseaseAnnotationDTO);
                FishExperiment existingModel = getMutantRepository().getFishModel(dam.getFishExperiment().getFish().getZdbID(),
                        dam.getFishExperiment().getExperiment().getZdbID());
                if (existingModel == null) {
                    dam.setFishExperiment(dam.getFishExperiment());
                    HibernateUtil.currentSession().save(dam.getFishExperiment());

                } else {
                    dam.setFishExperiment(existingModel);
                }
                DiseaseAnnotation existingDiseaseAnnotation = getMutantRepository().getDiseaseModel(diseaseAnnotation);
                dam.setDiseaseAnnotation(existingDiseaseAnnotation);
                HibernateUtil.currentSession().save(dam);
            } else {
                getMutantRepository().createDiseaseModel(diseaseAnnotation);
            }
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();

            if (diseaseAnnotation != null)
                throw new TermNotFoundException("Could not insert fish model [" + diseaseAnnotation + "] as it already exists.");
            else
                throw new TermNotFoundException("Could not insert fish model");

        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return getHumanDiseaseModelList(diseaseAnnotationDTO.getPublication().getZdbID());

    }


    @Override
    public List<RelatedEntityDTO> getStrList(String publicationID) {
        List<SequenceTargetingReagent> reagentList = getMutantRepository().getStrList(publicationID);
        if (reagentList == null)
            return null;
        List<RelatedEntityDTO> dtoList = new ArrayList<>(reagentList.size());
        for (SequenceTargetingReagent str : reagentList)
            dtoList.add(DTOConversionService.convertStrToRelatedEntityDTO(str));
        return dtoList;
    }

    protected void createFish(Publication publication, FishDTO newFish, GenotypeCreationReportDTO report) throws TermNotFoundException {
        Fish fish = DTOConversionService.convertToFishFromFishDTO(newFish);
        if (getMutantRepository().createFish(fish, publication)) {
            report.addMessage("created new fish " + fish.getHandle());
        } else {
            report.addMessage("imported fish " + fish.getHandle());
        }
    }

    @Override
    public List<FishDTO> getFishList(String publicationID) {
        List<Fish> fishList = getMutantRepository().getFishList(publicationID);
        if (CollectionUtils.isEmpty(fishList))
            return null;
        List<FishDTO> fishDtoList = new ArrayList<>(fishList.size());
        for (Fish fish : fishList)
            fishDtoList.add(DTOConversionService.convertToFishDtoFromFish(fish));
        return fishDtoList;
    }

    @Override
    public List<DiseaseAnnotationDTO> deleteDiseaseModel(DiseaseAnnotationDTO diseaseAnnotationDTO) throws TermNotFoundException {
        if (diseaseAnnotationDTO == null)
            throw new TermNotFoundException("No disease model found");
        if (diseaseAnnotationDTO.getPublication() == null || diseaseAnnotationDTO.getPublication().getZdbID() == null)
            throw new TermNotFoundException("No Publication found");

        Transaction tx = HibernateUtil.currentSession().beginTransaction();

        try {
            DiseaseAnnotation diseaseAnnotation = getMutantRepository().getDiseaseModelByID(diseaseAnnotationDTO.getZdbID());
            if (diseaseAnnotation == null)
                throw new TermNotFoundException("No disease model found ");

            getMutantRepository().deleteDiseaseModel(diseaseAnnotation);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
        }
        return getHumanDiseaseModelList(diseaseAnnotationDTO.getPublication().getZdbID());
    }

    public List<DiseaseAnnotationDTO> deleteDiseaseAnnotationModel(DiseaseAnnotationModelDTO diseaseAnnotationModelDTO) throws TermNotFoundException {
        if (diseaseAnnotationModelDTO == null)
            throw new TermNotFoundException("No disease model found");
        DiseaseAnnotationModel diseaseAnnotationModel = getMutantRepository().getDiseaseAnnotationModelByID(diseaseAnnotationModelDTO.getDamoID());
        DiseaseAnnotation dA = new DiseaseAnnotation();
        if (diseaseAnnotationModel != null) {
            dA = diseaseAnnotationModel.getDiseaseAnnotation();
        }

        HibernateUtil.createTransaction();
        try {


            getMutantRepository().deleteDiseaseAnnotationModel(diseaseAnnotationModel);

            HibernateUtil.flushAndCommitCurrentSession();
        } catch (HibernateException e) {
            HibernateUtil.rollbackTransaction();
        }
        return getHumanDiseaseModelList(dA.getPublication().getZdbID());
    }
}
