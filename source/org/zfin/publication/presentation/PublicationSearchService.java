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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.zfin.search.service.SolrQueryFacade.addTo;

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
        query.setFields(Arrays.stream(fields).map(FieldName::getName).toArray(String[]::new));
        addTo(query)
                .category(Category.PUBLICATION)
                .fqParsed(formBean.getAuthor(), FieldName.AUTHOR_STRING)
                .fqParsed(formBean.getKeywords(), FieldName.KEYWORD_AC)
                .fqParsed(formBean.getTitle(), FieldName.FULL_NAME_AC)
                .fq(formBean.getJournal(), FieldName.JOURNAL_T, FieldName.JOURNAL_NAME_T)
                .fq(formBean.getZdbID(), FieldName.ID_T)
                .fq(formBean.getPubStatus(), FieldName.PUBLICATION_STATUS)
                .fq(formBean.getCurationStatus(), FieldName.PUB_SIMPLE_STATUS)
                .fq(formBean.getCurator(), FieldName.CURATOR);
        if (formBean.getPubType() != null) {
            addTo(query).fq(formBean.getPubType().getDisplay(), FieldName.PUBLICATION_TYPE);
        }
        if (StringUtils.isNotEmpty(formBean.getTwoDigitYear()) && formBean.getTwoDigitYear().matches("[0-9]+")) {
            int fullYear = Integer.parseInt(formBean.getCentury() + formBean.getTwoDigitYear());
            switch (formBean.getYearType()) {
                case EQUALS:
                    addTo(query).fq(Integer.toString(fullYear), FieldName.YEAR);
                    break;
                case BEFORE:
                    addTo(query).fqLessThanOrEqual(Integer.toString(fullYear - 1), FieldName.YEAR);
                    break;
                case AFTER:
                    addTo(query).fqGreaterThanOrEqual(Integer.toString(fullYear + 1), FieldName.YEAR);
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
                addTo(query).fqNotAny(FieldName.PET_DATE);
            } else {
                addTo(query).fqRange(
                        DateTimeFormatter.ISO_INSTANT.format(petFrom),
                        DateTimeFormatter.ISO_INSTANT.format(petTo),
                        FieldName.PET_DATE);
            }
        }
        if (formBean.getSort() == null || formBean.getSort() == PublicationSearchBean.Sort.YEAR) {
            query.addSort(FieldName.YEAR.getName(), SolrQuery.ORDER.desc);
            query.addSort(FieldName.AUTHOR_SORT.getName(), SolrQuery.ORDER.asc);
        } else if (formBean.getSort() == PublicationSearchBean.Sort.AUTHOR) {
            query.addSort(FieldName.AUTHOR_SORT.getName(), SolrQuery.ORDER.asc);
            query.addSort(FieldName.YEAR.getName(), SolrQuery.ORDER.desc);
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

    private String referField(String tag, String value) {
        return "%" + tag + " " + Optional.ofNullable(value).orElse("") + "\n";
    }

}