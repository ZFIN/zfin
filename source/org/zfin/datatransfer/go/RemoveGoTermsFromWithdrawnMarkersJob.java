package org.zfin.datatransfer.go;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import java.util.Date;
import java.util.List;

/**
 * Converted from cron job in validatedata.pl removeGOTermsFromWithdrawnMarkers vai fogbugz 6109.
 */
public class RemoveGoTermsFromWithdrawnMarkersJob implements Job {

    private Logger logger = Logger.getLogger(RemoveGoTermsFromWithdrawnMarkersJob.class) ;
    private final static String TAB = "\t" ;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        List<MarkerGoTermEvidence> markerGoTermEvidenceRepositoryList =
                RepositoryFactory.getMarkerGoTermEvidenceRepository().getMarkerGoTermEvidencesForMarkerAbbreviation("WITHDRAWN%") ;

        if(CollectionUtils.isEmpty(markerGoTermEvidenceRepositoryList)) {
            logger.info("No MarkerGoTermEvidence's are related to a withdrawn marker");
            return ;
        }


        String report = createReport(markerGoTermEvidenceRepositoryList);

        HibernateUtil.createTransaction() ;
        try {
            for(MarkerGoTermEvidence markerGoTermEvidence : markerGoTermEvidenceRepositoryList){
                HibernateUtil.currentSession().delete(markerGoTermEvidence);
            }
            HibernateUtil.flushAndCommitCurrentSession() ;
            (new IntegratedJavaMailSender()).sendMail("removeGOTermsFromWithdrawnMarkers - Go annotations have been removed from withdrawn markers: "+(new Date()).toString()
                    , report,
                    ZfinProperties.splitValues(ZfinPropertiesEnum.VALIDATION_EMAIL_MUTANT));
        } catch (HibernateException e) {
            HibernateUtil.rollbackTransaction();
            logger.error("Failed to delete MarkerGoTermEvidence's",e);
            return ;
        }


    }


    private String createReport(List<MarkerGoTermEvidence> markerGoTermEvidences) {
        StringBuilder sb = new StringBuilder() ;
        sb.append("There are "+ markerGoTermEvidences.size()+
                "  GO annotation(s) that have been removed because " +
                "their referenced genes have been withdrawn. " +
                "There may be some duplicates because we are not including " +
                "inferred from information in this report. ");
        sb.append("\n") ;

        sb.append("marker-zdb-id").append(TAB).append("pub-id").append(TAB)
        .append("goterm-name").append(TAB).append("evidence-code") ;
        sb.append("\n") ;

        for(MarkerGoTermEvidence markerGoTermEvidence : markerGoTermEvidences){
            sb.append(markerGoTermEvidence.getMarker().getZdbID()).append(TAB)
                    .append(markerGoTermEvidence.getSource().getZdbID()).append(TAB)
                    .append(markerGoTermEvidence.getGoTerm().getTermName()).append(TAB)
                    .append(markerGoTermEvidence.getEvidenceCode().getCode()) ;
            sb.append("\n") ;
        }

        return sb.toString() ;
    }
}
