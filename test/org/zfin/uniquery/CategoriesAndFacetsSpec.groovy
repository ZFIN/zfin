package org.zfin.uniquery

import org.apache.log4j.Logger
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.response.FacetField
import org.apache.solr.client.solrj.response.QueryResponse
import org.springframework.beans.factory.annotation.Autowired
import org.zfin.ZfinIntegrationSpec
import org.zfin.search.FieldName
import org.zfin.search.service.SolrService
import org.zfin.search.Category
import spock.lang.Shared
import spock.lang.Unroll


class CategoriesAndFacetsSpec extends ZfinIntegrationSpec {

    public static Logger logger = Logger.getLogger(CategoriesAndFacetsSpec.class)

    @Autowired
    SolrService solrService

    @Shared
    SolrClient client
    @Shared
    SolrQuery query

    //sets up for all tests in class
    public def setupSpec() {
        client = SolrService.getSolrClient("prototype")
    }

    public def cleanSpec() {
        client = null
    }

    //sets up for each test
    def setup() {
        query = new SolrQuery();
    }

    def clean() {
        query = null
    }

    @Unroll
    def "#category category doesn't exist"() {

        when: "do a no result query asking for category facets"
        //SolrService.setCategory(category,facetBuilderService.getFacetMap(), query)
        query.addFacetField("category")
        query.rows = 0
        QueryResponse response = new QueryResponse();

        try {
            response = client.query(query);
        } catch (Exception e) {
            logger.error(e);
        }
        FacetField categoryFacet = response.facetFields.get(0)
        def facetValues = categoryFacet.values*.name

        then: "category facet should contain each category"
        facetValues.contains(category.name)

        where:
        category << Category.values()
    }

    @Unroll
    def "#category should have #field"() {
        when:
        SolrService.setCategory(category.name, query)
        query.rows = 0
        QueryResponse response = new QueryResponse();

        try {
            response = client.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        def returnedFacets = response.facetFields*.name

        then:
        returnedFacets.contains(field.name)

        where:
        [category, field] << [[Category.GENE], [FieldName.ANATOMY_TF, FieldName.STAGE, FieldName.AFFECTED_ANATOMY_TF, FieldName.AFFECTED_BIOLOGICAL_PROCESS_TF,
                                                FieldName.AFFECTED_MOLECULAR_FUNCTION_TF, FieldName.AFFECTED_CELLULAR_COMPONENT_TF,
                                                FieldName.PHENOTYPE_STATEMENT, FieldName.MISEXPRESSED_GENE, FieldName.DISEASE, FieldName.BIOLOGICAL_PROCESS_TF,
                                                FieldName.MOLECULAR_FUNCTION_TF, FieldName.CELLULAR_COMPONENT_TF, FieldName.CHROMOSOME, FieldName.TYPE]].combinations() +
                [[Category.FISH], [FieldName.AFFECTED_GENE, FieldName.AFFECTED_ANATOMY_TF, FieldName.AFFECTED_BIOLOGICAL_PROCESS_TF,
                                   FieldName.AFFECTED_MOLECULAR_FUNCTION_TF, FieldName.AFFECTED_CELLULAR_COMPONENT_TF,
                                   FieldName.PHENOTYPE_STATEMENT, FieldName.MISEXPRESSED_GENE, FieldName.EXPRESSION_ANATOMY_TF, FieldName.SEQUENCE_TARGETING_REAGENT,
                                   FieldName.CONSTRUCT, FieldName.SEQUENCE_ALTERATION, FieldName.BACKGROUND]].combinations() +
                [[Category.MUTANT], [FieldName.TYPE, FieldName.AFFECTED_GENE, FieldName.AFFECTED_ANATOMY_TF, FieldName.AFFECTED_BIOLOGICAL_PROCESS_TF,
                                     FieldName.AFFECTED_MOLECULAR_FUNCTION_TF, FieldName.AFFECTED_CELLULAR_COMPONENT_TF, FieldName.PHENOTYPE_STATEMENT, FieldName.MISEXPRESSED_GENE,
                                     FieldName.SOURCE, FieldName.LAB_OF_ORIGIN, FieldName.CONSEQUENCE, FieldName.INSTITUTION]].combinations() +
                [[Category.CONSTRUCT], [FieldName.TYPE, FieldName.REGULATORY_REGION, FieldName.CODING_SEQUENCE, FieldName.INSERTED_IN_GENE, FieldName.EXPRESSED_IN_TF,
                                        FieldName.REPORTER_COLOR, FieldName.ENGINEERED_REGION]].combinations() +
                [[Category.SEQUENCE_TARGETING_REAGENT], [FieldName.TYPE, FieldName.TARGETED_GENE]].combinations() +
                [[Category.MARKER], [FieldName.TYPE, FieldName.CHROMOSOME, FieldName.SOURCE]].combinations() +
                [[Category.FIGURE], [FieldName.EXPRESSION_ANATOMY_TF, FieldName.REPORTER_GENE, FieldName.ZEBRAFISH_GENE, FieldName.AFFECTED_ANATOMY_TF,
                                     FieldName.AFFECTED_BIOLOGICAL_PROCESS_TF, FieldName.AFFECTED_MOLECULAR_FUNCTION_TF,
                                     FieldName.AFFECTED_CELLULAR_COMPONENT_TF, FieldName.PHENOTYPE_STATEMENT, FieldName.MISEXPRESSED_GENE,
                                     FieldName.CONSTRUCT, FieldName.AUTHOR]].combinations() +
                [[Category.EXPRESSIONS], [FieldName.REPORTER_GENE, FieldName.ZEBRAFISH_GENE, FieldName.EXPRESSION_ANATOMY_TF, FieldName.ASSAY, FieldName.GENOTYPE_FULL_NAME,
                                          FieldName.HAS_IMAGE, FieldName.EXPERIMENTAL_CONDITIONS, FieldName.AUTHOR, FieldName.SEQUENCE_TARGETING_REAGENT]].combinations() +
                [[Category.PHENOTYPE], [FieldName.PHENOTYPE_STATEMENT, FieldName.MISEXPRESSED_GENE, FieldName.ANATOMY_TF, FieldName.BIOLOGICAL_PROCESS_TF, FieldName.MOLECULAR_FUNCTION_TF,
                                        FieldName.HAS_IMAGE, FieldName.STAGE, FieldName.SEQUENCE_TARGETING_REAGENT]].combinations() +
                [[Category.ANATOMY], [FieldName.ONTOLOGY, FieldName.TERM_STATUS]].combinations() +
                [[Category.COMMUNITY], [FieldName.TYPE]].combinations() +
                [[Category.PUBLICATION], [FieldName.GENE, FieldName.SEQUENCE_ALTERATION, FieldName.AUTHOR, FieldName.JOURNAL, FieldName.KEYWORD,
                                          FieldName.MESH_TERM, FieldName.PUBLICATION_TYPE]].combinations() +
                [[Category.ANTIBODY], [FieldName.TYPE, FieldName.ANTIGEN_GENE, FieldName.LABELED_STRUCTURE_TF, FieldName.ASSAY, FieldName.SOURCE, FieldName.HOST_ORGANISM]].combinations() +
                [[Category.DISEASE], [FieldName.GENE, FieldName.FISH, FieldName.EXPERIMENTAL_CONDITIONS]].combinations() +
                [[Category.REPORTER_LINE], [FieldName.EXPRESSION_ANATOMY_TF, FieldName.REGULATORY_REGION]].combinations()
        }


}
