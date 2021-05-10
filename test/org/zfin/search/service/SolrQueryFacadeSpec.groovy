package org.zfin.search.service

import org.apache.solr.client.solrj.SolrQuery
import org.zfin.AbstractZfinSpec
import org.zfin.search.Category
import org.zfin.search.FieldName

import static org.zfin.search.service.SolrQueryFacade.addTo

class SolrQueryFacadeSpec extends AbstractZfinSpec {

    def 'single field fq'() {
        when:
        SolrQuery query = new SolrQuery()
        addTo(query).fq("foo", FieldName.NAME)

        then:
        query.getFilterQueries() == ['name:"foo"'] as String[]
    }

    def 'multiple field fq'() {
        when:
        SolrQuery query = new SolrQuery()
        addTo(query).fq("foo", FieldName.NAME, FieldName.FULL_NAME)

        then:
        query.getFilterQueries() == ['name:"foo" OR full_name:"foo"'] as String[]
    }

    def 'add category'() {
        when:
        SolrQuery query = new SolrQuery()
        addTo(query).category(Category.ANTIBODY)

        then:
        query.getFilterQueries() == ['category:"Antibody"'] as String[]
    }

    def 'any value fq'() {
        when:
        SolrQuery query = new SolrQuery()
        addTo(query).fqAny(FieldName.YEAR)

        then:
        query.getFilterQueries() == ['year:[* TO *]'] as String[]
    }

    def 'not any value fq'() {
        when:
        SolrQuery query = new SolrQuery()
        addTo(query).fqNotAny(FieldName.YEAR)

        then:
        query.getFilterQueries() == ['-year:[* TO *]'] as String[]
    }

    def 'greater than fq'() {
        when:
        SolrQuery query = new SolrQuery()
        addTo(query).fqGreaterThanOrEqual('2003', FieldName.YEAR)

        then:
        query.getFilterQueries() == ['year:[2003 TO *]'] as String[]
    }

    def 'less than fq'() {
        when:
        SolrQuery query = new SolrQuery()
        addTo(query).fqLessThanOrEqual('2016', FieldName.YEAR)

        then:
        query.getFilterQueries() == ['year:[* TO 2016]'] as String[]
    }

    def 'range fq'() {
        when:
        SolrQuery query = new SolrQuery()
        addTo(query).fqRange('2003', '2016', FieldName.YEAR)

        then:
        query.getFilterQueries() == ['year:[2003 TO 2016]'] as String[]
    }

    def 'parsed fq'() {
        when:
        SolrQuery query = new SolrQuery()
        addTo(query).fqParsed('Abraham Lincoln', FieldName.NAME, FieldName.FULL_NAME)

        then:
        query.getFilterQueries() == ['{!edismax qf=\'name full_name\'}Abraham Lincoln'] as String[]
    }

    def 'chained calls fq()'() {
        when:
        SolrQuery query = new SolrQuery()
        addTo(query)
                .category(Category.PUBLICATION)
                .fqParsed('zebrafish development', FieldName.NAME)
                .fq('active', FieldName.PUBLICATION_STATUS)
                .fq('cancer', FieldName.KEYWORD)

        then:
        query.getFilterQueries() == [
                'category:"Publication"',
                '{!edismax qf=\'name\'}zebrafish development',
                'publication_status:"active"',
                'keyword:"cancer"'
        ] as String[]
    }

}
