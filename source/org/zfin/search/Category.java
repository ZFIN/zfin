package org.zfin.search;

import org.zfin.profile.service.ProfileService;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.zfin.search.FieldName.*;

/**
 * Enumeration of categories used in Faceted search including field names per category
 */
public enum Category {

    GENE("Gene / Transcript",
            new ArrayList<FacetQueryEnum>(),
            TYPE_TREE,
            ANATOMY_TF,
            STAGE,
            AFFECTED_ANATOMY_TF,
            AFFECTED_BIOLOGICAL_PROCESS_TF,
            AFFECTED_MOLECULAR_FUNCTION_TF,
            AFFECTED_CELLULAR_COMPONENT_TF,
            FieldName.DISEASE,
            PHENOTYPE_STATEMENT,
            MISEXPRESSED_GENE,
            BIOLOGICAL_PROCESS_TF,
            MOLECULAR_FUNCTION_TF,
            CELLULAR_COMPONENT_TF,
            CHROMOSOME
    ),
    CONSTRUCT("Construct",
            TYPE,
            REGULATORY_REGION,
            REGULATORY_REGION_SPECIES,
            CODING_SEQUENCE,
            CODING_SEQUENCE_SPECIES,
            INSERTED_IN_GENE,
            EXPRESSED_IN_TF,
            LAB_OF_ORIGIN,
            REPORTER_COLOR,
            EMISSION_COLOR,
            EXCITATION_COLOR,
            ENGINEERED_REGION
    ),
    ANTIBODY("Antibody",
            ANTIBODY_TYPE,
            ANTIGEN_GENE,
            LABELED_STRUCTURE_TF,
            ASSAY,
            SOURCE,
            HOST_ORGANISM
    ),
    ANATOMY("Ontologies",
            ONTOLOGY,
            TERM_STATUS
    ),
    DISEASE("Human Disease",
            FieldName.GENE,
            FieldName.FISH,
            FieldName.ZECO_CONDITIONS
    ),
    MUTANT("Mutation / Tg",
            TYPE,
            AFFECTED_GENE,
            PHENOTYPE_STATEMENT,
            AFFECTED_ANATOMY_TF,
            AFFECTED_BIOLOGICAL_PROCESS_TF,
            AFFECTED_MOLECULAR_FUNCTION_TF,
            AFFECTED_CELLULAR_COMPONENT_TF,
            MISEXPRESSED_GENE,
            SOURCE,
            LAB_OF_ORIGIN,
            CONSEQUENCE,
            INSTITUTION,
            MUTAGEN,
            REGULATORY_REGION,
            CODING_SEQUENCE,
            ANY_COLOR,
            EMISSION_COLOR,
            EXCITATION_COLOR,
            IS_ZEBRASHARE

//            SCREEN   <!--screen used to be here, removed as a result of case 11323-->
    ),
    PUBLICATION("Publication",
            //facet queries for pub dates can be found in FacetBuilderService
            FieldName.GENE,
            SEQUENCE_ALTERATION,
            FieldName.DISEASE,
            HAS_CTD,
        AUTHOR,
            FieldName.JOURNAL,
            KEYWORD,
            MESH_TERM,
            PUBLICATION_TYPE,
            TOPIC,
            CURATION_STATUS,
            PUBLICATION_STATUS,
            CURATION_LOCATION,
            PUB_OWNER
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
            MISEXPRESSED_GENE,
            FieldName.CONSTRUCT,
            HAS_IMAGE,
            EXPRESSION_ANATOMY_TF,
            AUTHOR
    ),
    MARKER("Marker / Clone",
            TYPE,
            CHROMOSOME,
            SOURCE
    ),
    SEQUENCE_TARGETING_REAGENT("Sequence Targeting Reagent (STR)",
            TYPE,
            TARGET
    ),
    EXPRESSIONS("Expression",
            asList(FacetQueryEnum.ANY_ZEBRAFISH_GENE,
                    FacetQueryEnum.ANY_REPORTER_GENE,
                    FacetQueryEnum.ANY_WILDTYPE,
                    FacetQueryEnum.ANY_MUTANT,
                    FacetQueryEnum.NONE_SEQUENCE_TARGETING_REAGENT),
            TYPE,
            REPORTER_GENE,
            ZEBRAFISH_GENE,
            EXPRESSION_ANATOMY_TF,
            STAGE,
            ASSAY,
            GENOTYPE_FULL_NAME,
            AUTHOR,
            ZECO_CONDITIONS,
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
            MISEXPRESSED_GENE,
            GENOTYPE_FULL_NAME,
            HAS_IMAGE,
            ZECO_CONDITIONS,
            FieldName.SEQUENCE_TARGETING_REAGENT
    ),
    COMMUNITY("Community",
            TYPE,
            COUNTRY
    ),
    FISH("Fish",
            AFFECTED_GENE,
            FieldName.DISEASE,
            EXPRESSION_ANATOMY_TF,
            AFFECTED_ANATOMY_TF,
            AFFECTED_BIOLOGICAL_PROCESS_TF,
            AFFECTED_MOLECULAR_FUNCTION_TF,
            AFFECTED_CELLULAR_COMPONENT_TF,
            PHENOTYPE_STATEMENT,
            MISEXPRESSED_GENE,
            FieldName.SEQUENCE_TARGETING_REAGENT,
            FieldName.CONSTRUCT,
            SEQUENCE_ALTERATION,
            BACKGROUND,
            SOURCE
    ),
    REPORTER_LINE("Reporter Line",
            REPORTER_GENE,
            EXPRESSION_ANATOMY_TF,
            REGULATORY_REGION,
            STAGE,
            ANY_COLOR,
            EMISSION_COLOR,
            EXCITATION_COLOR,
            SOURCE
    ),
    JOURNAL("Journal",
            RELATED_ACCESSION
    ),
    STR_RELATIONSHIP("STR Relationship");


