package org.zfin.ontology.datatransfer;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.framework.mail.MailSender;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to send out emails for a cron job.
 */
public class CronJobUtil {

    private String[] recipients;
    private MailSender smtpServer = AbstractZfinMailSender.getInstance();
    private Map<String, Object> objectMap = new HashMap<String, Object>();

    public CronJobUtil(String[] recipients) {
        this.recipients = recipients;
    }

    public void emailReport(String jobName, String message, AbstractScriptWrapper.ScriptExecutionStatus status) {
        emailReport(jobName, message, status, null);
    }

    public void emailReport(String jobName, String message, AbstractScriptWrapper.ScriptExecutionStatus status, String filename) {
        StringBuffer subject = new StringBuffer();
        subject.append("[");
        subject.append(status.toString());
        subject.append("]");
        subject.append("[");
        subject.append(jobName);
        subject.append("]");
        smtpServer.sendHtmlMail(subject.toString(), message, recipients, filename);
    }

    public void emailReport(String templateName, CronJobReport report) {
        emailReport(templateName, report, null);
    }

    public void emailReport(String templateName, CronJobReport report, String filename) {
        Configuration configuration = ZfinProperties.getTemplateConfiguration();
        StringWriter writer = new StringWriter();
        try {
            Template template = configuration.getTemplate(templateName);
            Map<String, Object> root = new HashMap<String, Object>();
            root.put("root", report);
            root.putAll(objectMap);
            template.process(root, writer);
            writer.flush();
        } catch (IOException e) {
            LOG.error(e);
            throw new RuntimeException("Error finding template file.", e);
        } catch (TemplateException e) {
            LOG.error(e);
            //throw new RuntimeException("Error while creating email body", e);
        }
        LOG.debug("Email Body:");
        LOG.debug(writer.getBuffer().toString());
        emailReport(report.getJobName(), writer.getBuffer().toString(), report.getStatus(), filename);
    }

    public void addObjectToTemplateMap(String name, Object object){
        objectMap.put(name, object);
    }

    private static final Logger LOG = LogManager.getLogger(CronJobUtil.class);
}
