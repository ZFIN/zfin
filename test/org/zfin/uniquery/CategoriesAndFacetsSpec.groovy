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

    SolrQuery query = new SolrQuery();

    //sets up for all tests in class
    public def setupSpec() {
        client = SolrService.getSolrClient("prototype")
    }

    @Unroll
    def "#category category doesn't exist"() {

        when: "do a no result query asking for category facets"
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
        [category, field] << [
                (Category.GENE)                      : [
                        FieldName.AFFECTED_ANATOMY_TF,
                        FieldName.AFFECTED_BIOLOGICAL_PROCESS_TF,
                        FieldName.AFFECTED_CELLULAR_COMPONENT_TF,
                        FieldName.AFFECTED_MOLECULAR_FUNCTION_TF,
                        FieldName.ANATOMY_TF,
                        FieldName.BIOLOGICAL_PROCESS_TF,
                        FieldName.CELLULAR_COMPONENT_TF,
                        FieldName.CHROMOSOME,
                        FieldName.DISEASE,
                        FieldName.MISEXPRESSED_GENE,
                        FieldName.MOLECULAR_FUNCTION_TF,
                        FieldName.PHENOTYPE_STATEMENT,
                        FieldName.STAGE,
                        FieldName.TYPE
                ],
                (Category.FISH)                      : [
                        FieldName.AFFECTED_ANATOMY_TF,
                        FieldName.AFFECTED_BIOLOGICAL_PROCESS_TF,
                        FieldName.AFFECTED_CELLULAR_COMPONENT_TF,
                        FieldName.AFFECTED_GENE,
                        FieldName.AFFECTED_MOLECULAR_FUNCTION_TF,
                        FieldName.BACKGROUND,
                        FieldName.CONSTRUCT,
                        FieldName.EXPRESSION_ANATOMY_TF,
                        FieldName.MISEXPRESSED_GENE,
                        FieldName.PHENOTYPE_STATEMENT,
                        FieldName.SEQUENCE_ALTERATION,
                        FieldName.SEQUENCE_TARGETING_REAGENT
                ],
                (Category.MUTANT)                    : [
                        FieldName.AFFECTED_ANATOMY_TF,
                        FieldName.AFFECTED_BIOLOGICAL_PROCESS_TF,
                        FieldName.AFFECTED_CELLULAR_COMPONENT_TF,
                        FieldName.AFFECTED_GENE,
                        FieldName.AFFECTED_MOLECULAR_FUNCTION_TF,
                        FieldName.CONSEQUENCE,
                        FieldName.INSTITUTION,
                        FieldName.LAB_OF_ORIGIN,
                        FieldName.MISEXPRESSED_GENE,
                        FieldName.PHENOTYPE_STATEMENT,
                        FieldName.SOURCE,
                        FieldName.TYPE
                ],
                (Category.CONSTRUCT)                 : [
                        FieldName.CODING_SEQUENCE,
                        FieldName.ENGINEERED_REGION,
                        FieldName.EXPRESSED_IN_TF,
                        FieldName.INSERTED_IN_GENE,
                        FieldName.REGULATORY_REGION,
                        FieldName.REPORTER_COLOR,
                        FieldName.TYPE
                ],
                (Category.SEQUENCE_TARGETING_REAGENT): [
                        FieldName.TARGETED_GENE,
                        FieldName.TYPE
                ],
                (Category.MARKER)                    : [
                        FieldName.CHROMOSOME,
                        FieldName.SOURCE,
                        FieldName.TYPE
                ],
                (Category.FIGURE)                    : [
                        FieldName.AFFECTED_ANATOMY_TF,
                        FieldName.AFFECTED_BIOLOGICAL_PROCESS_TF,
                        FieldName.AFFECTED_CELLULAR_COMPONENT_TF,
                        FieldName.AFFECTED_MOLECULAR_FUNCTION_TF,
                        FieldName.AUTHOR,
                        FieldName.CONSTRUCT,
                        FieldName.EXPRESSION_ANATOMY_TF,
                        FieldName.MISEXPRESSED_GENE,
                        FieldName.PHENOTYPE_STATEMENT,
                        FieldName.REPORTER_GENE,
                        FieldName.ZEBRAFISH_GENE
                ],
                (Category.EXPRESSIONS)               : [
                        FieldName.ASSAY,
                        FieldName.AUTHOR,
                        FieldName.EXPERIMENTAL_CONDITIONS,
                        FieldName.EXPRESSION_ANATOMY_TF,
                        FieldName.GENOTYPE_FULL_NAME,
                        FieldName.HAS_IMAGE,
                        FieldName.REPORTER_GENE,
                        FieldName.SEQUENCE_TARGETING_REAGENT,
                        FieldName.ZEBRAFISH_GENE
                ],
                (Category.PHENOTYPE)                 : [
                        FieldName.ANATOMY_TF,
                        FieldName.BIOLOGICAL_PROCESS_TF,
                        FieldName.HAS_IMAGE,
                        FieldName.MISEXPRESSED_GENE,
                        FieldName.MOLECULAR_FUNCTION_TF,
                        FieldName.PHENOTYPE_STATEMENT,
                        FieldName.SEQUENCE_TARGETING_REAGENT,
                        FieldName.STAGE
                ],
                (Category.ANATOMY)                   : [
                        FieldName.ONTOLOGY,
                        FieldName.TERM_STATUS
                ],
                (Category.COMMUNITY)                 : [
                        FieldName.TYPE
                ],
                (Category.PUBLICATION)               : [
                        FieldName.AUTHOR,
                        FieldName.GENE,
                        FieldName.JOURNAL,
                        FieldName.KEYWORD,
                        FieldName.MESH_TERM,
                        FieldName.PUBLICATION_TYPE,
                        FieldName.SEQUENCE_ALTERATION
                ],
                (Category.ANTIBODY)                  : [
                        FieldName.ANTIGEN_GENE,
                        FieldName.ASSAY,
                        FieldName.HOST_ORGANISM,
                        FieldName.LABELED_STRUCTURE_TF,
                        FieldName.SOURCE,
                        FieldName.TYPE
                ],
                (Category.DISEASE)                   : [
                        FieldName.EXPERIMENTAL_CONDITIONS,
                        FieldName.FISH,
                        FieldName.GENE
                ],
                (Category.REPORTER_LINE)             : [
                        FieldName.EXPRESSION_ANATOMY_TF,
                        FieldName.REGULATORY_REGION
                ]
        ].collectMany { category, facets -> facets.collect { facet -> [category, facet] } }
    }
}
