package org.zfin.search.service;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;
import org.zfin.search.*;
import org.zfin.search.presentation.Facet;
import org.zfin.search.presentation.FacetGroup;
import org.zfin.search.presentation.FacetQuery;
import org.zfin.search.presentation.FacetValue;
import org.apache.http.NameValuePair;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static org.zfin.search.FieldName.*;
import static java.util.Arrays.asList;


@Service
public class FacetBuilderService {

    public static Logger logger = Logger.getLogger(FacetBuilderService.class);

    public static String PHENOTYPE_FACET_QUERY = "phenotype_statement:[* TO *]";


    public List<FacetGroup> buildFacetGroup(String category, QueryResponse response, SolrQuery query, String baseUrl) {

        //a map to know whether or not to show a facet value as a link
        Map<String, Boolean> filterQuerySelectionMap = new HashMap<>();

        if (query != null && query.getFilterQueries() != null) {
            for (String fq : query.getFilterQueries()) {
                filterQuerySelectionMap.put(fq, true);
                logger.debug("added to filterQuerySelectionMap: " + fq);
            }
        }

        if (StringUtils.isEmpty(category) || StringUtils.equals(category, "Any")) {
            return buildCategoryFacetGroup(response, filterQuerySelectionMap, baseUrl);
        }
        else if (StringUtils.equals(category, Category.GENE.getName())) {
            return buildGeneFacetGroup(response, filterQuerySelectionMap, baseUrl);
        }
        else if (StringUtils.equals(category, Category.MUTANT.getName())) {
            return buildFeatureFacetGroup(response, filterQuerySelectionMap, baseUrl);
        }
        else if (StringUtils.equals(category, Category.FISH.getName())) {
            return buildFishFacetGroup(response, filterQuerySelectionMap, baseUrl);
        }
        else if (StringUtils.equals(category, Category.FIGURE.getName())) {
            return buildFigureFacetGroup(response, filterQuerySelectionMap, baseUrl);
        }
        else if (StringUtils.equals(category, Category.EXPRESSIONS.getName())) {
            return buildExpressionFacetGroup(response, filterQuerySelectionMap, baseUrl);
        }
        else if (StringUtils.equals(category, Category.PHENOTYPE.getName())) {
            return buildPhenotypeFacetGroup(response, filterQuerySelectionMap, baseUrl);
        }
        else if (StringUtils.equals(category, Category.PUBLICATION.getName())) {
            return buildPublicationFacetGroup(response, filterQuerySelectionMap, baseUrl);
        }
        else if (StringUtils.equals(category,Category.DISEASE.getName())) {
            return buildDiseaseFacetGroup(response, filterQuerySelectionMap, baseUrl);
        }
        else {
            return buildFlatFacetGroup(category, response, filterQuerySelectionMap, baseUrl);
        }


    }

    /* generically build a "one-facet-per-group" section for any category in the map */
    public List<FacetGroup> buildFlatFacetGroup(String category, QueryResponse response, Map<String, Boolean> filterQuerySelectionMap, String baseUrl) {

        //if the category is not in our closed set, just give up.
        if (!Category.getFacetMap().containsKey(category))
            return null;

        logger.debug("category: " + category);
        logger.debug("fields: " + Category.getFacetMap().get(category));

        List<FacetGroup> facetGroups = new ArrayList<>();

        String[] facetFields = StringUtils.split(Category.getFacetMap().get(category));

        int count = 0;
        for (String fieldName : facetFields) {
            logger.debug("fieldName:" + fieldName);
            boolean open;
            open = count <= 2;
            FacetGroup facetGroup = new FacetGroup(SolrService.getPrettyFieldName(fieldName), open);
            facetGroup.addFacet(buildFacet(fieldName, true, response, filterQuerySelectionMap, baseUrl));
            facetGroups.add(facetGroup);
            count++;
        }

        return facetGroups;
    }


    public List<FacetGroup> buildCategoryFacetGroup(QueryResponse response, Map<String, Boolean> filterQuerySelectionMap, String baseUrl) {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup category = new FacetGroup("Category", true);
        Facet categoryFacet = buildFacet("category", true, response, filterQuerySelectionMap, baseUrl);
        categoryFacet.setShowIncludeExcludeIcons(false);
        categoryFacet.setAlwaysShowAllFacets(true);
        categoryFacet.setShowAutocompleteBox(false);
        category.addFacet(categoryFacet);
        facetGroups.add(category);


        return facetGroups;
    }

