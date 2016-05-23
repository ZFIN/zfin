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
        facetValues.contains(category)

        where:
        category << [Category.GENE.name, Category.FISH.name, Category.MUTANT.name, Category.CONSTRUCT.name,
                Category.SEQUENCE_TARGETING_REAGENT.name, Category.MARKER.name, Category.FIGURE.name,
                Category.EXPRESSIONS.name, Category.PHENOTYPE.name, Category.ANATOMY.name, Category.COMMUNITY.name,
                Category.PUBLICATION.name, Category.ANTIBODY.name, Category.DISEASE.name]

    }

    @Unroll
    def "#category should have #field"() {
        when:
        SolrService.setCategory(category, query)
        query.rows = 0
        QueryResponse response = new QueryResponse();

        try {
            response = client.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        def returnedFacets = response.facetFields*.name

        then:
        returnedFacets.contains(field)

        where:
        [category, field] << [[Category.GENE.getName()] , [FieldName.ANATOMY_TF.getName(),FieldName.STAGE.name, "affected_anatomy_tf", "affected_biological_process_tf",
                                          "affected_molecular_function_tf", "affected_cellular_component_tf",
                                          "phenotype_statement", FieldName.MISEXPRESSED_GENE.getName(), "disease", "biological_process_tf",
                                          "molecular_function_tf", "cellular_component_tf","chromosome","type"]].combinations() \
                             + [[Category.FISH.getName()],["affected_gene","affected_anatomy_tf", "affected_biological_process_tf",
                                           "affected_molecular_function_tf", "affected_cellular_component_tf",
                                           "phenotype_statement", FieldName.MISEXPRESSED_GENE.getName(), FieldName.EXPRESSIONS_ANATOMY_TF.getName(), "sequence_targeting_reagent",
                                           "construct", "sequence_alteration", "background"]].combinations() \
                             + [[Category.MUTANT.getName()],["type","affected_gene", "affected_anatomy_tf", "affected_biological_process_tf",
                                           "affected_molecular_function_tf", "affected_cellular_component_tf", "phenotype_statement", FieldName.MISEXPRESSED_GENE.getName(),
                                           "source", "lab_of_origin", "rna_consequence","institution"]].combinations() \
                             + [[Category.CONSTRUCT.getName()],["type","regulatory_region", "coding_sequence","inserted_in_gene",FieldName.EXPRESSED_IN_TF.getName(),
                                           "reporter_color","engineered_region"]].combinations() \
                             + [[Category.SEQUENCE_TARGETING_REAGENT.getName()],["type","targeted_gene"]].combinations() \
                             + [[Category.MARKER.getName()],["type", "chromosome","source",]].combinations() \
                             + [[Category.FIGURE.getName()],[FieldName.EXPRESSIONS_ANATOMY_TF.getName(), "reporter_gene", "zebrafish_gene", "affected_anatomy_tf",
                                           "affected_biological_process_tf", "affected_molecular_function_tf",
                                           "affected_cellular_component_tf", "phenotype_statement", FieldName.MISEXPRESSED_GENE.getName(),
                                           "construct", "registered_author"]].combinations() \
                             + [[Category.EXPRESSIONS.getName()],["reporter_gene", "zebrafish_gene", FieldName.EXPRESSIONS_ANATOMY_TF.getName(), "assay", "genotype",
                                           "has_image","experimental_conditions","registered_author","sequence_targeting_reagent" ]].combinations() \
                             + [[Category.PHENOTYPE.getName()],["phenotype_statement", FieldName.MISEXPRESSED_GENE.getName(),"anatomy_tf","biological_process_tf","molecular_function_tf",
                                               "has_image","stage","sequence_targeting_reagent"]].combinations() \
                             + [[Category.ANATOMY.getName()],["ontology", "term_status"]].combinations() \
                             + [[Category.COMMUNITY.getName()],["type"]].combinations() \
                             + [[Category.PUBLICATION.getName()],["gene", "sequence_alteration", "registered_author", "journal", "keyword", FieldName.MESH_TERM.getName(), "publication_type"]].combinations() \
                             + [[Category.ANTIBODY.getName()],["type","antigen_gene","labeled_structure_tf", "assay", "source", "host_organism"]].combinations() \
                             + [[Category.DISEASE.getName()],[FieldName.GENE.name,FieldName.FISH.name,FieldName.EXPERIMENTAL_CONDITIONS.name]].combinations() \
                             + [[Category.REPORTER_LINE.getName()],[FieldName.EXPRESSIONS_ANATOMY_TF.getName(),FieldName.REGULATORY_REGION.getName()]].combinations()
        }


}
