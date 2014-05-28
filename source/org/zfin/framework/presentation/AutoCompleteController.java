package org.zfin.framework.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

@Controller
public class AutoCompleteController {

    private static Logger LOG = Logger.getLogger(AutoCompleteController.class);
    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();

    @RequestMapping("/gene-family")
    protected String geneFamilyHandler(@RequestParam("query") String query,
                                       @ModelAttribute("formBean") AutoCompleteBean autoCompleteBean) throws Exception {

        autoCompleteBean.setMarkerFamilyNames(mr.getMarkerFamilyNamesBySubstring(query));

        LOG.info("gene family, query: '"
                + query
                + "' size: "
                + autoCompleteBean.getMarkerFamilyNames().size());

        return "gene-family-autocomplete.page";
    }


}
