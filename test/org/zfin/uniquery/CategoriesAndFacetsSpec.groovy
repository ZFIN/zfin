package org.zfin.uniquery

import org.apache.log4j.Logger
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.FacetField
import org.apache.solr.client.solrj.response.QueryResponse
import org.springframework.beans.factory.annotation.Autowired
import org.zfin.ZfinIntegrationSpec
import org.zfin.search.Category
import org.zfin.search.FieldName
import org.zfin.search.presentation.FacetGroup
import org.zfin.search.service.FacetBuilderService
import org.zfin.search.service.SolrService
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
        category << Category.values().findAll { it != Category.STR_RELATIONSHIP }
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
                        FieldName.CONDITIONS,
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
                        FieldName.CONDITIONS,
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
                        FieldName.ANTIBODY_TYPE
                ],
                (Category.DISEASE)                   : [
                        FieldName.GENE,
                        FieldName.FISH,
                        FieldName.CONDITIONS
                ],
                (Category.REPORTER_LINE)             : [
                        FieldName.EXPRESSION_ANATOMY_TF,
                        FieldName.REGULATORY_REGION
                ],
                (Category.JOURNAL)                   : [
                        FieldName.RELATED_ACCESSION
                ]
        ].collectMany { category, fields -> fields.collect { field -> [category, field] } }
    }

    @Unroll
    def "#category category groups"() {
        when:
        SolrService.setCategory(category.name, query)
        query.rows = 20
        QueryResponse response = client.query(query)
        FacetBuilderService facetBuilder = new FacetBuilderService(response, "", new HashMap<String, Boolean>())
        List<FacetGroup> facetGroups = facetBuilder.buildFacetGroup(category.name)

        then:
        facetGroups*.label == expectedLabels

        where:
        category                            | expectedLabels
        Category.GENE                       | ["Type", "Expression", "Phenotype", "Human Disease", "Gene Ontology", "Location"]
        Category.EXPRESSIONS                | ["Expressed Gene", "Expressed In Anatomy", "Stage", "Has Image", "Is Wildtype and Clean", "Assay", "Genotype", "Sequence Targeting Reagent (STR)", "Conditions"]
        Category.PHENOTYPE                  | ["Phenotypic Gene", "Phenotype Statement", "Stage", "Manifests In", "Sequence Targeting Reagent (STR)", "Is Monogenic", "Conditions", "Has Image"]
        Category.DISEASE                    | ["Gene", "Disease Model"]
        Category.FISH                       | ["Affected Gene", "Is Model Of", "Expression Anatomy", "Phenotype", "Sequence Targeting Reagent (STR)", "Construct", "Mutation / Tg", "Background"]
        Category.REPORTER_LINE              | ["Reporter Gene", "Expression Anatomy", "Regulatory Region", "Stage"]
        Category.MUTANT                     | ["Type", "Affected Gene", "Phenotype", "Consequence", "Mutagen", "Source", "Lab of Origin", "Institution"]
        Category.CONSTRUCT                  | ["Type", "Regulatory Region", "Coding Sequence", "Inserted In Gene", "Expressed In", "Reporter Color", "Engineered Region"]
        Category.SEQUENCE_TARGETING_REAGENT | ["Type", "Targeted Gene"]
        Category.ANTIBODY                   | ["Type", "Antigen Gene", "Labeled Structure", "Assay", "Source", "Host Organism"]
        Category.MARKER                     | ["Type", "Location", "Source"]
        Category.FIGURE                     | ["Expression Anatomy", "Expressed Gene", "Phenotype", "Construct", "Registered Author", "Has Image"]
        Category.ANATOMY                    | ["Ontology", "Term Status"]
        Category.COMMUNITY                  | ["Type"]
        Category.PUBLICATION                | ["Curation", "Gene", "Mutation / Tg", "Human Disease", "Registered Author", "Journal", "Keyword", "MeSH Term", "Publication Type", "Publication Date"]
        Category.JOURNAL                    | ["Accession"]
    }
}