    public List<FacetGroup> buildDiseaseFacetGroup(QueryResponse response, Map<String, Boolean> filterQuerySelectionMap, String baseUrl) {
        List<FacetGroup> facetGroups = new ArrayList<>();

        facetGroups.add(buildSingleFacetGroup("Gene",FieldName.GENE.getName(), true, response, filterQuerySelectionMap, baseUrl ));

        FacetGroup diseaseModel = new FacetGroup("Disease Model", true);
        diseaseModel.addFacet(buildFacet(FieldName.FISH.getName(), true,response, filterQuerySelectionMap, baseUrl));
        diseaseModel.addFacet(buildFacet(FieldName.EXPERIMENTAL_CONDITIONS.getName(), true,response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(diseaseModel);

        return facetGroups;
    }


    /*  This is just a temporary method, should be replaced by further configuration */
    public List<FacetGroup> buildGeneFacetGroup(QueryResponse response, Map<String, Boolean> filterQuerySelectionMap, String baseUrl) {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup type = new FacetGroup("Type", true);
        type.addFacet(buildFacet("type", true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(type);

        FacetGroup expressedIn = new FacetGroup("Expression", true);
        expressedIn.addFacet(buildFacet(ANATOMY_TF.getName(), true, response, filterQuerySelectionMap, baseUrl));
        expressedIn.addFacet(buildFacet(FieldName.STAGE.getName(), false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(expressedIn);


        facetGroups.add(buildPhenotypeBlock(response, filterQuerySelectionMap, baseUrl));


        FacetGroup diseaseModel = new FacetGroup("Human Disease");
        diseaseModel.addFacet(buildFacet(DISEASE.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(diseaseModel);


        FacetGroup geneOntology = new FacetGroup("Gene Ontology");
        geneOntology.addFacet(buildFacet(BIOLOGICAL_PROCESS_TF.getName(), true, response, filterQuerySelectionMap, baseUrl));
        geneOntology.addFacet(buildFacet(MOLECULAR_FUNCTION_TF.getName(), true, response, filterQuerySelectionMap, baseUrl));
        geneOntology.addFacet(buildFacet(CELLULAR_COMPONENT_TF.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(geneOntology);

        FacetGroup location = new FacetGroup("Location", false);
        Facet locationFacet = buildFacet(CHROMOSOME.getName(), true, response, filterQuerySelectionMap, baseUrl);
        locationFacet.setAlwaysShowAllFacets(true);
        location.addFacet(locationFacet);
        facetGroups.add(location);

        return facetGroups;

    }

    public List<FacetGroup> buildExpressionFacetGroup(QueryResponse response, Map<String, Boolean> filterQuerySelectionMap, String baseUrl) {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup expressedGene = new FacetGroup("Expressed Gene", true);

        expressedGene.addFacet(buildFacet(FieldName.ZEBRAFISH_GENE.getName(),
                Category.EXPRESSIONS.getFacetQueriesForField(FieldName.ZEBRAFISH_GENE),
                true, response, filterQuerySelectionMap, baseUrl));
        expressedGene.addFacet(buildFacet(FieldName.REPORTER_GENE.getName(),
                Category.EXPRESSIONS.getFacetQueriesForField(FieldName.REPORTER_GENE),
                false, response, filterQuerySelectionMap, baseUrl));
        
        facetGroups.add(expressedGene);

        facetGroups.add(buildSingleFacetGroup("Expressed In Anatomy", EXPRESSIONS_ANATOMY_TF.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Stage", "stage", true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Has Image", "has_image", true, response, filterQuerySelectionMap, baseUrl));
        FacetGroup wildtypeGroup = buildSingleFacetGroup("Is Wildtype and Clean", "is_wildtype", false, response, filterQuerySelectionMap, baseUrl);
        wildtypeGroup.setRootOnly(true);
        facetGroups.add(wildtypeGroup);
        facetGroups.add(buildSingleFacetGroup("Assay", "assay", false, response, filterQuerySelectionMap, baseUrl));

        FacetGroup genotype = buildSingleFacetGroup("Genotype", "genotype",
                                                    Category.EXPRESSIONS.getFacetQueriesForField(FieldName.GENOTYPE),
                                                    false, response, filterQuerySelectionMap, baseUrl);

        facetGroups.add(genotype);

        facetGroups.add(buildSingleFacetGroup("Sequence Targeting Reagent (STR)", "sequence_targeting_reagent", false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Experimental Conditions", "experimental_conditions", false, response, filterQuerySelectionMap, baseUrl));


        return facetGroups;
    }


    public List<FacetGroup> buildFeatureFacetGroup(QueryResponse response, Map<String, Boolean> filterQuerySelectionMap, String baseUrl) {
        List<FacetGroup> facetGroups = new ArrayList<>();

        facetGroups.add(buildSingleFacetGroup("Type", "type", true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Affected Gene", "affected_gene", true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildPhenotypeBlock(response, filterQuerySelectionMap, baseUrl));
        //todo: need this in the index still?
        facetGroups.add(buildSingleFacetGroup("Consequence", "rna_consequence", false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Mutagen", "mutagen", false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Source", "source", false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Lab of Origin", "lab_of_origin", false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Institution", "institution", false, response, filterQuerySelectionMap, baseUrl));
        //screen used to be here, removed as a result of case 11323
        //facetGroups.add(buildSingleFacetGroup("Screen", "screen", false, response, fqMap, baseUrl));

        return facetGroups;
    }


    public List<FacetGroup> buildFishFacetGroup(QueryResponse response, Map<String, Boolean> filterQuerySelectionMap, String baseUrl) {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup affectedGene = new FacetGroup("Affected Gene", true);
        affectedGene.addFacet(buildFacet(AFFECTED_GENE.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(affectedGene);

        FacetGroup modelOf = new FacetGroup("Is Model Of", true);
        modelOf.addFacet(buildFacet(DISEASE.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(modelOf);

        FacetGroup expressionAnatomy = new FacetGroup("Expression Anatomy", true);
        expressionAnatomy.addFacet(buildFacet(EXPRESSIONS_ANATOMY_TF.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(expressionAnatomy);

        facetGroups.add(buildPhenotypeBlock(response, filterQuerySelectionMap, baseUrl));

        FacetGroup str = new FacetGroup("Sequence Targeting Reagent (STR)");
        str.addFacet(buildFacet(SEQUENCE_TARGETING_REAGENT.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(str);

        FacetGroup construct = new FacetGroup("Construct");
        construct.addFacet(buildFacet(CONSTRUCT.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(construct);

        FacetGroup sequenceAlteration = new FacetGroup("Mutation / Tg");
        sequenceAlteration.addFacet(buildFacet(SEQUENCE_ALTERATION.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(sequenceAlteration);

        FacetGroup background = new FacetGroup("Background");
        background.addFacet(buildFacet(BACKGROUND.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(background);

        return facetGroups;

    }


    public List<FacetGroup> buildFigureFacetGroup(QueryResponse response, Map<String, Boolean> filterQuerySelectionMap, String baseUrl) {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup expressionAnatomy = new FacetGroup("Expression Anatomy", true);
        expressionAnatomy.addFacet(buildFacet(EXPRESSIONS_ANATOMY_TF.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(expressionAnatomy);

        FacetGroup expressedGene = new FacetGroup("Expressed Gene", true);
        expressedGene.addFacet(buildFacet("zebrafish_gene", true, response, filterQuerySelectionMap, baseUrl));
        expressedGene.addFacet(buildFacet("reporter_gene", true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(expressedGene);

        facetGroups.add(buildPhenotypeBlock(response, filterQuerySelectionMap, baseUrl));

        FacetGroup construct = new FacetGroup("Construct");
        construct.addFacet(buildFacet(CONSTRUCT.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(construct);


        FacetGroup author = new FacetGroup("Registered Author");
        author.addFacet(buildFacet("registered_author", true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(author);

        facetGroups.add(buildSingleFacetGroup("Has Image", "has_image", true, response, filterQuerySelectionMap, baseUrl));

        return facetGroups;
    }

    public List<FacetGroup> buildPhenotypeFacetGroup(QueryResponse response, Map<String, Boolean> filterQuerySelectionMap, String baseUrl) {
        List<FacetGroup> facetGroups = new ArrayList<>();

        facetGroups.add(buildSingleFacetGroup("Phenotypic Gene", GENE.getName(), true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Phenotype Statement", PHENOTYPE_STATEMENT.getName(), true, response, filterQuerySelectionMap, baseUrl));

        facetGroups.add(buildSingleFacetGroup("Stage", STAGE.getName(), true, response, filterQuerySelectionMap, baseUrl));

        //FacetGroup phenotype = new FacetGroup("Manifests In", false);
        FacetGroup phenotype = new FacetGroup("Manifests In", true);
        phenotype.addFacet(buildFacet(ANATOMY_TF.getName(), true, response, filterQuerySelectionMap, baseUrl));
        phenotype.addFacet(buildFacet(BIOLOGICAL_PROCESS_TF.getName(), false, response, filterQuerySelectionMap, baseUrl));
        phenotype.addFacet(buildFacet(MOLECULAR_FUNCTION_TF.getName(), false, response, filterQuerySelectionMap, baseUrl));
        phenotype.addFacet(buildFacet(CELLULAR_COMPONENT_TF.getName(), false, response, filterQuerySelectionMap, baseUrl));
        phenotype.addFacet(buildFacet(MISEXPRESSED_GENE.getName(), false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(phenotype);
        //facetGroups.add(buildSingleFacetGroup("Genes With Altered Expression","genes_with_altered_expression",false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Sequence Targeting Reagent (STR)", SEQUENCE_TARGETING_REAGENT.getName(), false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Is Monogenic", "is_monogenic", false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Has Image", "has_image", false, response, filterQuerySelectionMap, baseUrl));
        //todo: stage, conditions, author, data_source

        return facetGroups;
    }


    public List<FacetGroup> buildPublicationFacetGroup(QueryResponse response, Map<String, Boolean> filterQuerySelectionMap, String baseUrl) {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup curation = new FacetGroup("Curation", true);
        curation.setRootOnly(true);
        curation.addFacet(buildFacet("topic", false, response, filterQuerySelectionMap, baseUrl));
        curation.addFacet(buildFacet("curator", false, response, filterQuerySelectionMap, baseUrl));
        curation.addFacet(buildFacet("curation_status", false, response, filterQuerySelectionMap, baseUrl));
        curation.addFacet(buildFacet("indexing_status", false, response, filterQuerySelectionMap, baseUrl));
        curation.addFacet(buildFacet("publication_status", false, response, filterQuerySelectionMap, baseUrl));

        facetGroups.add(curation);

        facetGroups.add(buildSingleFacetGroup("Gene", "gene", true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Mutation / Tg", "sequence_alteration", true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Human Disease", "disease", true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Registered Author", "registered_author", true, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Journal", "journal", false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Keyword", "keyword", false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("MeSH Term", MESH_TERM.getName(), false, response, filterQuerySelectionMap, baseUrl));
        facetGroups.add(buildSingleFacetGroup("Publication Type", "publication_type", false, response, filterQuerySelectionMap, baseUrl));

        FacetGroup publishedDateGroup = new FacetGroup("Publication Date", false);


        //todo: some part of this code should get moved from here to somewhere re-used when we have another facet query section

        publishedDateGroup.setFacetQueries(buildFacetQueries(Category.PUBLICATION.getFacetQueries(), response, filterQuerySelectionMap, baseUrl));

        facetGroups.add(publishedDateGroup);


        return facetGroups;
    }

    public List<FacetQuery> buildFacetQueries(List<FacetQueryEnum> facetQueryEnumList,
                                              QueryResponse response,
                                              Map<String, Boolean> filterQuerySelectionMap,
                                              String baseUrl) {

        List<FacetQuery> unselectedFacetQueries = new ArrayList<>();
        List<FacetQuery> selectedFacetQueries = new ArrayList<>();

        for (FacetQueryEnum facetQueryEnum : facetQueryEnumList) {
            Integer count = response.getFacetQuery().get(facetQueryEnum.getQuery());
            if (count > 0) {
                FacetQuery facetQuery = new FacetQuery();
                facetQuery.setLabel(facetQueryEnum.getLabel());
                facetQuery.setCount(count);
                //the map tells us if it's a selected value or not
                if (filterQuerySelectionMap.containsKey(facetQueryEnum.getQuery()) && filterQuerySelectionMap.get(facetQueryEnum.getQuery()))
                    facetQuery.setSelected(true);
                else
                    facetQuery.setSelected(false);

                if (facetQuery.getSelected()) {
                    facetQuery.setUrl(SolrService.getBreadBoxUrl(facetQueryEnum.getQuery(), baseUrl));
                    selectedFacetQueries.add(facetQuery);
                } else {
                    try {
                        facetQuery.setUrl(baseUrl + "&fq=" + URLEncoder.encode(facetQueryEnum.getQuery(),"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    unselectedFacetQueries.add(facetQuery);
                }

            }
        }

        List<FacetQuery> facetQueries = new ArrayList<>();
        facetQueries.addAll(selectedFacetQueries);
        facetQueries.addAll(unselectedFacetQueries);

        return facetQueries;

    }

    public FacetGroup buildSingleFacetGroup(String label, String fieldName,
                                            boolean openByDefault,
                                            QueryResponse response,
                                            Map<String, Boolean> filterQuerySelectionMap,
                                            String baseUrl) {
        return buildSingleFacetGroup(label, fieldName, new ArrayList<FacetQueryEnum>(), openByDefault, response, filterQuerySelectionMap, baseUrl);
    }

    public FacetGroup buildSingleFacetGroup(String label, String fieldName, List<FacetQueryEnum> facetQueryEnumList,
                                            boolean openByDefault,
                                            QueryResponse response,
                                            Map<String, Boolean> filterQuerySelectionMap,
                                            String baseUrl) {
        FacetGroup group = new FacetGroup(label, openByDefault);
        group.addFacet(buildFacet(fieldName, true, response, filterQuerySelectionMap, baseUrl));
        group.setFacetQueries(buildFacetQueries(facetQueryEnumList, response, filterQuerySelectionMap, baseUrl));
        return group;
    }

    public FacetGroup buildPhenotypeBlock(QueryResponse response,
                                          Map<String, Boolean> filterQuerySelectionMap,
                                          String baseUrl) {
        FacetGroup phenotype = new FacetGroup("Phenotype", true);
        phenotype.addFacet(buildFacet(AFFECTED_ANATOMY_TF.getName(), true, response, filterQuerySelectionMap, baseUrl));
        phenotype.addFacet(buildFacet(AFFECTED_BIOLOGICAL_PROCESS_TF.getName(), false, response, filterQuerySelectionMap, baseUrl));
        phenotype.addFacet(buildFacet(AFFECTED_MOLECULAR_FUNCTION_TF.getName(), false, response, filterQuerySelectionMap, baseUrl));
        phenotype.addFacet(buildFacet(AFFECTED_CELLULAR_COMPONENT_TF.getName(), false, response, filterQuerySelectionMap, baseUrl));
        phenotype.addFacet(buildFacet(PHENOTYPE_STATEMENT.getName(), false, response, filterQuerySelectionMap, baseUrl));
        phenotype.addFacet(buildFacet(MISEXPRESSED_GENE.getName(), false, response, filterQuerySelectionMap, baseUrl));
        return phenotype;
    }


    public Facet buildFacet(String fieldName,
                            boolean openByDefault,
                            QueryResponse response,
                            Map<String, Boolean> filterQuerySelectionMap,
                            String baseUrl) {
        return buildFacet(fieldName, new ArrayList<FacetQueryEnum>(), openByDefault, response, filterQuerySelectionMap, baseUrl);
    }

    public Facet buildFacet(String fieldName,
                            List<FacetQueryEnum> facetQueryEnumList,
                            boolean openByDefault,
                            QueryResponse response,
                            Map<String, Boolean> filterQuerySelectionMap,
                            String baseUrl) {

        FacetField facetField = response.getFacetField(fieldName);

        if (facetField == null)
            return null;

        Facet facet = new Facet(facetField);

        facet.setFacetQueries(buildFacetQueries(facetQueryEnumList, response, filterQuerySelectionMap, baseUrl));

        List<String> facetQueryValues = SolrService.getFacetQueryValues(facetQueryEnumList);

        facet.setOpenByDefault(openByDefault);

        //That methods collects a list of "joinable" facets, which basically means things with names,
        //only show the autocomplete box if it has a name...
        facet.setShowAutocompleteBox(SolrService.isAnAutocompletableFacet(fieldName));

        List<FacetField.Count> selectedFacetFieldCounts = new ArrayList<>();
        List<FacetField.Count> facetFieldCounts = new ArrayList<>();


        //to take advantage of a Comparators that already exist, split into groups and sort
        //while they're still FacetField.Count, then create FacetValues based on those.


        //Get selected facets, they may not actually come back from Solr, like, if there's one
        //result and it has a ton of anatomy terms.  Since we don't need to show counts anyway,
        //build fake FacetField.Count instances and throw them in the selected list 
        for (String key : filterQuerySelectionMap.keySet()) {
	        NameValuePair nameValuePair = SolrService.splitFilterQuery(key);
            if (StringUtils.equals(nameValuePair.getName(), facetField.getName())) {
                String value = nameValuePair.getValue().replace("\"","");    
                FacetField.Count count = new FacetField.Count(facetField, value, 0);
                if (!facetQueryValues.contains(value)) {
                    selectedFacetFieldCounts.add(count);
                }
            }
        }

        //build the facet list, ignoring the selected facet values
        for (FacetField.Count count : facetField.getValues()) {
            String quotedFq = facetField.getName() + ":\"" + count.getName() + "\"";
            if (!filterQuerySelectionMap.containsKey(quotedFq)) {
                facetFieldCounts.add(count);
            }

        }

        sortFacetValues(facetField.getName(), selectedFacetFieldCounts);
        sortFacetValues(facetField.getName(), facetFieldCounts);

        List<FacetValue> selectedFacetValues = new ArrayList<>();
        List<FacetValue> facetValues = new ArrayList<>();

        for (FacetField.Count count : selectedFacetFieldCounts) {
            selectedFacetValues.add(buildFacetValue(facetField, count, true, baseUrl));
            logger.debug("breadbox facet: " + count.getName());
        }
        for (FacetField.Count count : facetFieldCounts) {
            facetValues.add(buildFacetValue(facetField, count, false, baseUrl));
            logger.debug("facet: " + count.getName());
        }

        facet.setSelectedFacetValues(selectedFacetValues);
        facet.setFacetValues(facetValues);


        //if there is a facet query for the number of docs with a value for this field, fill it in
        String key = facetField.getName() + ":[* TO *]";
        Map<String, Integer> facetQueryMap = response.getFacetQuery();
        if (facetQueryMap != null && facetQueryMap.containsKey(key))
            facet.setNonEmptyDocumentCount(facetQueryMap.get(key));
        return facet;
    }

    public FacetValue buildFacetValue(FacetField facetField, FacetField.Count count, boolean selected, String baseUrl) {
        String url;
        if (selected) {
            String quotedFq = facetField.getName() + ":\"" + count.getName() + "\"";
            url = SolrService.getBreadBoxUrl(quotedFq, baseUrl);
        } else
            url = SolrService.getFacetUrl(facetField, count, baseUrl);

        String excludeUrl = SolrService.getNotFacetUrl(facetField, count, baseUrl);

        return new FacetValue(count, selected, url, excludeUrl);
    }


    //Stubbing this out for phenotype, will obviously need to be generalized
    public List<FacetQuery> getFacetQueries(QueryResponse response, String baseUrl) {

        List<FacetQuery> facetQueryList = new ArrayList<>();

        FacetQuery phenotypeFacetQuery = new FacetQuery();
        phenotypeFacetQuery.setLabel("Has Phenotype");

        String key = FacetBuilderService.PHENOTYPE_FACET_QUERY;
        Map<String, Integer> facetQueryMap = response.getFacetQuery();
        if (facetQueryMap != null && facetQueryMap.containsKey(key))
            phenotypeFacetQuery.setCount(facetQueryMap.get(key));

        phenotypeFacetQuery.setUrl(baseUrl + "&fq=" + PHENOTYPE_STATEMENT.getName() + ":[* TO *]");

        facetQueryList.add(phenotypeFacetQuery);

        return facetQueryList;

    }


    public void sortFacetValues(String fieldName, List<FacetField.Count> values) {

        if (SolrService.isToBeHumanSorted(fieldName)) {
            Collections.sort(values, new FacetValueAlphanumComparator<>());
        }
        if (fieldName.equals("category")) {
            Collections.sort(values, new FacetCategoryComparator<>());
        }
        if (fieldName.equals("stage")) {
            Collections.sort(values, new FacetStageComparator<>());
        }

    }

}
