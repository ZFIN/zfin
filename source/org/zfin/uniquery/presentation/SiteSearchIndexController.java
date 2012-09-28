package org.zfin.uniquery.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.uniquery.SiteSearchIndexService;
import org.zfin.util.database.UnloadService;
import org.zfin.util.database.presentation.UnloadBean;

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

    @Autowired
    private UnloadBean unloadBean;

    @ModelAttribute("formBean")
    private UnloadBean getDefaultBean() {
        UnloadBean unloadBean = new UnloadBean();
        unloadBean.setUnloadService(unloadService);
        return unloadBean;
    }

    @RequestMapping(value = "/index-summary")
    public String getArchiveSummary(Model model) throws Exception {
        LOG.info("Start Detail Controller");

        model.addAttribute(siteSearchIndexService);
        model.addAttribute(LookupStrings.FORM_BEAN, unloadBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Site Search Summary");
        return "unload/site-search-index-summary.page";
    }

    @RequestMapping(value = "/update-site-search-cache")
    public String updateCache(Model model) throws Exception {
        siteSearchIndexService.updateCache();
        return getArchiveSummary(model);
    }


}
