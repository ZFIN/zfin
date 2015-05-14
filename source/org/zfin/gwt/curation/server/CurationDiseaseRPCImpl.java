package org.zfin.gwt.curation.server;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.DiseaseModelDTO;
import org.zfin.gwt.curation.ui.CurationDiseaseRPC;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.mutant.DiseaseModel;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.mutant.Fish;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.*;


public class CurationDiseaseRPCImpl extends ZfinRemoteServiceServlet implements CurationDiseaseRPC {

    @Override
    public List<TermDTO> saveHumanDisease(TermDTO term, String publicationID) throws TermNotFoundException {
        if (term == null)
            throw new TermNotFoundException("No term provided");
        if (publicationID == null)
            throw new TermNotFoundException("No Publication found");

        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            GenericTerm gTerm = DTOConversionService.convertToTerm(term);
            getInfrastructureRepository().insertPublicAttribution(gTerm.getZdbID(), publicationID);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw new TermNotFoundException("Problem saving Human diesease: " + e.getMessage());
        }
        return getHumanDiseaseList(publicationID);
    }

    public List<TermDTO> getHumanDiseaseList(String publicationID) {
        List<GenericTerm> diseaseList = getPhenotypeRepository().getHumanDiseases(publicationID);

        List<TermDTO> dtoList = new ArrayList<>(diseaseList.size());
        for (GenericTerm term : diseaseList)
            dtoList.add(DTOConversionService.convertToTermDTO(term));
        return dtoList;
    }

    @Override
    public List<TermDTO> deleteHumanDisease(TermDTO term, String publicationID) throws TermNotFoundException {
        if (term == null)
            throw new TermNotFoundException("No term found");
        if (publicationID == null)
            throw new TermNotFoundException("No Publication found");

        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            GenericTerm gTerm = DTOConversionService.convertToTerm(term);
            getInfrastructureRepository().deleteRecordAttribution(gTerm.getZdbID(), publicationID);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
        }
        return getHumanDiseaseList(publicationID);
    }

    @Override
    public List<DiseaseModelDTO> getHumanDiseaseModelList(String publicationID) throws TermNotFoundException {
        List<DiseaseModel> diseaseModelList = getPhenotypeRepository().getHumanDiseaseModels(publicationID);
        List<DiseaseModelDTO> dtoList = new ArrayList<>();
        for (DiseaseModel diseaseModel : diseaseModelList)
            dtoList.add(DTOConversionService.convertToDiseaseModelDTO(diseaseModel));
        return dtoList;
    }

    @Override
    public List<DiseaseModelDTO> addHumanDiseaseModel(DiseaseModelDTO diseaseModelDTO) throws TermNotFoundException {
        HibernateUtil.createTransaction();
        try {
            DiseaseModel diseaseModel = DTOConversionService.convertToDiseaseFromDiseaseDTO(diseaseModelDTO);
            getMutantRepository().createDiseaseModel(diseaseModel);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
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
            Boolean fishExists = getMutantRepository().existsAttribution(attrib);
            if (!fishExists)
                getMutantRepository().createFish(fish, publication);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
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

}
