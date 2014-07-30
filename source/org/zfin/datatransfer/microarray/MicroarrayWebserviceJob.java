package org.zfin.datatransfer.microarray;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

/**
 */
public class MicroarrayWebserviceJob extends AbstractValidateDataReportTask {

    public static final String MICROARRAY_PUB = "ZDB-PUB-071218-1";

    private static Logger logger = Logger.getLogger(MicroarrayWebserviceJob.class);

    private static final ExpressionService expressionService = new ExpressionService();
    private List<List<String>> addedMarkers;
    private List<List<String>> removedMarkers;
    private String errorMessage;

    @Override
    protected void addCustomVariables(Map<String, Object> map) {
        super.addCustomVariables(map);
        map.put("addedMarkers", addedMarkers);
        map.put("removedMarkers", removedMarkers);
        map.put("errorMessage", errorMessage);
    }

    @Override
    public void execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();


        GeoMicorarrayEntriesBean microEntriesBeans;
        try {
            microEntriesBeans = NCBIEfetch.getMicroarraySequences();
        } catch (Exception e) {
            throw new RuntimeException("Error getting Microarray sequences", e);
        }

        Set<String> validZdbIDs = new HashSet<String>();

        // abbrev , Marker zdbID
        Map<String, String> markerAbbrevMap = RepositoryFactory.getMarkerRepository().getGeoMarkerCandidates();
        Collection<String> validGeoSymbols = CollectionUtils.intersection(microEntriesBeans.getGeneSymbols(), markerAbbrevMap.keySet());
        for (String validGeoSymbol : validGeoSymbols) {
            validZdbIDs.add(markerAbbrevMap.get(validGeoSymbol));
        }

        // accession,marker zdbID
        Map<String, String> possibleDblinkMap = RepositoryFactory.getSequenceRepository().getGeoAccessionCandidates();
        Collection<String> validGeoAccessions = CollectionUtils.intersection(microEntriesBeans.getAccessions(), possibleDblinkMap.keySet());
        for (String geoAccession : validGeoAccessions) {
            validZdbIDs.add(possibleDblinkMap.get(geoAccession));
        }

        List<String> currentMarkerZdbIds = RepositoryFactory.getInfrastructureRepository().getPublicationAttributionsForPub(MICROARRAY_PUB);

        Collection<String> markersToAdd = CollectionUtils.subtract(validZdbIDs, currentMarkerZdbIds);
        Collection<String> markersToRemove = CollectionUtils.subtract(currentMarkerZdbIds, validZdbIDs);

        logger.info("adding: " + markersToAdd.size());
        logger.info("removing: " + markersToRemove.size());

        HibernateUtil.createTransaction();

        try {
            RepositoryFactory.getInfrastructureRepository().removeAttributionsNotFound(markersToRemove, MICROARRAY_PUB);
            RepositoryFactory.getInfrastructureRepository().addAttributionsNotFound(markersToAdd, MICROARRAY_PUB);

            HibernateUtil.flushAndCommitCurrentSession();

            addedMarkers = buildResultTable(markersToAdd);
            removedMarkers = buildResultTable(markersToRemove);
        } catch (Exception e) {
            logger.error(e);
            HibernateUtil.rollbackTransaction();
            errorMessage = "Error in microarray update:\n" + ExceptionUtils.getFullStackTrace(e);
        } finally {
            if (HibernateUtil.currentSession().isOpen()) {
                HibernateUtil.currentSession().close();
            }
            ReportConfiguration config = new ReportConfiguration(jobName, dataDirectory, jobName, false);
            createErrorReport(null, null, config);
        }

    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        MicroarrayWebserviceJob job = new MicroarrayWebserviceJob();
        job.setPropertyFilePath(args[0]);
        job.setBaseDir(args[1]);
        job.setJobName(args[2]);
        job.init();
        job.execute();
    }

    private static List<List<String>> buildResultTable(Collection<String> markers) {
        List<List<String>> table = new ArrayList<>();
        for (String marker : markers) {
            List<String> row = new ArrayList<>();
            row.add(marker);
            row.add(expressionService.getGeoLinkForMarkerZdbId(marker));
            table.add(row);
        }
        return table;
    }

}
