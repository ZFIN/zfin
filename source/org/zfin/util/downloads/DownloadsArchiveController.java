package org.zfin.util.downloads;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.util.FileUtil;
import org.zfin.util.database.presentation.UnloadBean;
import org.zfin.util.downloads.jaxb.DownloadFileEntry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller class that serves the site search index page.
 */
@Controller
@RequestMapping("/unload")
public class DownloadsArchiveController {

    private static final Logger LOG = LogManager.getLogger(DownloadsArchiveController.class);
    public static final String NEWLINE_CHARACTER = System.getProperty("line.separator");

    @Autowired
    private DownloadFileService downloadFileService;

    @Autowired
    HttpServletRequest httpServletRequest;

    @ModelAttribute("formBean")
    private UnloadBean getDefaultBean() {
        UnloadBean unloadBean = new UnloadBean();
        unloadBean.setDownloadFileService(downloadFileService);
        unloadBean.setRequest(httpServletRequest.getRequestURI());
        return unloadBean;
    }

    @RequestMapping(value = "/downloads/archive")
    public String archiveSummary(Model model) throws Exception {
        LOG.info("Start Detail Controller");

        if (hasErrors())
            return "unload/download-error-message";
        model.addAttribute(LookupStrings.FORM_BEAN, getDefaultBean());
        return "unload/download-summary";
    }

    private boolean hasErrors() {
        return !downloadFileService.isDownloadArchiveExists() || !downloadFileService.isValidArchiveFound();
    }

    @RequestMapping(value = "/downloads/update-cache")
    public String updateCache(Model model) throws Exception {
        downloadFileService.updateCache();
        return archiveSummary(model);
    }

    @RequestMapping(value = "/downloads/archive/{year}.{month}.{day}")
    public String getArchivedDownloads(Model model,
                                       @PathVariable String year,
                                       @PathVariable String month,
                                       @PathVariable String day,
                                       @ModelAttribute("formBean") UnloadBean formBean) throws Exception {
        // no archives found at all?
        if (!downloadFileService.isDownloadArchiveExists())
            return "unload/download-error-message";
        String date = year + "." + month + "." + day;
        formBean.setDate(date);
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, date);
        return "unload/download-date-summary";
    }

    @RequestMapping(value = "/downloads/{fileName}.{extension}")
    public String viewCurrentFile(Model model,
                                  @PathVariable String fileName,
                                  @PathVariable String extension,
                                  @ModelAttribute("formBean") UnloadBean formBean) {

        if (hasErrors())
            return "unload/download-error-message";
        String currentDate = downloadFileService.getMatchingIndexDirectory();
        String fullFileName = fileName + "." + extension;
        File file = downloadFileService.getFile(fullFileName, currentDate);
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        File dFile = FileUtil.createFile("data-transfer", currentDate, file.getName());
        formBean.setFileName("/" + dFile.getPath());
        return "unload/view-download-file";
    }

    @RequestMapping(value = "/downloads/archive/{year}.{month}.{day}/{fileName}.{extension}")
    public String viewArchiveFile(Model model,
                                  @PathVariable String year,
                                  @PathVariable String month,
                                  @PathVariable String day,
                                  @PathVariable String fileName,
                                  @PathVariable String extension,
                                  @ModelAttribute("formBean") UnloadBean formBean) {

        // no archives found at all?
        if (!downloadFileService.isDownloadArchiveExists())
            return "unload/download-error-message";
        String fullFileName = fileName + "." + extension;
        String archiveDate = year + "." + month + "." + day;
        File file = downloadFileService.getFile(fullFileName, archiveDate);
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        File dFile = FileUtil.createFile("data-transfer", archiveDate, file.getName());
        formBean.setFileName("/" + dFile.getPath());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, archiveDate);
        return "unload/view-download-file";
    }

    @RequestMapping(value = "/downloads/file/{fileName}.{extension}")
    public String downloadFile(Model model,
                               @PathVariable String fileName,
                               @PathVariable String extension,
                               @ModelAttribute("formBean") UnloadBean formBean,
                               HttpServletResponse response) throws Exception {
        if (hasErrors())
            return "unload/download-error-message";
        String currentDate = downloadFileService.getMatchingIndexDirectory();
        String[] dateArray = currentDate.split("\\.");
        return downloadArchiveFile(model, dateArray[0], dateArray[1], dateArray[2], fileName, extension, formBean, response);
    }

    @RequestMapping(value = "/downloads/archive/{year}.{month}.{day}/file/{fileName}.{extension}")
    public String downloadArchiveFile(Model model,
                                      @PathVariable String year,
                                      @PathVariable String month,
                                      @PathVariable String day,
                                      @PathVariable String fileName,
                                      @PathVariable String extension,
                                      @ModelAttribute("formBean") UnloadBean formBean,
                                      HttpServletResponse response) throws Exception {
        // no archives found at all?
        if (!downloadFileService.isDownloadArchiveExists())
            return "unload/download-error-message";
        String fullFileName = fileName + "." + extension;
        String archiveDate = year + "." + month + "." + day;
        DownloadFileEntry downloadFile = downloadFileService.getDownloadFile(fullFileName, archiveDate);
        File file = downloadFileService.getFile(fullFileName, archiveDate);
        String formattedFileName = FileUtil.addTimeStampToFileName(downloadFile.getFullFileName(), archiveDate);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + formattedFileName + "\"");
        response.setContentType("application/" + downloadFile.getFileExtension());
        List<InputStream> fullStream = new ArrayList<InputStream>(5);
        String headerLine = downloadFile.getHeaderLine("\t");
        String timestamp = "";
        if (downloadFile.getFileFormat().equalsIgnoreCase(FileFormat.GFF3.toString())) {
            timestamp += "# ";
            headerLine = "# " + headerLine;
        }
        timestamp += "Date: " + archiveDate;
        fullStream.add(new ByteArrayInputStream(timestamp.getBytes()));
        fullStream.add(new ByteArrayInputStream(NEWLINE_CHARACTER.getBytes()));
        fullStream.add(new ByteArrayInputStream(headerLine.getBytes()));
        fullStream.add(new ByteArrayInputStream(NEWLINE_CHARACTER.getBytes()));
        fullStream.add(new FileInputStream(file));
        /*
        *         InputStream in = new FileInputStream(file);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096]; // some large number - pick one
        for (int size; ((size = in.read(buffer)) != -1); )
            byteOut.write(buffer, 0, size);
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        InputStream in1 = StreamUtil.replaceStringsInStream(byteIn, "\t", ",");
        fullStream.add(in1);
*/
        InputStream is = new SequenceInputStream(Collections.enumeration(fullStream));
        IOUtils.copy(is, response.getOutputStream());
        response.getOutputStream().close();
        return null;
    }

    @RequestMapping(value = "/downloads")
    public String getCurrentDownloadFiles(Model model,
                                          @ModelAttribute("formBean") UnloadBean formBean) throws Exception {
        String date = downloadFileService.getMatchingIndexDirectory();
        formBean.setDate(date);
        formBean.setCurrentDate((true));
        String[] dateArray = date.split("\\.");
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, date);
        return getArchivedDownloads(model, dateArray[0], dateArray[1], dateArray[2], formBean);
    }

}
