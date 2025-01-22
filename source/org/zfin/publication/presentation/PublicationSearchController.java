package org.zfin.publication.presentation;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.PublicationType;
import org.zfin.publication.repository.PublicationRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/publication")
public class PublicationSearchController {

    private final static Logger LOG = LogManager.getLogger(PublicationSearchController.class);
    private final static int MIN_PAGE_SIZE = 1;
    private final static int DEFAULT_PAGE_SIZE = 10;
    private final static int MAX_PAGE_SIZE = 1000;
    private final static String PUB_FACETED_SEARCH_URL = "/search?q=&fq=category%3A%22Publication%22&category=Publication";

    @Autowired
    private PublicationSearchService publicationSearchService;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String showSearchForm(Model model,
                                 @ModelAttribute PublicationSearchBean formBean,
                                 HttpServletRequest request) {
        GregorianCalendar oldestPubEntryDate = publicationRepository.getOldestPubEntryDate();
        GregorianCalendar newestPubEntryDate = publicationRepository.getNewestPubEntryDate();
        setDefaultValue(formBean, "petFromMonth", oldestPubEntryDate.get(Calendar.MONTH) + 1);
        setDefaultValue(formBean, "petFromDay", oldestPubEntryDate.get(Calendar.DAY_OF_MONTH));
        setDefaultValue(formBean, "petFromYear", oldestPubEntryDate.get(Calendar.YEAR));
        setDefaultValue(formBean, "petToMonth", newestPubEntryDate.get(Calendar.MONTH) + 1);
        setDefaultValue(formBean, "petToDay", newestPubEntryDate.get(Calendar.DAY_OF_MONTH));
        setDefaultValue(formBean, "petToYear", newestPubEntryDate.get(Calendar.YEAR));
        int count;
        try {
            count = Integer.parseInt(formBean.getCount());
            if (count < MIN_PAGE_SIZE) {
                count = MIN_PAGE_SIZE;
            }
            if (count > MAX_PAGE_SIZE) {
                count = MAX_PAGE_SIZE;
            }
        } catch (NumberFormatException e) {
            count = DEFAULT_PAGE_SIZE;
        }
        formBean.setCount(Integer.toString(count));
        formBean.setMaxDisplayRecords(count);
        formBean.setRequestUrl(request.getRequestURL());
        formBean.setQueryString(request.getQueryString());
        if (!request.getParameterMap().isEmpty()) {
            publicationSearchService.populateSearchResults(formBean);
        }
        model.addAttribute("formBean", formBean);
        model.addAttribute("yearTypes", PublicationSearchBean.YearType.values());
        PublicationType[] pubTypes = PublicationType.values();
        Arrays.sort(pubTypes, Comparator.comparingInt(PublicationType::getDisplayOrder));
        model.addAttribute("pubTypes", pubTypes);
        model.addAttribute("sortOrders", PublicationSearchBean.Sort.values());
        model.addAttribute("curators", profileRepository.getCurators());
        model.addAttribute("oldestPubEntryDate", oldestPubEntryDate.getTime());
        model.addAttribute("newestPubEntryDate", newestPubEntryDate.getTime());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication Search");
        return "publication/publication-search";
    }

    @RequestMapping(value = "/search/printable", method = RequestMethod.GET)
    public String returnPrintableResults(Model model,
                                         @ModelAttribute PublicationSearchBean formBean) {
        //redirect to PUB_FACETED_SEARCH_URL
        return "redirect:" + PUB_FACETED_SEARCH_URL;
    }

    @RequestMapping(value = "/search/refer", method = RequestMethod.GET)
    public String returnReferResults(@ModelAttribute PublicationSearchBean formBean,
                                   HttpServletResponse response) throws IOException {
        //redirect to PUB_FACETED_SEARCH_URL
        return "redirect:" + PUB_FACETED_SEARCH_URL;
    }

    private void setDefaultValue(PublicationSearchBean formBean, String property, Object value) {
        try {
            if (PropertyUtils.getProperty(formBean, property) == null) {
                PropertyUtils.setProperty(formBean, property, value);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOG.error("Unable to set property " + property + " on pub form search bean", e);
        }
    }
}
