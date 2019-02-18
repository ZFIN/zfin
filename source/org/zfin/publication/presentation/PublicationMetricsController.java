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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication Metrics");

        Map<String, Map<String, Long>> resultTable = new LinkedHashMap<>();

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat(formBean.getGroupBy().getFormat());

        Calendar start = Calendar.getInstance();
        start.setTime(inputFormat.parse(formBean.getFromDate()));
        Calendar end = Calendar.getInstance();
        end.setTime(inputFormat.parse(formBean.getToDate()));

        Calendar calendar = (Calendar) start.clone();
        while (calendar.before(end)) {
            Map<String, Long> row = new LinkedHashMap<>();
            for (PublicationTrackingStatus.Name status : formBean.getStatuses()) {
                row.put(status.toString(), 0L);
            }
            resultTable.put(outputFormat.format(calendar.getTime()), row);
            calendar.add(formBean.getGroupBy().getField(), 1);
        }

        if (formBean.getQueryType() == PublicationMetricsFormBean.QueryType.PET_DATE) {
            List<PubMetricResultBean> resultList = publicationRepository.getMetricsByPETDate(start, end,
                    formBean.getGroupBy().toString(), formBean.getStatuses(), formBean.isCurrentStatusOnly());
            for (PubMetricResultBean result : resultList) {
                String rowKey = outputFormat.format(result.getDate());
                resultTable.get(rowKey).put(result.getStatus().toString(), result.getCount());
            }
        }

        model.addAttribute("resultsTable", resultTable);

        return "publication/metrics.page";
    }

}
