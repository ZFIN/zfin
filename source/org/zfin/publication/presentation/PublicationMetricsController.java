package org.zfin.publication.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.publication.PublicationTrackingLocation;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.publication.repository.PublicationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/publication")
public class PublicationMetricsController {

    private final static Logger LOG = Logger.getLogger(PublicationMetricsController.class);

    @Autowired
    private PublicationRepository publicationRepository;

    @RequestMapping(value = "/metrics", method = RequestMethod.GET)
    public String showSearchForm(@ModelAttribute("formBean") PublicationMetricsFormBean formBean,
                                 Model model) {

        model.addAttribute("statuses", publicationRepository.getAllPublicationStatuses().stream()
                .map(PublicationTrackingStatus::getName)
                .collect(Collectors.toList()));
        model.addAttribute("queryTypes", PublicationMetricsFormBean.QueryType.values());
        model.addAttribute("intervals", PublicationMetricsFormBean.Interval.values());
        model.addAttribute("statistics", PublicationMetricsFormBean.Statistic.values());
        List<PublicationTrackingLocation> locations = publicationRepository.getAllPublicationLocations();
        model.addAttribute("indexingLocations", locations.stream()
                .filter(l -> l.getRole() == PublicationTrackingLocation.Role.INDEXER)
                .map(PublicationTrackingLocation::getName)
                .collect(Collectors.toList()));
        model.addAttribute("curatingLocations", locations.stream()
                .filter(l -> l.getRole() == PublicationTrackingLocation.Role.CURATOR)
                .map(PublicationTrackingLocation::getName)
                .collect(Collectors.toList()));
        model.addAttribute("results", publicationRepository.getMetricsByPETDate());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication Metrics");

        LOG.warn(formBean.getQueryType());
        LOG.warn(formBean.getFromDate());
        LOG.warn(formBean.getToDate());
        LOG.warn(formBean.getGroupBy());
        LOG.warn(formBean.getStatistics());
        LOG.warn(formBean.getStatuses());
        LOG.warn(formBean.getLocations());
        LOG.warn(formBean.isCurrentStatusOnly());

        return "publication/metrics.page";
    }

}
