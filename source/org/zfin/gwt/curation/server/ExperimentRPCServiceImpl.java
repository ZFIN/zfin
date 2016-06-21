package org.zfin.gwt.curation.server;


import com.gargoylesoftware.htmlunit.javascript.host.Window;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.ui.ExperimentRPCService;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.gwt.root.ui.ValidationException;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class ExperimentRPCServiceImpl extends ZfinRemoteServiceServlet implements ExperimentRPCService {

    @Override
    public List<EnvironmentDTO> createCondition(String publicationID, ConditionDTO conditionDTO) throws ValidationException, TermNotFoundException {
        if (StringUtils.isEmpty(publicationID))
            throw new ValidationException("No Publication ID provided");
        if (conditionDTO == null)
            throw new ValidationException("No condition entity provided");
        String experimentID = conditionDTO.getEnvironmentZdbID();
        if (experimentID == null)
            throw new ValidationException("No experimentID provided");
        HibernateUtil.createTransaction();
        try {
            Experiment experiment = getExpressionRepository().getExperimentByID(experimentID);
            if (experiment == null)
                throw new ValidationException("No experiment found for " + experimentID);
            ExperimentCondition condition = new ExperimentCondition();
            condition.setExperiment(experiment);
            experiment.addExperimentCondition(condition);

            GenericTerm zecoTerm = DTOConversionService.convertToTerm(conditionDTO.zecoTerm);
            condition.setZecoTerm(zecoTerm);
            if (conditionDTO.aoTerm != null) {
                GenericTerm term = DTOConversionService.convertToTerm(conditionDTO.aoTerm);
                condition.setAoTerm(term);
            }
            if (conditionDTO.goCCTerm != null) {
                GenericTerm term = DTOConversionService.convertToTerm(conditionDTO.goCCTerm);
                condition.setGoCCTerm(term);
            }
            if (conditionDTO.taxonTerm != null) {
                GenericTerm term = DTOConversionService.convertToTerm(conditionDTO.taxonTerm);
                condition.setTaxaonymTerm(term);
            }
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }

        return getExperimentList(publicationID);
    }
    public List<EnvironmentDTO> createExperiment(String publicationID, EnvironmentDTO environmentDTO) throws ValidationException {
        com.google.gwt.user.client.Window.alert("createExp");
        com.google.gwt.user.client.Window.alert(publicationID);

        if (StringUtils.isEmpty(publicationID))
            throw new ValidationException("No Publication ID provided");



        HibernateUtil.createTransaction();
        try {
            Experiment experiment1 = getExpressionRepository().getExperimentByPubAndName(publicationID, environmentDTO.getName());
            if (experiment1==null) {
                Experiment experiment = new Experiment();

                experiment.setName("tryruityertuiytu");
                experiment.setPublication(getPublicationRepository().getPublication(publicationID));
            }
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        }
        return getExperimentList(publicationID);
    }

    @Override
    public List<EnvironmentDTO> deleteCondition(ConditionDTO conditionDTO) throws ValidationException, TermNotFoundException {
        if (conditionDTO == null)
            throw new ValidationException("No condition entity provided");
        String experimentID = conditionDTO.getEnvironmentZdbID();
        if (experimentID == null)
            throw new ValidationException("No experimentID provided");
        String publicationID = null;
        HibernateUtil.createTransaction();
        try {
            Experiment experiment = getExpressionRepository().getExperimentByID(experimentID);
            if (experiment == null)
                throw new ValidationException("No experiment found for " + experimentID);
            publicationID = experiment.getPublication().getZdbID();
            if (experiment.getExperimentConditions() != null) {
                for (ExperimentCondition condition : experiment.getExperimentConditions()) {
                    if (condition.getZdbID().equals(conditionDTO.getZdbID())) {
                        experiment.getExperimentConditions().remove(condition);
                        getExpressionRepository().deleteExperimentCondition(condition);
                        break;
                    }
                }
            }
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }

        return getExperimentList(publicationID);
    }
    public List<EnvironmentDTO> deleteExperiment(EnvironmentDTO experimentDTO) throws ValidationException {
        if (experimentDTO == null)
            throw new ValidationException("No experiment entity provided");
        String experimentID = experimentDTO.getZdbID();
        if (experimentID == null)
            throw new ValidationException("No experimentID provided");
        String publicationID = null;
        HibernateUtil.createTransaction();
        try {
            Experiment experiment = getExpressionRepository().getExperimentByID(experimentID);
            if (experiment == null) {
                throw new ValidationException("No experiment found for " + experimentID);
            }
            publicationID = experiment.getPublication().getZdbID();
            InfrastructureRepository infraRep = RepositoryFactory.getInfrastructureRepository();
            infraRep.deleteActiveDataByZdbID(experiment.getZdbID());




            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        }

        return getExperimentList(publicationID);
    }
    @Override
    public List<EnvironmentDTO> copyConditions(String experimentID, List<String> copyConditionIdList) throws ValidationException, TermNotFoundException {
        if (experimentID == null)
            throw new ValidationException("No experiment ID provided");
        if (CollectionUtils.isEmpty(copyConditionIdList))
            throw new ValidationException("No condition to be copied provided");
        Experiment experiment = null;
        HibernateUtil.createTransaction();
        try {
            experiment = getExpressionRepository().getExperimentByID(experimentID);
            if (experiment == null)
                throw new ValidationException("No experiment found for " + experimentID);
            for (String conditionID : copyConditionIdList) {
                ExperimentCondition condition = getExpressionRepository().getExperimentCondition(conditionID);
                boolean conditionExists = false;
                if (experiment.getExperimentConditions() != null) {
                    for (ExperimentCondition expCondition : experiment.getExperimentConditions()) {
                        if (expCondition.equals(condition))
                            conditionExists = true;
                    }
                }
                if (conditionExists)
                    continue;
                ExperimentCondition newCondition = new ExperimentCondition();
                newCondition.setZecoTerm(condition.getZecoTerm());
                newCondition.setAoTerm(condition.getAoTerm());
                newCondition.setGoCCTerm(condition.getGoCCTerm());
                newCondition.setTaxaonymTerm(condition.getTaxaonymTerm());
                newCondition.setChebiTerm(condition.getChebiTerm());
                newCondition.setExperiment(experiment);
                experiment.addExperimentCondition(newCondition);
            }
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return getExperimentList(experiment.getPublication().getZdbID());
    }

    @Override
    public List<EnvironmentDTO> getExperimentList(String publicationID) {
        com.google.gwt.user.client.Window.alert("gtgexplist");
        List<EnvironmentDTO> list = new ArrayList<>();
        List<Experiment> experimentSet = getExpressionRepository().geExperimentByPublication(publicationID);
        if (experimentSet != null) {
            for (Experiment experiment : experimentSet) {
                EnvironmentDTO dto = DTOConversionService.convertToEnvironmentDTO(experiment);
                list.add(dto);
            }
        }
        return list;
    }

}
