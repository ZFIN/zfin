package org.zfin.search.service

import org.apache.solr.client.solrj.SolrQuery
import org.zfin.AbstractZfinSpec

class SolrServiceSpec extends AbstractZfinSpec {

    def 'queryHasFilterQueries is false for query with no filters'() {
        when:
        SolrQuery query = new SolrQuery();

        then:
        !SolrService.queryHasFilterQueries(query)
    }

    def 'queryHasFilterQueries is false for query with root only filter'() {
        when:
        SolrQuery query = new SolrQuery();
        query.addFilterQuery("root_only:false");

        then:
        !SolrService.queryHasFilterQueries(query)
    }

    def 'queryHasFilterQueries is true for query with category'() {
        when:
        SolrQuery query = new SolrQuery();
        query.addFilterQuery("category:\"Expression\"");

        then:
        SolrService.queryHasFilterQueries(query)
    }
}
