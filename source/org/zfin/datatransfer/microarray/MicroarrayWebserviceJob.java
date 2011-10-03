package org.zfin.datatransfer.microarray;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.expression.service.ExpressionService;
import org.zfin.expression.service.MicroarrayWebServiceBean;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

/**
 */
public class MicroarrayWebserviceJob implements Job {


    public static final String MICROARRAY_PUB = "ZDB-PUB-071218-1";
    private Logger logger = Logger.getLogger(MicroarrayWebserviceJob.class);

//    @Autowired
    private ExpressionService expressionService = new ExpressionService();


    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            GeoMicorarrayEntriesBean microEntriesBeans = NCBIEfetch.getMicroarraySequences();

            Set<String> validZdbIDs = new HashSet<String>();

            // abbrev , Marker zdbID
            Map<String, String> markerAbbrevMap = RepositoryFactory.getMarkerRepository().getGeoMarkerCandidates();
            Collection<String> validGeoSymbols = CollectionUtils.intersection(microEntriesBeans.getGeneSymbols(), markerAbbrevMap.keySet());
            for (String validGeoSymbol : validGeoSymbols) {
                validZdbIDs.add(markerAbbrevMap.get(validGeoSymbol));
            }
            validGeoSymbols = null;
            markerAbbrevMap = null;

            // accession,marker zdbID
            Map<String, String> possibleDblinkMap = RepositoryFactory.getSequenceRepository().getGeoAccessionCandidates();
            Collection<String> validGeoAccessions = CollectionUtils.intersection(microEntriesBeans.getAccessions(), possibleDblinkMap.keySet());
            for (String geoAccession : validGeoAccessions) {
                validZdbIDs.add(possibleDblinkMap.get(geoAccession));
            }
            possibleDblinkMap = null;
            validGeoAccessions = null;


            List<String> currentMarkerZdbIds = RepositoryFactory.getInfrastructureRepository().getPublicationAttributionsForPub(MICROARRAY_PUB);

            Collection<String> markersToAdd = CollectionUtils.subtract(validZdbIDs, currentMarkerZdbIds);
            Collection<String> markersToRemove = CollectionUtils.subtract(currentMarkerZdbIds, validZdbIDs);

            StringBuilder message = new StringBuilder();
            message.append("Markers added to Geo (" + markersToAdd.size() + "):\n");
            message.append("Markers removed from Geo ("+markersToRemove.size()+"):\n\n");

            message.append("Markers added to Geo (" + markersToAdd.size() + "):\n\n");
            for(String markerZdbID: markersToAdd){
                message.append(expressionService.getGeoLinkForMarkerZdbId(markerZdbID)).append("\n");
            }

            message.append("Markers removed from Geo (" + markersToRemove.size() + "):\n\n");
            for(String markerZdbID: markersToRemove){
                message.append(expressionService.getGeoLinkForMarkerZdbId(markerZdbID)).append("\n");
            }

            logger.info("adding: " + markersToAdd.size());
            logger.info("removing: " + markersToRemove.size());

            HibernateUtil.createTransaction();

            try {
                int removedAttributionsFromMarkerZdbIds = RepositoryFactory.getInfrastructureRepository().removeAttributionsNotFound(markersToRemove, MICROARRAY_PUB);
                int addedAttributionMarkerZdbIds = RepositoryFactory.getInfrastructureRepository().addAttributionsNotFound(markersToAdd, MICROARRAY_PUB);


                HibernateUtil.flushAndCommitCurrentSession();

                // TODO: email and stuff
                (new IntegratedJavaMailSender()).sendMail("microarray updates for: "+(new Date()).toString()
                        , message.toString(),
                        ZfinProperties.splitValues(ZfinPropertiesEnum.MICROARRAY_EMAIL));

            } catch (Exception e) {
                logger.error(e);
                HibernateUtil.rollbackTransaction();

                // TODO: email and stuff
                (new IntegratedJavaMailSender()).sendMail("ERROR in microarray update for: "+(new Date()).toString()
                        , "ERROR: " + e.fillInStackTrace().toString() +"\n"+message.toString(),
                        ZfinProperties.splitValues(ZfinPropertiesEnum.MICROARRAY_EMAIL));
            }
            finally {
                if(HibernateUtil.currentSession().isOpen()){
                    HibernateUtil.currentSession().close();
                }
            }




//            // get all genes
//            MicroarrayWebServiceBean microarrayWebServiceBean ;
//            microarrayWebServiceBean = expressionService.processMicroarrayRecordAttributionsForType(Marker.Type.GENE);
//            writeBean(microarrayWebServiceBean);
//
//            microarrayWebServiceBean = expressionService.processMicroarrayRecordAttributionsForType(Marker.Type.GENEP);
//            writeBean(microarrayWebServiceBean);
//
//            // get all cdna and clone
//            microarrayWebServiceBean = expressionService.processMicroarrayRecordAttributionsForType(Marker.Type.CDNA);
//            writeBean(microarrayWebServiceBean);
//
//            microarrayWebServiceBean = expressionService.processMicroarrayRecordAttributionsForType(Marker.Type.EST);
//            writeBean(microarrayWebServiceBean);
        } catch (Exception e) {
            // the error should already be logged
            logger.error(e);
        }

    }

    private void writeBean(MicroarrayWebServiceBean microarrayWebServiceBean) {
        HibernateUtil.createTransaction();
        try {
//            expressionService.writeMicroarrayWebServiceBean(microarrayWebServiceBean);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            logger.error("Failed to write bean out: ", e);
        }
    }


}
