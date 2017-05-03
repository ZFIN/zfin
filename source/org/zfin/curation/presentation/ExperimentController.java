package org.zfin.curation.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.ui.experiment.ExperimentCurationService;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.ui.ValidationException;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

@Controller
@RequestMapping("/curation")
public class ExperimentController implements ExperimentCurationService {

    private final static Logger LOG = RootLogger.getLogger(ExperimentController.class);


    @Override
    @RequestMapping(value = "/{publicationID}/experiment-list", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<ExperimentDTO> getExperiments(@PathVariable String publicationID) throws ValidationException {

        List<ExperimentDTO> list = new ArrayList<>();
        HibernateUtil.createTransaction();
        try {

            List<Experiment> experimentSet = getExpressionRepository().geExperimentByPublication(publicationID);
            if (experimentSet != null) {
                for (Experiment experiment : experimentSet) {
                    ExperimentDTO dto = DTOConversionService.convertToEnvironmentTabDTO(experiment);
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
        return list;
    }

    @Override
    @RequestMapping(value = "/{publicationID}/create-condition", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<ExperimentDTO> createCondition(@PathVariable String publicationID,
                                               @RequestBody ConditionDTO conditionDTO) throws ValidationException, TermNotFoundException {
        if (org.apache.commons.lang3.StringUtils.isEmpty(publicationID))
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
            if (conditionDTO.chebiTerm != null) {
                GenericTerm term = DTOConversionService.convertToTerm(conditionDTO.chebiTerm);
                condition.setChebiTerm(term);
            }
            experiment.addExperimentCondition(condition);
            getExpressionRepository().saveExperimentCondition(condition);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }

        return getExperiments(publicationID);

    }


    @Override
    @RequestMapping(value = "/{experimentID}/copy-condition", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<ExperimentDTO> copyConditions(@PathVariable String experimentID,
                                              @RequestBody List<String> copyConditionIdList) throws ValidationException, TermNotFoundException {
        if (experimentID == null)
            throw new ValidationException("No experiment ID provided");
        if (CollectionUtils.isEmpty(copyConditionIdList))
            throw new ValidationException("No condition to be copied provided");
        Experiment experiment;
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
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            throw new TermNotFoundException(e.getMessage());
        }
        return getExperiments(experiment.getPublication().getZdbID());
    }

    @RequestMapping(value = "/{publicationID}/delete-experiment", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public List<ExperimentDTO> deleteExperiment(@PathVariable String publicationID,
                                                @RequestBody ExperimentDTO experimentDTO) throws ValidationException {
        if (experimentDTO == null)
            throw new ValidationException("No experiment entity provided");
        String experimentID = experimentDTO.getZdbID();
        if (experimentID == null)
            throw new ValidationException("No experimentID provided");
        String pubID = null;
        HibernateUtil.createTransaction();
        try {
            Experiment experiment = getExpressionRepository().getExperimentByID(experimentID);
            if (experiment == null) {
                throw new ValidationException("No experiment found for " + experimentID);
            }
            pubID = experiment.getPublication().getZdbID();
            if (!pubID.equals(publicationID))
                throw new ValidationException("Publication provided does not match the experiment");

            InfrastructureRepository infraRep = RepositoryFactory.getInfrastructureRepository();
            infraRep.deleteActiveDataByZdbID(experiment.getZdbID());


            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        }

        return getExperiments(publicationID);

    }

    @RequestMapping(value = "/{publicationID}/delete-condition", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public List<ExperimentDTO> deleteCondition(@PathVariable String publicationID,
                                                @RequestBody ConditionDTO conditionDTO) throws ValidationException, TermNotFoundException {
        if (conditionDTO == null)
            throw new ValidationException("No condition entity provided");
        String experimentID = conditionDTO.getEnvironmentZdbID();
        if (experimentID == null)
            throw new ValidationException("No experimentID provided");
        String pubID = null;
        HibernateUtil.createTransaction();
        try {
            Experiment experiment = getExpressionRepository().getExperimentByID(experimentID);
            if (experiment == null)
                throw new ValidationException("No experiment found for " + experimentID);
            pubID = experiment.getPublication().getZdbID();
            if (!pubID.equals(publicationID))
                throw new ValidationException("Publication provided does not match the experiment");
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

        return getExperiments(publicationID);
    }


    @RequestMapping(value = "/{publicationID}/create-experiment", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<ExperimentDTO> createExperiment(@PathVariable String publicationID,
                                                @RequestBody ExperimentDTO experimentDTO) throws ValidationException, TermNotFoundException {

        if (StringUtils.isEmpty(publicationID))
            throw new ValidationException("No Publication ID provided");

        HibernateUtil.createTransaction();
        try {
            Publication publication = getPublicationRepository().getPublication(publicationID);
            if (publication == null)
                throw new ValidationException("No Publication with ID found: " + publicationID);
            Experiment experiment = new Experiment();
            experiment.setPublication(publication);
            experiment.setName(experimentDTO.getName());
            getExpressionRepository().saveExperiment(experiment);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        }
        return getExperiments(publicationID);
    }

    @RequestMapping(value = "/{publicationID}/update-experiment", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<ExperimentDTO> updateExperiment(@PathVariable String publicationID,
                                                @RequestBody ExperimentDTO experimentDTO)
            throws ValidationException, TermNotFoundException {
        if (experimentDTO == null)
            throw new ValidationException("No experiment entity provided");
        String experimentID = experimentDTO.getZdbID();
        if (experimentID == null)
            throw new ValidationException("No experimentID provided");
        String pubID = null;
        String experimentName = experimentDTO.getName();
        HibernateUtil.createTransaction();
        try {
            Experiment experiment = getExpressionRepository().getExperimentByID(experimentID);
            if (experiment == null) {
                throw new ValidationException("No experiment found for " + experimentID);
            }
            pubID = experiment.getPublication().getZdbID();
            if (!pubID.equals(publicationID))
                throw new ValidationException("Publication provided does not match the experiment");
            experiment.setName(experimentName);
            getExpressionRepository().saveExperiment(experiment);


            HibernateUtil.flushAndCommitCurrentSession();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
        }

        return getExperiments(publicationID);
    }

    static Set<String> zecoRootTerms = new HashSet<>(10);

    static {
        zecoRootTerms.add("ZECO:0000229");
        zecoRootTerms.add("ZECO:0000111");
        zecoRootTerms.add("ZECO:0000239");
        zecoRootTerms.add("ZECO:0000176");
        zecoRootTerms.add("ZECO:0000143");
        zecoRootTerms.add("ZECO:0000105");
    }

    private HashMap<String, Set<String>> childMapZECO = new HashMap<>();

    @RequestMapping(value = "/zeco-child-map", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public HashMap<String, Set<String>> getZecoChildMap() {
        if (childMapZECO.size() > 0)
            return childMapZECO;
        for (String zecoTermRootID : zecoRootTerms) {
            GenericTerm term = getOntologyRepository().getTermByOboID(zecoTermRootID);
            Set<String> termSet = new HashSet<>();
            termSet.add(zecoTermRootID);
            for (GenericTerm childTerm : term.getAllChildren()) {
                termSet.add(childTerm.getOboID());
            }
            childMapZECO.put(zecoTermRootID, termSet);
        }
        return childMapZECO;
    }

}
