package org.zfin.infrastructure.ant;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 */
public class ReportConfigurationTest {


    @Test
    public void checkReportConfigWithDefaultTemplate() {
        String jobName = "New-Load_d";
        File templateDir = new File("test");
        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, templateDir, jobName, true);
        assertEquals("report.html.template", reportConfiguration.getTemplateFileName());
        assertEquals("New-Load_d.html", reportConfiguration.getReportFileName());
    }

    @Test
    public void checkReportConfigWithGivenTemplateFile() {
        String jobName = "New-Load_d";
        File templateDir = new File("test");
        // no template file provided
        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, templateDir, jobName, false);
        assertEquals("New-Load_d.html.template", reportConfiguration.getTemplateFileName());
        assertEquals("New-Load_d.html", reportConfiguration.getReportFileName());

        // full template file provided
        templateDir = new File("test", "job.html.template");
        reportConfiguration = new ReportConfiguration(jobName, templateDir, jobName, false);
        assertEquals("job.html.template", reportConfiguration.getTemplateFileName());
        assertEquals("job.html", reportConfiguration.getReportFileName());
    }
}
