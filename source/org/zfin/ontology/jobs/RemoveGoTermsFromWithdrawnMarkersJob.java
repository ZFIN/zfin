package org.zfin.ontology.jobs;


import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

public class RemoveGoTermsFromWithdrawnMarkersJob extends AbstractValidateDataReportTask {

    private static final Logger logger = LogManager.getLogger(RemoveGoTermsFromWithdrawnMarkersJob.class);

    public RemoveGoTermsFromWithdrawnMarkersJob(String jobName, String propertyDirectory, String baseDir) {
        super(jobName, propertyDirectory, baseDir);
    }

    @Override
    public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();

        List<MarkerGoTermEvidence> markerGoTermEvidenceRepositoryList =
                RepositoryFactory.getMarkerGoTermEvidenceRepository().getMarkerGoTermEvidencesForMarkerAbbreviation("WITHDRAWN%");

        if (CollectionUtils.isEmpty(markerGoTermEvidenceRepositoryList)) {
            logger.info("No MarkerGoTermEvidence's are related to a withdrawn marker");
            return 0;
        }

        HibernateUtil.createTransaction();
        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        List<String> errors = new ArrayList<>();
        try {
            for (MarkerGoTermEvidence markerGoTermEvidence : markerGoTermEvidenceRepositoryList) {
                infrastructureRepository.deleteActiveDataByZdbID(markerGoTermEvidence.getZdbID());
            }
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (HibernateException e) {
            HibernateUtil.rollbackTransaction();
            logger.error("Failed to delete MarkerGoTermEvidences", e);
            errors.add(e.getMessage());
        }

        List<List<String>> resultsDisplay = new ArrayList<>();
        for (MarkerGoTermEvidence evidence : markerGoTermEvidenceRepositoryList) {
            List<String> result = new ArrayList<>();
            result.add(evidence.getMarker().getZdbID());
            result.add(evidence.getSource().getZdbID());
            result.add(evidence.getGoTerm().getTermName());
            result.add(evidence.getEvidenceCode().getCode());
            resultsDisplay.add(result);
        }
        createErrorReport(errors, resultsDisplay);
        return errors.size();
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        RemoveGoTermsFromWithdrawnMarkersJob job = new RemoveGoTermsFromWithdrawnMarkersJob(args[2], args[0], args[1]);
        job.initDatabase();
        System.exit(job.execute());
    }

}
