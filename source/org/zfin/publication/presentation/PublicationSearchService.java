package org.zfin.publication.presentation;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.gwt.root.util.StringUtils;
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
        if (StringUtils.isNotEmpty(formBean.getAuthor())) {
            query.addFilterQuery(fq(FieldName.AUTHOR_STRING, formBean.getAuthor()));
        }
        if (StringUtils.isNotEmpty(formBean.getTitle())) {
            query.addFilterQuery(fq(FieldName.NAME, formBean.getTitle()));
        }
        if (StringUtils.isNotEmpty(formBean.getJournal())) {
            query.addFilterQuery(fq(FieldName.JOURNAL, formBean.getJournal()));
        }
        query.addSort(FieldName.DATE.getName(), SolrQuery.ORDER.desc);
        query.addSort(FieldName.NAME_SORT.getName(), SolrQuery.ORDER.asc);
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

}