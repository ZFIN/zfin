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
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;


public class CurationDiseaseRPCImpl extends ZfinRemoteServiceServlet implements CurationDiseaseRPC {

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
            genotypeDTOList.add(DTOConversionService.convertToPureGenotypeDTOs(genotype));
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
    public ExternalNoteDTO savePublicNote(String publicationID, ExternalNoteDTO externalNoteDTO) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        try {
            Publication publication = getPublicationRepository().getPublication(publicationID);
            if (publication == null)
                throw new TermNotFoundException("No publication with ID: " + publicationID + " found");
            String noteID = externalNoteDTO.getZdbID();
            ExternalNote note = getInfrastructureRepository().getExternalNoteByID(noteID);
            if (note == null)
                throw new TermNotFoundException("No note with ID: " + noteID + " found");
            note.setNote(externalNoteDTO.getNoteData());
            HibernateUtil.currentSession().saveOrUpdate(note);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return externalNoteDTO;
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
            note.setType("genotype");
            note.setGenotype(genotype);
            note.setNote(text);
            getInfrastructureRepository().saveExternalNote(note, publication);
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
            Publication publication = getPublicationRepository().getPublication(publicationID);
            if (publication == null)
                throw new TermNotFoundException("No publication with ID: " + publicationID + " found");
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
    public CuratorNoteDTO saveCuratorNote(String publicationID, CuratorNoteDTO curatorNoteDTO) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        try {
            Publication publication = getPublicationRepository().getPublication(publicationID);
            if (publication == null)
                throw new TermNotFoundException("No publication with ID: " + publicationID + " found");
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
        return curatorNoteDTO;
    }

    @Override
    public List<GenotypeDTO> deleteCuratorNote(String publicationID, CuratorNoteDTO note) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        try {
            Publication publication = getPublicationRepository().getPublication(publicationID);
            if (publication == null)
                throw new TermNotFoundException("No publication with ID: " + publicationID + " found");
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
    public GenotypeDTO createGenotypeFeature(String publicationID, List<GenotypeFeatureDTO> genotypeFeatureDTOList, GenotypeDTO genotypeBackgroundDTO, String nickname)
            throws TermNotFoundException {
        Genotype genotype;
        HibernateUtil.createTransaction();
        try {
            Publication publication = getPublicationRepository().getPublication(publicationID);
            if (publication == null)
                throw new TermNotFoundException("No publication with ID: " + publicationID + " found");
            String backgroundGenotypeID = genotypeBackgroundDTO.getZdbID();
            Genotype genotypeBackground = getMutantRepository().getGenotypeByID(backgroundGenotypeID);
            if (genotypeBackground == null)
                throw new TermNotFoundException("No genotype with ID: " + backgroundGenotypeID + " found");
             genotype = GenotypeService.createGenotype(genotypeFeatureDTOList, genotypeBackground);
            if (nickname != null)
                genotype.setNickname(nickname);
            getMutantRepository().saveGenotype(genotype, publicationID);
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
        return DTOConversionService.convertToPureGenotypeDTOs(genotype);
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
