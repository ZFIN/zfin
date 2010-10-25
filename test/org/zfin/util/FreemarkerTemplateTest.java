package org.zfin.util;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import org.junit.Test;
import org.zfin.ontology.datatransfer.CronJobReport;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * Test for using Freemarker template library.
 */
public class FreemarkerTemplateTest {

    @Test
    public void testFreeMarkerTemplate() throws Exception {

        Configuration config = new Configuration();
        File templateDirectory = new File(".");
        config.setDirectoryForTemplateLoading(templateDirectory);
        config.setObjectWrapper(new DefaultObjectWrapper());
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("user", "My humble self.");
        root.put("list", getNames());

        Template template = config.getTemplate("freeMarker-test.ftl");
        StringWriter writer = new StringWriter();

        template.process(root, writer);
        writer.flush();
        assertEquals("My humble self.\n" +
                "\n" +
                "ArrayList element 1\n" +
                "ArrayList element 2\n" +
                "ArrayList element 3\n" +
                "ArrayList element 4\n", writer.getBuffer().toString());
    }

    @Test
    public void newTermsAdded() throws Exception {

        Configuration config = new Configuration();
        File templateDirectory = FileUtil.createFileFromStrings("home", "WEB-INF", "templates");
        config.setDirectoryForTemplateLoading(templateDirectory);
        config.setObjectWrapper(new DefaultObjectWrapper());
        Map<String, Object> root = new HashMap<String, Object>();
        CronJobReport report = new CronJobReport("Test Job");
        List<String> columns = new ArrayList<String>();
        columns.add("GO:0061323");
        columns.add("cell proliferation involved in heart morphogenesis");
        List<String> columnRwoTwo = new ArrayList<String>();
        columnRwoTwo.add("GO:0061325");
        columnRwoTwo.add("cohesin localization to chromatin");
        List<List<String>> rows = new ArrayList<List<String>>();
        rows.add(columns);
        rows.add(columnRwoTwo);

        report.setRows(rows);
        root.put("root", report);

        Template template = config.getTemplate("ontology-loader-new-terms.ftl");
        StringWriter writer = new StringWriter();

        template.process(root, writer);
        writer.flush();
        assertEquals("<html>\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Pragma\" content=\"no-cache\"/>\n" +
                "    <meta http-equiv=\"Cache-Control\" content=\"no-cache\"/>\n" +
                "    <meta http-equiv=\"Expires\" content=\"0\"/>\n" +
                "    <title>\n" +
                "        Ontology Loader\n" +
                "    </title>\n" +
                "    <style type=\"text/css\">\n" +
                "        .bold {\n" +
                "            font-weight: bold;\n" +
                "            text-align: left;\n" +
                "        }\n" +
                "    </style>\n" +
                "\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<table>\n" +
                "    <tr>\n" +
                "        <td colspan=\"2\" class=\"bold\">Added new terms to ontology: 2</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>Seq</td>\n" +
                "        <td width=\"300\">Term Name</td>\n" +
                "        <td width=\"150\">Term ID</td>\n" +
                "    </tr>\n" +
                "        <tr>\n" +
                "            <td>1</td>\n" +
                "            <td>cell proliferation involved in heart morphogenesis</td>\n" +
                "            <td>GO:0061323</td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td>2</td>\n" +
                "            <td>cohesin localization to chromatin</td>\n" +
                "            <td>GO:0061325</td>\n" +
                "        </tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>", writer.getBuffer().toString());
    }

    @Test
    public void newReplacedPhenotypes() throws Exception {

        Configuration config = new Configuration();
        File templateDirectory = FileUtil.createFileFromStrings("home", "WEB-INF", "templates");
        config.setDirectoryForTemplateLoading(templateDirectory);
        config.setObjectWrapper(new DefaultObjectWrapper());
        Map<String, Object> root = new HashMap<String, Object>();
        CronJobReport report = new CronJobReport("Test Job");
        report.setDataSectionTitle("Replaced terms");
        List<String> columns = new ArrayList<String>();
        columns.add("ZDB-PHENO-100203-3");
        columns.add("ZDB-TERM-100203-3");
        columns.add("new term");
        columns.add("ZDB-TERM-100203-3");
        columns.add("old term");
        List<List<String>> rows = new ArrayList<List<String>>();
        rows.add(columns);

        report.setRows(rows);
        root.put("root", report);

        Template template = config.getTemplate("ontology-loader-report-replaced-terms.ftl");
        StringWriter writer = new StringWriter();

        template.process(root, writer);
        writer.flush();
        assertEquals("<html>\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Pragma\" content=\"no-cache\"/>\n" +
                "    <meta http-equiv=\"Cache-Control\" content=\"no-cache\"/>\n" +
                "    <meta http-equiv=\"Expires\" content=\"0\"/>\n" +
                "    <title>\n" +
                "        Ontology Loader\n" +
                "    </title>\n" +
                "    <style type=\"text/css\">\n" +
                "        .bold {\n" +
                "            font-weight: bold;\n" +
                "            text-align: left;\n" +
                "        }\n" +
                "    </style>\n" +
                "\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<table>\n" +
                "    <tr>\n" +
                "        <td colspan=\"2\" class=\"bold\">Added new terms to ontology: 2</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>Seq</td>\n" +
                "        <td width=\"300\">Term Name</td>\n" +
                "        <td width=\"150\">Term ID</td>\n" +
                "    </tr>\n" +
                "        <tr>\n" +
                "            <td>1</td>\n" +
                "            <td>cell proliferation involved in heart morphogenesis</td>\n" +
                "            <td>GO:0061323</td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td>2</td>\n" +
                "            <td>cohesin localization to chromatin</td>\n" +
                "            <td>GO:0061325</td>\n" +
                "        </tr>\n" +
                "</table>\n" +
                "\n" +
                "\n" +
                "</body>\n" +
                "</html>", writer.getBuffer().toString());
    }

    public List<String> getNames() {
        List<String> list = new ArrayList<String>();

        list.add("ArrayList element 1");
        list.add("ArrayList element 2");
        list.add("ArrayList element 3");
        list.add("ArrayList element 4");

        return list;

    }

}
