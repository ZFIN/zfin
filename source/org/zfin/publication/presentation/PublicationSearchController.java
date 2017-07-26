package org.zfin.publication.presentation;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Controller
@RequestMapping("/publication")
public class PublicationSearchController {

    private final static Logger LOG = Logger.getLogger(PublicationSearchController.class);
    private final static int PAGE_SIZE = 10;

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
        formBean.setMaxDisplayRecords(PAGE_SIZE);
        formBean.setRequestUrl(request.getRequestURL());
        formBean.setQueryString(request.getQueryString());
        GregorianCalendar oldestPubEntryDate = publicationRepository.getOldestPubEntryDate();
        GregorianCalendar newestPubEntryDate = publicationRepository.getNewestPubEntryDate();
        setDefaultValue(formBean, "petFromMonth", oldestPubEntryDate.get(Calendar.MONTH) + 1);
        setDefaultValue(formBean, "petFromDay", oldestPubEntryDate.get(Calendar.DAY_OF_MONTH));
        setDefaultValue(formBean, "petFromYear", oldestPubEntryDate.get(Calendar.YEAR));
        setDefaultValue(formBean, "petToMonth", newestPubEntryDate.get(Calendar.MONTH) + 1);
        setDefaultValue(formBean, "petToDay", newestPubEntryDate.get(Calendar.DAY_OF_MONTH));
        setDefaultValue(formBean, "petToYear", newestPubEntryDate.get(Calendar.YEAR));
        if (!request.getParameterMap().isEmpty()) {
            publicationSearchService.populateSearchResults(formBean);
        }
        model.addAttribute("formBean", formBean);
        model.addAttribute("yearTypes", PublicationSearchBean.YearType.values());
        model.addAttribute("pubTypes", Publication.Type.values());
        model.addAttribute("sortOrders", PublicationSearchBean.Sort.values());
        model.addAttribute("curators", profileRepository.getCurators());
        model.addAttribute("oldestPubEntryDate", oldestPubEntryDate.getTime());
        model.addAttribute("newestPubEntryDate", newestPubEntryDate.getTime());
        return "publication/publication-search.page";
    }

    @RequestMapping(value = "/search/printable", method = RequestMethod.GET)
    public String returnPrintableResults(Model model,
                                         @ModelAttribute PublicationSearchBean formBean) {
        formBean.setMaxDisplayRecords(Integer.MAX_VALUE);
        formBean.setPageInteger(1);
        model.addAttribute("formBean", formBean);
        model.addAttribute("resultBeans", publicationSearchService.getResultsAsResultBeans(formBean));
        model.addAttribute("today", new Date());
        // this isn't really called via an ajax request, but this is how you get an unstyled page so...
        return "publication/printable-results.ajax";
    }

    @RequestMapping(value = "/search/refer", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public void returnReferResults(@ModelAttribute PublicationSearchBean formBean,
                                   HttpServletResponse response) throws IOException {
        formBean.setMaxDisplayRecords(Integer.MAX_VALUE);
        formBean.setPageInteger(1);
        List<PublicationSearchResultBean> results = publicationSearchService.getResultsAsResultBeans(formBean);
        String fileName = "ZFIN-Pub-Search-" + DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        for (PublicationSearchResultBean result : results) {
            writer.println(publicationSearchService.formatAsRefer(result));
        }
        writer.close();
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
