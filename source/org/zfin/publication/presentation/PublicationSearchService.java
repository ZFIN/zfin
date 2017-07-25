package org.zfin.publication.presentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.search.Category;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PublicationSearchService {

    private final static Logger LOG = Logger.getLogger(PublicationSearchService.class);

    @Autowired
    private PublicationRepository publicationRepository;

    public void populateSearchResults(PublicationSearchBean formBean) {
        QueryResponse response = makeSolrQuery(formBean, FieldName.ID);
        List<Publication> results = response.getResults().stream()
                .map(doc -> publicationRepository.getPublication(doc.getFieldValue(FieldName.ID.getName()).toString()))
                .collect(Collectors.toList());
        formBean.setResults(results);
        formBean.setTotalRecords((int) response.getResults().getNumFound());
    }

    public List<PublicationSearchResultBean> getResultsAsResultBeans(PublicationSearchBean formBean) {
        QueryResponse response = makeSolrQuery(formBean, FieldName.ID, FieldName.AUTHOR_STRING, FieldName.YEAR,
                FieldName.NAME, FieldName.JOURNAL, FieldName.PAGES, FieldName.VOLUME, FieldName.PUBLICATION_STATUS);
        return response.getBeans(PublicationSearchResultBean.class);
    }

    public String formatAsRefer(PublicationSearchResultBean searchResult) {
        return  referField("A", searchResult.getAuthors()) +
                referField("D", searchResult.getYear()) +
                referField("T", searchResult.getTitle()) +
                referField("J", searchResult.getJournal()) +
                referField("V", searchResult.getVolume()) +
                referField("P", searchResult.getPages());
    }

    private QueryResponse makeSolrQuery(PublicationSearchBean formBean, FieldName... fields) {
        SolrQuery query = new SolrQuery();
        query.addFilterQuery(fq(FieldName.CATEGORY, Category.PUBLICATION.getName()));
        query.setFields(Arrays.stream(fields).map(FieldName::getName).toArray(String[]::new));
        addFq(query, FieldName.AUTHOR_STRING, formBean.getAuthor());
        addFq(query, FieldName.NAME, formBean.getTitle());
        addFq(query, FieldName.JOURNAL, formBean.getJournal());
        addFq(query, FieldName.KEYWORD_T, formBean.getKeywords());
        addFq(query, FieldName.ID_T, formBean.getZdbID());
        addFq(query, FieldName.PUBLICATION_STATUS, formBean.getPubStatus());
        addFq(query, FieldName.PUB_SIMPLE_STATUS, formBean.getCurationStatus());
        addFq(query, FieldName.CURATOR, formBean.getCurator());
        if (formBean.getPubType() != null) {
            addFq(query, FieldName.PUBLICATION_TYPE, formBean.getPubType().getDisplay());
        }
        if (StringUtils.isNotEmpty(formBean.getTwoDigitYear()) && formBean.getTwoDigitYear().matches("[0-9]+")) {
            int fullYear = Integer.parseInt(formBean.getCentury() + formBean.getTwoDigitYear());
            switch (formBean.getYearType()) {
                case EQUALS:
                    query.addFilterQuery("year:" + fullYear);
                    break;
                case BEFORE:
                    query.addFilterQuery("year:[* TO " + (fullYear - 1) + "]");
                    break;
                case AFTER:
                    query.addFilterQuery("year:[" + (fullYear + 1) + " TO *]");
                    break;
            }
        }
        if (formBean.getPetFromYear() != null && formBean.getPetFromMonth() != null && formBean.getPetFromDay() != null &&
                formBean.getPetToYear() != null && formBean.getPetToMonth() != null && formBean.getPetToDay() != null) {
            DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-M-d");
            ZonedDateTime petFrom = parser
                    .parse(formBean.getPetFromYear() + "-" + formBean.getPetFromMonth() + "-" + formBean.getPetFromDay(), LocalDate::from)
                    .atStartOfDay(ZoneOffset.UTC);
            ZonedDateTime petTo = parser
                    .parse(formBean.getPetToYear() + "-" + formBean.getPetToMonth() + "-" + formBean.getPetToDay(), LocalDate::from)
                    .atStartOfDay(ZoneOffset.UTC);
            if (petFrom.isAfter(petTo)) {
                query.addFilterQuery("-pet_date:[* TO *]");
            } else {
                query.addFilterQuery("pet_date:[" + DateTimeFormatter.ISO_INSTANT.format(petFrom) +
                        " TO " + DateTimeFormatter.ISO_INSTANT.format(petTo) + "]");
            }
        }
        switch (formBean.getSort()) {
            case YEAR:
                query.addSort(FieldName.YEAR.getName(), SolrQuery.ORDER.desc);
                query.addSort(FieldName.AUTHOR_SORT.getName(), SolrQuery.ORDER.asc);
                break;
            case AUTHOR:
                query.addSort(FieldName.AUTHOR_SORT.getName(), SolrQuery.ORDER.asc);
                query.addSort(FieldName.YEAR.getName(), SolrQuery.ORDER.desc);
                break;
        }
        query.setRows(formBean.getMaxDisplayRecordsInteger());
        query.setStart((formBean.getPageInteger() - 1) * formBean.getMaxDisplayRecordsInteger());

        QueryResponse response = new QueryResponse();
        try {
            response = SolrService.getSolrClient().query(query);
        } catch (Exception e) {
            LOG.error(e);
        }
        return response;
    }

    private String fq(FieldName fieldName, String value) {
        return fieldName.getName() + ":(\"" + SolrService.luceneEscape(value) + "\")";
    }

    private void addFq(SolrQuery query, FieldName fieldName, String value) {
        if (StringUtils.isNotEmpty(value)) {
            query.addFilterQuery(fq(fieldName, value));
        }
    }

    private String referField(String tag, String value) {
        return "%" + tag + " " + Optional.ofNullable(value).orElse("") + "\n";
    }

}