package org.zfin.gwt.curation.server;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.DiseaseModelDTO;
import org.zfin.gwt.curation.ui.CurationDiseaseRPC;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.ontology.GenericTerm;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;


/**
 * Created by cmpich on 3/27/15.
 */
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
        List<GenericTerm> diseaseList = getPhenotypeRepository().getHumanDiseases(publicationID);

        List<DiseaseModelDTO> dtoList = new ArrayList<>(diseaseList.size());
/*
        for (DiseaseModelDTO term : diseaseList)
            dtoList.add(DTOConversionService.convertToDiseaseDTO(term));
*/

        DiseaseModelDTO dto = new DiseaseModelDTO();
        GenotypeDTO genotypeDTO = new GenotypeDTO();
        genotypeDTO.setHandle("b577[2,U,U]");
        dto.setGenotype(genotypeDTO);
        EnvironmentDTO environmentDTO = new EnvironmentDTO();
        environmentDTO.setName("Standard");
        dto.setEnvironment(environmentDTO);
        TermDTO termDTO = new TermDTO();
        termDTO.setName("tetanus");
        dto.setTerm(termDTO);
        dtoList.add(dto);
        return null;
    }

    @Override
    public List<DiseaseModelDTO> addHumanDiseaseModel(DiseaseModelDTO diseaseModelDTO, String publicationID) throws TermNotFoundException {
        return getHumanDiseaseModelList(publicationID);
    }
}
