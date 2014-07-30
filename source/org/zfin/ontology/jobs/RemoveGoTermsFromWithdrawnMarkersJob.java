package org.zfin.ontology.jobs;


import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

public class RemoveGoTermsFromWithdrawnMarkersJob extends AbstractValidateDataReportTask {

    private static final Logger logger = Logger.getLogger(RemoveGoTermsFromWithdrawnMarkersJob.class);

    @Override
    public void execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();

        List<MarkerGoTermEvidence> markerGoTermEvidenceRepositoryList =
                RepositoryFactory.getMarkerGoTermEvidenceRepository().getMarkerGoTermEvidencesForMarkerAbbreviation("WITHDRAWN%");

        if (CollectionUtils.isEmpty(markerGoTermEvidenceRepositoryList)) {
            logger.info("No MarkerGoTermEvidence's are related to a withdrawn marker");
            return;
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

        ReportConfiguration config = new ReportConfiguration(jobName, dataDirectory, "results", true);
        logger.info(config.getReportFile());
        List<List<String>> resultsDisplay = new ArrayList<>();
        for (MarkerGoTermEvidence evidence : markerGoTermEvidenceRepositoryList) {
            List<String> result = new ArrayList<>();
            result.add(evidence.getMarker().getZdbID());
            result.add(evidence.getSource().getZdbID());
            result.add(evidence.getGoTerm().getTermName());
            result.add(evidence.getEvidenceCode().getCode());
            resultsDisplay.add(result);
        }
        createErrorReport(errors, resultsDisplay, config);
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        RemoveGoTermsFromWithdrawnMarkersJob job = new RemoveGoTermsFromWithdrawnMarkersJob();
        job.setPropertyFilePath(args[0]);
        job.setBaseDir(args[1]);
        job.setJobName(args[2]);
        job.init();
        job.execute();
    }

}
