package org.zfin.publication.presentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        boolean showEmptyForm = formBean.getQueryType() == null && formBean.getGroupType() == null;
        model.addAttribute("errors", errors);

        if (showEmptyForm) {
            return "publication/metrics";
        }

        if (formBean.getQueryType() == null) {
            errors.add("A query mode must be chosen");
        }
        if (formBean.getGroupType() == null) {
            errors.add("A Group By variable must be chosen");
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        if (formBean.getQueryType() != null) {
            if (formBean.getQueryType().isFromRequired()) {
                if (StringUtils.isEmpty(formBean.getFromDate())) {
                    errors.add("A From date is required");
                } else {
                    try {
                        start.setTime(inputFormat.parse(formBean.getFromDate()));
                    } catch (ParseException e) {
                        errors.add("Unable to parse From date: \"" + formBean.getFromDate() + "\"");
                    }
                }
            }
            if (formBean.getQueryType().isToRequired()) {
                if (StringUtils.isEmpty(formBean.getToDate())) {
                    errors.add("A To date is required");
                } else {
                    try {
                        end.setTime(inputFormat.parse(formBean.getToDate()));
                    } catch (ParseException e) {
                        errors.add("Unable to parse To date: \"" + formBean.getToDate() + "\"");
                    }
                }
            }
        }
        if (errors.size() > 0) {
            return "publication/metrics";
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

        if (formBean.getQueryType() == PublicationMetricsFormBean.QueryType.SNAPSHOT) {
            List<MetricsOnDateBean> resultList = publicationRepository.getSnapshotMetrics(formBean.getGroupType());

            for (Object rowLabel : rowLabels) {
                Map<String, Number> row = new LinkedHashMap<>();
                row.put(PublicationMetricsFormBean.Statistic.AVERAGE.getDisplay(), null);
                row.put(PublicationMetricsFormBean.Statistic.STANDARD_DEVIATION.getDisplay(), null);
                row.put(PublicationMetricsFormBean.Statistic.MINIMUM.getDisplay(), null);
                row.put(PublicationMetricsFormBean.Statistic.MAXIMUM.getDisplay(), null);
                row.put(PublicationMetricsFormBean.Statistic.OLDEST_AVERAGE.getDisplay(), null);
                resultTable.put(rowLabel.toString(), row);
            }

            for (MetricsOnDateBean result : resultList) {
                Object rowKey = result.getCategory();
                if (rowKey == null || !resultTable.containsKey(rowKey.toString())) {
                    continue;
                }
                Map<String, Number> row = resultTable.get(rowKey.toString());
                row.put(PublicationMetricsFormBean.Statistic.AVERAGE.getDisplay(), result.getAverage());
                row.put(PublicationMetricsFormBean.Statistic.STANDARD_DEVIATION.getDisplay(), result.getStandardDeviation());
                row.put(PublicationMetricsFormBean.Statistic.MINIMUM.getDisplay(), result.getMinimum());
                row.put(PublicationMetricsFormBean.Statistic.MAXIMUM.getDisplay(), result.getMaximum());
                row.put(PublicationMetricsFormBean.Statistic.OLDEST_AVERAGE.getDisplay(), result.getOldestAverage());
            }

        } else if (formBean.getQueryType() == PublicationMetricsFormBean.QueryType.CUMULATIVE) {
            List<MetricsOnDateBean> resultList = publicationRepository.getCumulativeMetrics(end, formBean.getGroupType());

            for (Object rowLabel : rowLabels) {
                Map<String, Number> row = new LinkedHashMap<>();
                row.put(PublicationMetricsFormBean.Statistic.AVERAGE.getDisplay(), null);
                row.put(PublicationMetricsFormBean.Statistic.STANDARD_DEVIATION.getDisplay(), null);
                row.put(PublicationMetricsFormBean.Statistic.MINIMUM.getDisplay(), null);
                row.put(PublicationMetricsFormBean.Statistic.MAXIMUM.getDisplay(), null);
                resultTable.put(rowLabel.toString(), row);
            }

            for (MetricsOnDateBean result : resultList) {
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
            SimpleDateFormat outputFormat = new SimpleDateFormat(formBean.getGroupBy().getFormat());
            List<MetricsByDateBean> resultList = publicationRepository.getMetricsByDate(start, end, formBean.getQueryType(), formBean.getGroupBy(), formBean.getGroupType());
            resultList.sort(Comparator.comparing(MetricsByDateBean::getDate));
            if (formBean.getQueryType() == PublicationMetricsFormBean.QueryType.PET_DATE) {
                Map<String, Number> totals = new LinkedHashMap<>();
                Calendar calendar = (Calendar) start.clone();
                while (calendar.before(end)) {
                    int total = 0;
                    for (MetricsByDateBean result : resultList) {
                        if (result.getDate().getTime() == calendar.getTimeInMillis()) {
                            total += result.getCount().intValue();
                        }
                    }
                    totals.put(outputFormat.format(calendar.getTime()), total);
                    calendar.add(formBean.getGroupBy().getField(), 1);
                }
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


        return "publication/metrics";
    }

}
