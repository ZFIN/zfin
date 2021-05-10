package org.zfin.datatransfer.microarray;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.ReportGenerator;

import java.io.File;
import java.util.*;

/**
 */
public class MicroarrayWebserviceJob extends AbstractValidateDataReportTask {

    public static final String MICROARRAY_PUB = "ZDB-PUB-071218-1";

    private static Logger logger = LogManager.getLogger(MicroarrayWebserviceJob.class);

    private static final ExpressionService expressionService = new ExpressionService();

    public MicroarrayWebserviceJob(String jobName, String propertyDirectory, String baseDir) {
        super(jobName, propertyDirectory, baseDir);
    }

    @Override
    public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();


        GeoMicorarrayEntriesBean microEntriesBeans;
        try {
            microEntriesBeans = NCBIEfetch.getMicroarraySequences();
        } catch (Exception e) {
            throw new RuntimeException("Error getting Microarray sequences", e);
        }

        Set<String> validZdbIDs = new HashSet<>();

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
        ReportGenerator rg = new ReportGenerator();
        rg.setReportTitle("Report for " + jobName);
        rg.includeTimestamp();
        int exitCode = 0;
        try {
            RepositoryFactory.getInfrastructureRepository().removeAttributionsNotFound(markersToRemove, MICROARRAY_PUB);
            RepositoryFactory.getInfrastructureRepository().addAttributionsNotFound(markersToAdd, MICROARRAY_PUB);

            HibernateUtil.flushAndCommitCurrentSession();

            List<List<String>> addedMarkers = buildResultTable(markersToAdd);
            List<List<String>> removedMarkers = buildResultTable(markersToRemove);

            rg.addDataTable(addedMarkers.size() + " Markers Added to Geo",
                    Arrays.asList("Marker", "Geo Link"),
                    addedMarkers);
            rg.addDataTable(removedMarkers.size() + " Markers Removed from Geo",
                    Arrays.asList("Marker", "Geo Link"),
                    removedMarkers);
        } catch (Exception e) {
            logger.error(e);
            HibernateUtil.rollbackTransaction();
            rg.addErrorMessage("Error in microarray update");
            rg.addErrorMessage(e);
            exitCode = 1;
        } finally {
            if (HibernateUtil.currentSession().isOpen()) {
                HibernateUtil.currentSession().close();
            }
        }
        File reportDirectory = new File(dataDirectory, jobName);
        rg.writeFiles(reportDirectory, jobName);
        return exitCode;
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        MicroarrayWebserviceJob job = new MicroarrayWebserviceJob(args[2], args[0], args[1]);
        job.initDatabase();
        System.exit(job.execute());
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
