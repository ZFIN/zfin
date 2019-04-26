package org.zfin.publication.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/publication")
public class PublicationMetricsController {

    private final static Logger LOG = LogManager.getLogger(PublicationMetricsController.class);

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
        model.addAttribute("groupTypes", PublicationMetricsFormBean.GroupType.values());
        model.addAttribute("activationStatuses", Publication.Status.values());
        model.addAttribute("indexedStatuses", PublicationMetricsFormBean.INDEXED_STATUSES);
        model.addAttribute("locations", publicationRepository.getAllPublicationLocations().stream()
                .filter(l -> l.getRole() == PublicationTrackingLocation.Role.CURATOR)
                .map(PublicationTrackingLocation::getName)
                .collect(Collectors.toList()));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication Metrics");

        List<String> errors = new ArrayList<>();
        if (formBean.getQueryType() != null && formBean.getGroupType() != null) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");

            Calendar end = Calendar.getInstance();
            try {
                end.setTime(inputFormat.parse(formBean.getToDate()));
            } catch (ParseException e) {
                errors.add("Unable to parse To date: \"" + formBean.getToDate() + "\"");
                return "publication/metrics.page";
            }

            Map<String, Map<String, Number>> resultTable = new LinkedHashMap<>();

            Object[] rowLabels = new Object[]{};
            switch (formBean.getGroupType()) {
                case ACTIVE:
                    rowLabels = formBean.getActivationStatuses();
                    break;
                case INDEXED:
                    rowLabels = formBean.getIndexedStatuses();
                    break;
                case STATUS:
                    rowLabels = formBean.getStatuses();
                    break;
                case LOCATION:
                    rowLabels = formBean.getLocations();
            }

            if (formBean.getQueryType() == PublicationMetricsFormBean.QueryType.CUMULATIVE) {
                List<CumulativeStatisticsBean> resultList = publicationRepository.getCumulativeMetrics(end, formBean.getGroupType());

                for (Object rowLabel : rowLabels) {
                    Map<String, Number> row = new LinkedHashMap<>();
                    row.put(PublicationMetricsFormBean.Statistic.AVERAGE.getDisplay(), null);
                    row.put(PublicationMetricsFormBean.Statistic.STANDARD_DEVIATION.getDisplay(), null);
                    row.put(PublicationMetricsFormBean.Statistic.MINIMUM.getDisplay(), null);
                    row.put(PublicationMetricsFormBean.Statistic.MAXIMUM.getDisplay(), null);
                    resultTable.put(rowLabel.toString(), row);
                }

                for (CumulativeStatisticsBean result : resultList) {
                    Object rowKey = result.getCategory();
                    if (rowKey == null || !resultTable.containsKey(rowKey.toString())) {
                        continue;
                    }
                    Map<String, Number> row = resultTable.get(rowKey.toString());
                    row.put(PublicationMetricsFormBean.Statistic.AVERAGE.getDisplay(), result.getAverage());
                    row.put(PublicationMetricsFormBean.Statistic.STANDARD_DEVIATION.getDisplay(), result.getStandardDeviation());
                    row.put(PublicationMetricsFormBean.Statistic.MINIMUM.getDisplay(), result.getMinimum());
                    row.put(PublicationMetricsFormBean.Statistic.MAXIMUM.getDisplay(), result.getMaximum());
                }
            } else {
                Calendar start = Calendar.getInstance();
                SimpleDateFormat outputFormat = new SimpleDateFormat(formBean.getGroupBy().getFormat());
                try {
                    start.setTime(inputFormat.parse(formBean.getFromDate()));
                } catch (ParseException e) {
                    errors.add("Unable to parse From date: \"" + formBean.getFromDate() + "\"");
                    return "publication/metrics.page";
                }

                List<MetricsByDateBean> resultList = publicationRepository.getMetricsByDate(start, end, formBean.getQueryType(), formBean.getGroupBy(), formBean.getGroupType());
                resultList.sort(Comparator.comparing(MetricsByDateBean::getDate));
                if (formBean.getQueryType() == PublicationMetricsFormBean.QueryType.PET_DATE) {
                    Map<String, Number> totals = resultList.stream().collect(Collectors.toMap(
                            result -> outputFormat.format(result.getDate()),
                            MetricsByDateBean::getCount,
                            (a, b) -> a.intValue() + b.intValue(),
                            LinkedHashMap::new
                    ));
                    resultTable.put("All", totals);
                }
                for (Object rowLabel : rowLabels) {
                    Map<String, Number> row = new LinkedHashMap<>();
                    Calendar calendar = (Calendar) start.clone();
                    while (calendar.before(end)) {
                        row.put(outputFormat.format(calendar.getTime()), 0);
                        calendar.add(formBean.getGroupBy().getField(), 1);
                    }
                    resultTable.put(rowLabel.toString(), row);
                }

                for (MetricsByDateBean result : resultList) {
                    Object rowKey = result.getCategory();
                    if (rowKey == null || !resultTable.containsKey(rowKey.toString())) {
                        continue;
                    }
                    resultTable.get(rowKey.toString()).put(outputFormat.format(result.getDate()), result.getCount());
                }
            }

            model.addAttribute("resultsTable", resultTable);
        }


        return "publication/metrics.page";
    }

}
