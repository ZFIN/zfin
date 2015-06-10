package org.zfin.gwt.curation.server;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.zfin.ExternalNote;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.DiseaseModelDTO;
import org.zfin.gwt.curation.ui.CurationDiseaseRPC;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.*;


public class CurationDiseaseRPCImpl extends ZfinRemoteServiceServlet implements CurationDiseaseRPC {

    @Override
    public List<GenotypeDTO> getGenotypeList(String publicationID) {
        List<Genotype> genotypeList = getMutantRepository().getGenotypesForAttribution(publicationID);
        if (CollectionUtils.isEmpty(genotypeList))
            return null;
        List<GenotypeDTO> genotypeDTOList = new ArrayList<>(genotypeList.size());
        for (Genotype genotype : genotypeList)
            genotypeDTOList.add(DTOConversionService.convertToGenotypeDTO(genotype));
        return genotypeDTOList;
    }

    @Override
    public List<FeatureDTO> getFeatureList(String publicationID) {
        List<Feature> featureList = getFeatureRepository().getFeaturesByPublication(publicationID);
        if (CollectionUtils.isEmpty(featureList))
            return null;
        List<FeatureDTO> featureDTOList = new ArrayList<>(featureList.size());
        for (Feature feature : featureList)
            featureDTOList.add(DTOConversionService.convertToFeatureDTO(feature));
        return featureDTOList;
    }

    @Override
    public List<GenotypeDTO> searchGenotypes(String publicationID, String featureID, String genotypeID) {
        Feature feature = getFeatureRepository().getFeatureByID(featureID);
        Genotype background = getMutantRepository().getGenotypeByID(genotypeID);
        Publication publication = getPublicationRepository().getPublication(publicationID);
        List<Genotype> genotypeList = getMutantRepository().getGenotypesByFeatureAndBackground(feature, background, publication);
        List<GenotypeDTO> genotypeDTOList = new ArrayList<>(genotypeList.size());
        for (Genotype genotype : genotypeList)
            genotypeDTOList.add(DTOConversionService.convertToGenotypeDTO(genotype));
        return genotypeDTOList;
    }

    @Override
    public List<GenotypeDTO> addGenotypeToPublication(String publicationID, String genotypeID) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        try {
            Publication publication = getPublicationRepository().getPublication(publicationID);
            if (publication == null)
                throw new TermNotFoundException("No publication with ID: " + publicationID + " found");
            Genotype genotype = getMutantRepository().getGenotypeByID(genotypeID);
            if (genotype == null)
                throw new TermNotFoundException("No genotype with ID: " + genotypeID + " found");
            getInfrastructureRepository().insertPublicAttribution(genotypeID, publicationID, RecordAttribution.SourceType.STANDARD);
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
    public GenotypeDTO savePublicNote(String publicationID, GenotypeDTO genotypeDTO) throws TermNotFoundException {

        HibernateUtil.createTransaction();
        try {
            Publication publication = getPublicationRepository().getPublication(publicationID);
            if (publication == null)
                throw new TermNotFoundException("No publication with ID: " + publicationID + " found");
            String genotypeID = genotypeDTO.getZdbID();
            Genotype genotype = getMutantRepository().getGenotypeByID(genotypeID);
            if (genotype == null)
                throw new TermNotFoundException("No genotype with ID: " + genotypeID + " found");
            Set<GenotypeExternalNote> notes = genotype.getExternalNotes();
            GenotypeExternalNote note = new GenotypeExternalNote();
            if (CollectionUtils.isEmpty(notes)) {
                notes = new HashSet<>(1);
                note.setGenotype(genotype);
                notes.add(note);
            } else {
                // assumes that there is only one note for genotypes.
                note = notes.iterator().next();
            }
            note.setNote(genotypeDTO.getPublicNote());
            HibernateUtil.currentSession().saveOrUpdate(note);
            genotype.setExternalNotes(notes);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return genotypeDTO;
    }

    public List<TermDTO> getHumanDiseaseList(String publicationID) {
        List<GenericTerm> diseaseList = getPhenotypeRepository().getHumanDiseases(publicationID);

        List<TermDTO> dtoList = new ArrayList<>(diseaseList.size());
        for (GenericTerm term : diseaseList)
            dtoList.add(DTOConversionService.convertToTermDTO(term));
        return dtoList;
    }

    @Override
    public List<DiseaseModelDTO> getHumanDiseaseModelList(String publicationID) throws TermNotFoundException {
        List<DiseaseModelDTO> dtoList = PhenotypeService.getDiseaseModelDTOs(publicationID);
        return dtoList;
    }

    @Override
    public List<DiseaseModelDTO> addHumanDiseaseModel(DiseaseModelDTO diseaseModelDTO) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        DiseaseModel diseaseModel = null;
        try {
            diseaseModel = DTOConversionService.convertToDiseaseFromDiseaseDTO(diseaseModelDTO);
            getMutantRepository().createDiseaseModel(diseaseModel);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
            if (diseaseModel != null)
                throw new TermNotFoundException("Could not insert fish model [" + diseaseModel + "] as it already exists.");
            else
                throw new TermNotFoundException("Could not insert fish model");

        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return getHumanDiseaseModelList(diseaseModelDTO.getPublication().getZdbID());
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

    @Override
    public List<FishDTO> createFish(String publicationID, FishDTO newFish) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        try {
            Publication publication = getPublicationRepository().getPublication(publicationID);
            Fish fish = DTOConversionService.convertToFishFromFishDTO(newFish);
            PublicationAttribution attrib = new PublicationAttribution();
            attrib.setPublication(publication);
            attrib.setDataZdbID(fish.getZdbID());
            attrib.setSourceType(RecordAttribution.SourceType.STANDARD);
/*
            Boolean fishExists = getMutantRepository().existsAttribution(attrib);
            Fish existingFish = getMutantRepository().getFishByGenoStr(fish);
            if (existingFish != null)
                throw new TermNotFoundException("Fish already exists: " + existingFish.getName() + " [" + existingFish.getZdbID() + "]");

*/
            getMutantRepository().createFish(fish, publication);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }

        return getFishList(publicationID);
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
    public List<DiseaseModelDTO> deleteDiseaseModel(DiseaseModelDTO diseaseModelDTO) throws TermNotFoundException {
        if (diseaseModelDTO == null)
            throw new TermNotFoundException("No disease model found");
        if (diseaseModelDTO.getPublication() == null || diseaseModelDTO.getPublication().getZdbID() == null)
            throw new TermNotFoundException("No Publication found");

        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            DiseaseModel diseaseModel = getMutantRepository().getDiseaseModelByID(diseaseModelDTO.getID());
            if (diseaseModel == null)
                throw new TermNotFoundException("No disease model found ");
            getMutantRepository().deleteDiseaseModel(diseaseModel);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
        }
        return getHumanDiseaseModelList(diseaseModelDTO.getPublication().getZdbID());
    }

}
