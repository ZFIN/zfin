package org.zfin.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.zfin.search.FieldName.*;

/**
 * Enumeration of categories used in Faceted search including field names per category
 */
public enum Category {

    GENE("Gene / Transcript",
            TYPE,
            EXPRESSED_IN_TF,
            AFFECTED_ANATOMY_TF,
            AFFECTED_BIOLOGICAL_PROCESS_TF,
            AFFECTED_MOLECULAR_FUNCTION_TF,
            AFFECTED_CELLULAR_COMPONENT_TF,
            FieldName.DISEASE,
            PHENOTYPE_STATEMENT,
            BIOLOGICAL_PROCESS_TF,
            MOLECULAR_FUNCTION_TF,
            CELLULAR_COMPONENT_TF,
            CHROMOSOME
    ),
    CONSTRUCT("Construct",
            TYPE,
            REGULATORY_REGION,
            CODING_SEQUENCE,
            INSERTED_IN_GENE,
            EXPRESSED_IN_TF,
            REPORTER_COLOR,
            ENGINEERED_REGION
    ),
    ANTIBODY("Antibody",
            TYPE,
            ANTIGEN_GENE,
            LABELED_STRUCTURE_TF,
            ASSAY,
            SOURCE,
            HOST_ORGANISM
    ),
    ANATOMY("Anatomy / GO",
            ONTOLOGY,
            TERM_STATUS
    ),
    DISEASE("Human Disease",
            FieldName.GENE,
            FieldName.DISEASE_MODEL,
            FieldName.EXPERIMENTAL_CONDITIONS
    ),
    MUTANT("Mutation / Tg",
            TYPE,
            AFFECTED_GENE,
            PHENOTYPE_STATEMENT,
            AFFECTED_ANATOMY_TF,
            AFFECTED_BIOLOGICAL_PROCESS_TF,
            AFFECTED_MOLECULAR_FUNCTION_TF,
            AFFECTED_CELLULAR_COMPONENT_TF,
            SOURCE,
            LAB_OF_ORIGIN,
            INSTITUTION,
            MUTAGEN

//            SCREEN   <!--screen used to be here, removed as a result of case 11323-->
    ),
    PUBLICATION("Publication",
            //facet queries
            asList(FacetQueryEnum.DATE_LAST_30_DAYS,
                    FacetQueryEnum.DATE_LAST_90_DAYS,
                    FacetQueryEnum.DATE_THIS_YEAR,
                    FacetQueryEnum.DATE_THIS_YEAR_MINUS_1,
                    FacetQueryEnum.DATE_THIS_YEAR_MINUS_2,
                    FacetQueryEnum.DATE_THIS_YEAR_MINUS_3,
                    FacetQueryEnum.DATE_THIS_YEAR_MINUS_4,
                    FacetQueryEnum.DATE_THIS_YEAR_MINUS_5,
                    FacetQueryEnum.DATE_MORE_THAN_5_YEARS),
            FieldName.GENE,
            SEQUENCE_ALTERATION,
            FieldName.DISEASE,
            AUTHOR,
            JOURNAL,
            KEYWORD,
            PUBLICATION_TYPE,
            TOPIC,
            CURATION_STATUS,
            PUBLICATION_STATUS,
            INDEXING_STATUS,
            CURATOR
    ),
    FIGURE("Figure",
            ZEBRAFISH_GENE,
            REPORTER_GENE,
            AFFECTED_ANATOMY_TF,
            AFFECTED_BIOLOGICAL_PROCESS_TF,
            AFFECTED_MOLECULAR_FUNCTION_TF,
            AFFECTED_CELLULAR_COMPONENT_TF,
            PHENOTYPE_QUALITY_TF,
            PHENOTYPE_STATEMENT,
            FieldName.CONSTRUCT,
            HAS_IMAGE,
            EXPRESSIONS_ANATOMY_TF,
            AUTHOR
    ),
    MARKER("Marker / Clone",
            TYPE,
            CHROMOSOME
    ),
    SEQUENCE_TARGETING_REAGENT("Sequence Targeting Reagent (STR)",
            TYPE,
            TARGETED_GENE
    ),
    EXPRESSIONS("Expression",
            asList( //FacetQueryEnum.ANY_ZEBRAFISH_GENE,
                    //FacetQueryEnum.ANY_REPORTER_GENE,
                    FacetQueryEnum.ANY_WILDTYPE,
                    FacetQueryEnum.ANY_MUTANT),
            REPORTER_GENE,
            ZEBRAFISH_GENE,
            EXPRESSIONS_ANATOMY_TF,
            STAGE,
            ASSAY,
            GENOTYPE,
            AUTHOR,
            EXPERIMENTAL_CONDITIONS,
            HAS_IMAGE,
            FieldName.SEQUENCE_TARGETING_REAGENT,
            IS_WILDTYPE
    ),
    PHENOTYPE("Phenotype",
            FieldName.GENE,
            SEQUENCE_ALTERATION,
            IS_MONOGENIC,
            STAGE,
            ANATOMY_TF,
            BIOLOGICAL_PROCESS_TF,
            MOLECULAR_FUNCTION_TF,
            CELLULAR_COMPONENT_TF,
            PHENOTYPE_STATEMENT,
            HAS_IMAGE,
            FieldName.SEQUENCE_TARGETING_REAGENT
    ),
    COMMUNITY("Community",
            TYPE
    ),
    /*
        PERSON("Person",
                FieldName.GENE,
                SEQUENCE_ALTERATION
        ),
    */
    FISH("Fish", AFFECTED_GENE,
            FieldName.DISEASE,
            EXPRESSIONS_ANATOMY_TF,
            AFFECTED_ANATOMY_TF,
            AFFECTED_BIOLOGICAL_PROCESS_TF,
            AFFECTED_MOLECULAR_FUNCTION_TF,
            AFFECTED_CELLULAR_COMPONENT_TF,
            PHENOTYPE_STATEMENT,
            FieldName.SEQUENCE_TARGETING_REAGENT,
            FieldName.CONSTRUCT,
            SEQUENCE_ALTERATION,
            BACKGROUND),;