    Category(String name, FieldName... fieldNames) {
        this.name = name;
        this.fieldNames = fieldNames;
        this.facetQueries = new ArrayList<>();
    }

    Category(String name,
             List<FacetQueryEnum> facetQueries,
             FieldName... fieldNames) {
        this.name = name;
        this.fieldNames = fieldNames;
        this.facetQueries = facetQueries;
    }

    private String name;
    private FieldName[] fieldNames;
    private List<FacetQueryEnum> facetQueries;

    public static Category getCategory(String name) {
        if (name == null) {
            return null;
        }
        for (Category category : values()) {
            if (category.getName().equals(name)) {
                return category;
            }
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
        if (fieldNames == null) {
            return null;
        }
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
            if (!fieldName.isHierarchical()) {
                fields.add(fieldName.getName());
            }
        }

        return fields.toArray(new String[fields.size()]);
    }

    public List<String> getPivotFacetStrings() {
        List<String> pivotStrings = new ArrayList<>();
        for (FieldName fieldName : fieldNames) {
            if (fieldName.isHierarchical()) {
                pivotStrings.add(fieldName.getPivotKey());
            }
        }
        return pivotStrings;
    }

    public List<FacetQueryEnum> getFacetQueriesForField(FieldName fieldName) {
        List<FacetQueryEnum> matchingFacetQueries = new ArrayList<>();

        for (FacetQueryEnum facetQueryEnum : facetQueries) {
            if (facetQueryEnum.getFieldName() == fieldName) {
                matchingFacetQueries.add(facetQueryEnum);
            }
        }
        return matchingFacetQueries;
    }

    public static Collection<String> getCategoryDisplayList() {
        return Arrays.stream(Category.values())
                .filter(category -> {
                    if (category == STR_RELATIONSHIP) {
                        return false;
                    }
                    return category != JOURNAL || ProfileService.isRootUser();
                })
                .map(Category::getName)
                .sorted(FacetCategoryComparator::compareString)
                .collect(Collectors.toList());
    }

    private static Map<String, String> facetMap = new HashMap<>();

    /*  This is mostly legacy code that the methods below are replacing, but it still used for telling SOLR which facets
 *  need to be queried.   Eventually, buildFacetGroup shouldn't require the SOLR response, and another method like
  *  populateFacetGroups(...)  would inject the facets into structure and this code can go away ..  */
    public static Map<String, String> getFacetMap() {
        if (facetMap != null && facetMap.size() > 0) {
            return facetMap;
        }
        for (Category category : Category.values()) {
            facetMap.put(category.getName(), category.getConcatenateFieldNames());
        }

        return facetMap;
    }

}
