package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.properties.ZfinProperties;

import java.util.List;

/**
 */
public class ValidateBlastDatabases extends QuartzJobBean {

    private Logger logger = Logger.getLogger(ValidateBlastDatabases.class);


    public void validateDatabase() {
        List<String> failures = MountedWublastBlastService.getInstance().validateAllPhysicalDatabasesReadable();
        if (CollectionUtils.isNotEmpty(failures)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to validate remote databases:\n");
            for (String failure : failures) {
                logger.error(failure);
                sb.append(failure).append("\n");
            }
            (new IntegratedJavaMailSender()).sendMail("Failed to validate " + failures.size() + " remote databases",
                    sb.toString(), ZfinProperties.getValidationOtherEmailAddresses());
        } else {
            logger.info("No failed databases found.");
        }
        HibernateUtil.closeSession();

    }

    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        validateDatabase();
    }


    public void initDatabase() {
        String[] hibernateConfiguration =
                new String[]{
                        "filters.hbm.xml",
                        "antibody.hbm.xml",
                        "anatomy.hbm.xml",
                        "blast.hbm.xml",
                        "expression.hbm.xml",
                        "general.hbm.xml",
                        "infrastructure.hbm.xml",
                        "mapping.hbm.xml",
                        "marker.hbm.xml",
                        "mutant.hbm.xml",
                        "orthology.hbm.xml",
                        "people.hbm.xml",
                        "publication.hbm.xml",
                        "reno.hbm.xml",
                        "sequence.hbm.xml",
                };

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator(false);

            ZfinProperties.init("test", "zfin-properties-test.xml");
        }
    }

    public static void main(String args[]) {
        ValidateBlastDatabases validateBlastDatabases = new ValidateBlastDatabases();
        validateBlastDatabases.initDatabase();
        validateBlastDatabases.validateDatabase();
    }
}