    Category(String name, FieldName... fieldNames) {
        this.name = name;
        this.fieldNames = fieldNames;
        this.facetQueries = new ArrayList<>();
    }

    Category(String name, List<FacetQueryEnum> facetQueries, FieldName... fieldNames) {
        this.name = name;
        this.fieldNames = fieldNames;
        this.facetQueries = facetQueries;
    }

    private String name;
    private FieldName[] fieldNames;
    private List<FacetQueryEnum> facetQueries;

    public static Category getCategory(String name) {
        if (name == null)
            return null;
        for (Category category : values()) {
            if (category.getName().equals(name))
                return category;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public FieldName[] getFieldNames() {
        return fieldNames;
    }

    public List<FacetQueryEnum> getFacetQueries() {
        return facetQueries;
    }

    public String getConcatenateFieldNames() {
        if (fieldNames == null)
            return null;
        StringBuilder builder = new StringBuilder();
        for (FieldName fieldName : fieldNames) {
            builder.append(fieldName.getName());
            builder.append(" ");
        }
        return builder.toString();
    }

    public String[] getFieldArray() {
        List<String> fields = new ArrayList<>();
        for (FieldName fieldName : fieldNames) {
            fields.add(fieldName.getName());
        }

        return fields.toArray(new String[fields.size()]);
    }

    private static Map<String, String> facetMap = new HashMap<>();

    /*  This is mostly legacy code that the methods below are replacing, but it still used for telling SOLR which facets
 *  need to be queried.   Eventually, buildFacetGroup shouldn't require the SOLR response, and another method like
  *  populateFacetGroups(...)  would inject the facets into structure and this code can go away ..  */
    public static Map<String, String> getFacetMap() {
        if (facetMap != null && facetMap.size() > 0)
            return facetMap;
        for (Category category : Category.values())
            facetMap.put(category.getName(), category.getConcatenateFieldNames());

        return facetMap;
    }

}
