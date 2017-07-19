package org.zfin.publication.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.publication.Publication;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/publication")
public class PublicationSearchController {

    private final static Logger LOG = Logger.getLogger(PublicationSearchController.class);
    private final static int PAGE_SIZE = 10;

    @Autowired
    private PublicationSearchService publicationSearchService;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String showSearchForm(Model model,
                                 @ModelAttribute PublicationSearchBean formBean,
                                 HttpServletRequest request) {
        formBean.setMaxDisplayRecords(PAGE_SIZE);
        formBean.setRequestUrl(request.getRequestURL());
        formBean.setQueryString(request.getQueryString());
        if (!formBean.isEmpty()) {
            publicationSearchService.populateSearchResults(formBean);
        }
        model.addAttribute("formBean", formBean);
        model.addAttribute("yearTypes", PublicationSearchBean.YearType.values());
        model.addAttribute("centuries", PublicationSearchBean.Century.values());
        model.addAttribute("pubTypes", Publication.Type.values());
        model.addAttribute("sortOrders", PublicationSearchBean.Sort.values());
        return "publication/publication-search.page";
    }

}
