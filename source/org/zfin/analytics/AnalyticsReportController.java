package org.zfin.analytics;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.LookupStrings;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/analytics")
public class AnalyticsReportController {

    @RequestMapping(value = "/report/new")
    public String requestReport(Model model) {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Run Analytics Report");
        model.addAttribute("analyticsReportRequestForm", new AnalyticsReportRequestForm());
        model.addAttribute("reportNames", AnalyticsReportDefinitionList.getReportNames());
        return "analytics/new-report";
    }

    @RequestMapping(value = "/report/new", method = RequestMethod.POST)
    public String submitReport(@ModelAttribute AnalyticsReportRequestForm form, Model model, HttpServletResponse response) {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Analytics Report Request Received");
        model.addAttribute("analyticsReportRequestForm", form);

        AnalyticsReportDefinition reportDefinition = AnalyticsReportDefinitionList.get(form.getReportName());

        File reportDirectory = AnalyticsReportService.runReportWithDates(reportDefinition, form.getCredentials(), form.getStart(), form.getEnd());

        //send the reportDirectory (as a zip file) to the user

        if (reportDirectory != null && reportDirectory.isDirectory()) {
            try {
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=" + reportDirectory.getName() + ".zip");
                ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
                zipDirectory(reportDirectory, reportDirectory.getName(), zipOut);
                zipOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zipOut) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zipOut);
                continue;
            }
            FileInputStream fis = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(parentFolder + "/" + file.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
    }
}