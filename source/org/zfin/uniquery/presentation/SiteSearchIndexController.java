package org.zfin.uniquery.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.uniquery.SiteSearchIndexService;
import org.zfin.util.database.DatabaseIndexer;
import org.zfin.util.database.Grep;
import org.zfin.util.database.UnloadIndexingService;
import org.zfin.util.database.UnloadService;
import org.zfin.util.database.presentation.UnloadBean;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Controller class that serves the site search index page.
 */
@Controller
public class SiteSearchIndexController {

    private static final Logger LOG = Logger.getLogger(SiteSearchIndexController.class);

    @Autowired
    private UnloadService unloadService;


    @Qualifier(value = "siteSearchIndexService")
    @Autowired
    private SiteSearchIndexService siteSearchIndexService;

    @ModelAttribute("formBean")
    private UnloadBean getDefaultBean() {
        UnloadBean unloadBean = new UnloadBean();
        unloadBean.setUnloadService(unloadService);
        return unloadBean;
    }

    @RequestMapping(value = "/index-summary")
    public String getIndexSummary(Model model) throws Exception {
        LOG.info("Start Detail Controller");
        model.addAttribute(LookupStrings.FORM_BEAN, siteSearchIndexService);
        return "unload/site-search-index-summary.page";
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
