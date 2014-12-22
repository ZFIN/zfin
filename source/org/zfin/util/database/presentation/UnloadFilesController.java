package org.zfin.util.database.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.database.DatabaseIndexer;
import org.zfin.util.database.Grep;
import org.zfin.util.database.UnloadIndexingService;
import org.zfin.util.database.UnloadService;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Controller class that serves the unload history page.
 */
@Controller
@RequestMapping("/unload")
public class UnloadFilesController {

    private static final Logger LOG = Logger.getLogger(UnloadFilesController.class);

    @Autowired
    private UnloadService unloadService;

    @Autowired
    private UnloadIndexingService unloadIndexingService;

    @Autowired
    private DatabaseIndexer databaseIndexer;

    @ModelAttribute("formBean")
    private UnloadBean getDefaultBean() {
        UnloadBean unloadBean = new UnloadBean();
        unloadBean.setUnloadService(unloadService);
        unloadBean.setUnloadIndexingService(unloadIndexingService);
        return unloadBean;
    }

    @RequestMapping(value = "/summary")
    public String getUnloadIndexSummary(Model model,
                                        UnloadBean formBean) throws Exception {
        LOG.info("Start Detail Controller");

        formBean = getDefaultBean();
        formBean.setDataTableMap(unloadService.getAllTablesIndexed());
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);

        return "unload/unload-summary.page";
    }

    @RequestMapping(value = "/table-summary/{tableName}")
    public String getTableSummaryView(Model model,
                                      @PathVariable("tableName") String tableName,
                                      UnloadBean formBean) throws Exception {
        formBean.setUnloadService(unloadService);
        formBean.setTableName(tableName);
        model.addAttribute("tableName", tableName.toUpperCase());
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        return "unload/table-summary.page";
    }

    @RequestMapping(value = "/entity-history")
    public String getEntityHistory(Model model,
                                   UnloadBean formBean) throws Exception {
        formBean.setUnloadService(unloadService);
        String tableName = formBean.getTableName();
        model.addAttribute("tableName", tableName.toUpperCase());
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        return "unload/entity-history.page";
    }

    @RequestMapping(value = "/use-production-index/{production}")
    public String changeIndexDirectory(Model model,
                                       @PathVariable("production") String productionStr,
                                       @ModelAttribute("formBean") UnloadBean formBean) throws Exception {
        formBean.setUnloadService(unloadService);
        boolean production = productionStr != null && productionStr.equalsIgnoreCase("true");
        if (production)
            formBean.getUnloadService().setIndexDirectory("/Volumes/research/zcentral/www_homes/eselsohr/home/WEB-INF/unload-index");
        else
            formBean.getUnloadService().setIndexDirectory(ZfinPropertiesEnum.WEBROOT_DIRECTORY.value() + "/WEB-INF/unload-index");
        return getUnloadIndexSummary(model, formBean);
    }

    @RequestMapping(value = "/fetch-entity-record", method = RequestMethod.GET)
    public String fetchEntity(Model model,
                              @ModelAttribute("formBean") UnloadBean unloadBean) throws Exception {

        String recordLine = getRecord(unloadBean);
        model.addAttribute("recordLine", recordLine);
        return "unload/record-detail.ajax-page";
    }

    @RequestMapping(value = "/upgrade-table-index/{tableName}", method = RequestMethod.GET)
    public String upgradeTableIndex(Model model,
                                    @PathVariable("tableName") String tableName) throws Exception {

        databaseIndexer.runUpdate(tableName);
        return "redirect:/action/unload/summary";
    }

    @RequestMapping(value = "/index-new-table", method = RequestMethod.POST)
    public String indexTable(Model model,
                             @ModelAttribute("formBean") UnloadBean formBean) throws Exception {

        databaseIndexer.indexNewTableByDate(formBean.getTableName(), formBean.getDate());
        return "redirect:/action/unload/summary";
    }

    @RequestMapping(value = "/re-load-index")
    public String reLoadIndex() throws Exception {
        unloadService.reLoadIndex();
        return "unload/unload-summary.page";
    }

    @RequestMapping(value = "/date-history", method = RequestMethod.GET)
    public String differenceBetweenDateRanges(Model model,
                                              @ModelAttribute("formBean") UnloadBean unloadBean) throws Exception {

        List<UnloadService.EntityTrace> previousDate = unloadService.getDateHistory(unloadBean.getDate());

        model.addAttribute("dateHistory", previousDate);
        return "unload/date-history.page";
    }

    @RequestMapping(value = "/fetch-changed-entity-records", method = RequestMethod.GET)
    public String fetchChangeRecords(Model model,
                                     @ModelAttribute("formBean") UnloadBean unloadBean) throws Exception {

        String recordLine = getRecord(unloadBean);
        String previousDate = unloadService.getPreviousDate(unloadBean.getDate());
        String previousRecordLine = getPreviousRecord(unloadBean, previousDate);

        model.addAttribute("recordLine", recordLine);
        model.addAttribute("currentDate", unloadBean.getDate());
        model.addAttribute("previousRecordLine", previousRecordLine);
        model.addAttribute("previousDate", previousDate);
        return "unload/record-comparison-detail.ajax-page";
    }

    private String getRecord(UnloadBean unloadBean) {
        return getEntityFromUnloadFile(unloadBean.getEntityID(), unloadBean.getTableName(), unloadBean.getDate());
    }

    private String getPreviousRecord(UnloadBean unloadBean, String previousDate) {
        return getEntityFromUnloadFile(unloadBean.getEntityID(), unloadBean.getTableName(), previousDate);
    }

    synchronized private String getEntityFromUnloadFile(String entityID, String tableName, String date) {
        String unloadDirectory = ZfinPropertiesEnum.DATABASE_UNLOAD_DIRECTORY.value();
        tableName = tableName.toLowerCase();
        File file = new File(unloadDirectory, date);
        File unloadFile = new File(file, tableName);
        entityID += "";
        LOG.info("Check unload: " + file.getName());
        Grep grep = null;
        try {
            grep = new Grep(entityID, unloadFile);
            // fish out the correct matching line (cannot match on on <ID|>
            // e.g. ZDB-ANAT-011113-12 matches in Grep also ZDB-ANAT-011113-129
            for (String matchingLine : grep.getLinesMatched())
                if (matchingLine.contains(entityID + "|"))
                    return matchingLine;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
