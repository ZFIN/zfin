package org.zfin.publication.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingLocation;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.publication.repository.PublicationRepository;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/publication")
public class PublicationMetricsController {

    private final static Logger LOG = Logger.getLogger(PublicationMetricsController.class);

    @Autowired
    private PublicationRepository publicationRepository;

    @RequestMapping(value = "/metrics", method = RequestMethod.GET)
    public String showSearchForm(@ModelAttribute("formBean") PublicationMetricsFormBean formBean,
                                 Model model) throws Exception {

        String[] indexedStatuses = new String[] { "Indexed", "Unindexed" };
        model.addAttribute("statuses", publicationRepository.getAllPublicationStatuses().stream()
                .map(PublicationTrackingStatus::getName)
                .collect(Collectors.toList()));
        model.addAttribute("queryTypes", PublicationMetricsFormBean.QueryType.values());
        model.addAttribute("intervals", PublicationMetricsFormBean.Interval.values());
        model.addAttribute("groupTypes", PublicationMetricsFormBean.GroupType.values());
        model.addAttribute("activationStatuses", Publication.Status.values());
        model.addAttribute("indexedStatuses", indexedStatuses);
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
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication Metrics");

        if (formBean.getQueryType() != null) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat(formBean.getGroupBy().getFormat());

            Calendar start = Calendar.getInstance();
            start.setTime(inputFormat.parse(formBean.getFromDate()));
            Calendar end = Calendar.getInstance();
            end.setTime(inputFormat.parse(formBean.getToDate()));

            Map<String, Map<String, Long>> resultTable = new LinkedHashMap<>();
            Object[] rowLabels;
            switch (formBean.getGroupType()) {
                case ACTIVE:
                    rowLabels = formBean.getActivationStatuses();
                    break;
                case INDEXED:
                    rowLabels = indexedStatuses;
                    break;
                case STATUS:
                    rowLabels = formBean.getStatuses();
                    break;
                default:
                    rowLabels = new Object[]{};
                    break;
            }
            for (Object rowLabel : rowLabels) {
                Map<String, Long> row = new LinkedHashMap<>();
                Calendar calendar = (Calendar) start.clone();
                while (calendar.before(end)) {
                    row.put(outputFormat.format(calendar.getTime()), 0L);
                    calendar.add(formBean.getGroupBy().getField(), 1);
                }
                resultTable.put(rowLabel.toString(), row);
            }

            if (formBean.getQueryType() == PublicationMetricsFormBean.QueryType.PET_DATE) {
                List<PubMetricResultBean> resultList;
                switch (formBean.getGroupType()) {
                    case ACTIVE:
                        resultList = publicationRepository.getActivationStatusMetricsByPETDate(start, end, formBean.getGroupBy().toString(), formBean.getActivationStatuses());
                        break;
                    case INDEXED:
                        resultList = publicationRepository.getIndexedStatusMetricsByPETDate(start, end, formBean.getGroupBy().toString(), formBean.getIndexedStatuses());
                        break;
                    case STATUS:
                        resultList = publicationRepository.getStatusMetricsByPETDate(start, end, formBean.getGroupBy().toString(), formBean.getStatuses(), formBean.isCurrentStatusOnly());
                        break;
                    default:
                        resultList = new ArrayList<>();
                        break;
                }
                for (PubMetricResultBean result : resultList) {
                    resultTable.get(result.getCategory().toString()).put(outputFormat.format(result.getDate()), result.getCount());
                }
            }
            model.addAttribute("resultsTable", resultTable);
        }


        return "publication/metrics.page";
    }

}
