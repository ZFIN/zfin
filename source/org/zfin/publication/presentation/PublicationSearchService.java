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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublicationSearchService {

    private final static Logger LOG = Logger.getLogger(PublicationSearchService.class);

    @Autowired
    private PublicationRepository publicationRepository;

    public void populateSearchResults(PublicationSearchBean formBean) {
        SolrQuery query = new SolrQuery();
        query.addFilterQuery(fq(FieldName.CATEGORY, Category.PUBLICATION.getName()));
        query.setFields(FieldName.ID.getName());
        addFq(query, FieldName.AUTHOR_STRING, formBean.getAuthor());
        addFq(query, FieldName.NAME, formBean.getTitle());
        addFq(query, FieldName.JOURNAL, formBean.getJournal());
        addFq(query, FieldName.KEYWORD, formBean.getKeywords());
        addFq(query, FieldName.ID_T, formBean.getZdbID());
        if (formBean.getPubType() != null) {
            addFq(query, FieldName.PUBLICATION_TYPE, formBean.getPubType().getDisplay());
        }
        if (StringUtils.isNotEmpty(formBean.getTwoDigitYear()) && formBean.getTwoDigitYear().matches("[0-9]+")) {
            int fullYear = Integer.parseInt(formBean.getCentury().getDisplay() + formBean.getTwoDigitYear());
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
        switch (formBean.getSort()) {
            case YEAR:
                query.addSort(FieldName.YEAR.getName(), SolrQuery.ORDER.desc);
                query.addSort(FieldName.NAME_SORT.getName(), SolrQuery.ORDER.asc);
                break;
            case AUTHOR:
                query.addSort(FieldName.NAME_SORT.getName(), SolrQuery.ORDER.asc);
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
        List<Publication> results = response.getResults().stream()
                .map(doc -> publicationRepository.getPublication(doc.getFieldValue(FieldName.ID.getName()).toString()))
                .collect(Collectors.toList());
        formBean.setResults(results);
        formBean.setTotalRecords((int) response.getResults().getNumFound());
    }

    private static String fq(FieldName fieldName, String value) {
        return fieldName.getName() + ":(\"" + SolrService.luceneEscape(value) + "\")";
    }

    private static void addFq(SolrQuery query, FieldName fieldName, String value) {
        if (StringUtils.isNotEmpty(value)) {
            query.addFilterQuery(fq(fieldName, value));
        }
    }

}