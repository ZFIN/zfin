package org.zfin.search.service;


import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.zfin.search.*;
import org.zfin.search.presentation.Facet;
import org.zfin.search.presentation.FacetGroup;
import org.zfin.search.presentation.FacetQuery;
import org.zfin.search.presentation.FacetValue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static org.zfin.search.FieldName.*;


public class FacetBuilderService {

    public static Logger logger = Logger.getLogger(FacetBuilderService.class);

    public static String PHENOTYPE_FACET_QUERY = "phenotype_statement:[* TO *]";

    private final QueryResponse response;
    private final String baseUrl;
    private final Map<String, Boolean> filterQuerySelectionMap;

    public FacetBuilderService(QueryResponse response, String baseUrl, Map<String, Boolean> filterQuerySelectionMap) {
        this.response = response;
        this.baseUrl = baseUrl;
        this.filterQuerySelectionMap = filterQuerySelectionMap;
    }

    public List<FacetGroup> buildFacetGroup(String category) {

        if (StringUtils.isEmpty(category) || StringUtils.equals(category, "Any")) {
            return buildCategoryFacetGroup();
        } else if (StringUtils.equals(category, Category.GENE.getName())) {
            return buildGeneFacetGroup();
        } else if (StringUtils.equals(category, Category.MUTANT.getName())) {
            return buildFeatureFacetGroup();
        } else if (StringUtils.equals(category, Category.REPORTER_LINE.getName())) {
            return buildReporterLineFacetGroup();
        } else if (StringUtils.equals(category, Category.FISH.getName())) {
            return buildFishFacetGroup();
        } else if (StringUtils.equals(category, Category.FIGURE.getName())) {
            return buildFigureFacetGroup();
        } else if (StringUtils.equals(category, Category.EXPRESSIONS.getName())) {
            return buildExpressionFacetGroup();
        } else if (StringUtils.equals(category, Category.PHENOTYPE.getName())) {
            return buildPhenotypeFacetGroup();
        } else if (StringUtils.equals(category, Category.PUBLICATION.getName())) {
            return buildPublicationFacetGroup();
        } else if (StringUtils.equals(category, Category.DISEASE.getName())) {
            return buildDiseaseFacetGroup();
        } else if (StringUtils.equals(category, Category.ANATOMY.getName())) {
            return buildAnatomyFacetGroup();
        } else if (StringUtils.equals(category, Category.JOURNAL.getName())) {
            return buildJournalFacetGroup();
        } else {
            return buildFlatFacetGroup(category);
        }
    }

    /* generically build a "one-facet-per-group" section for any category in the map */
    private List<FacetGroup> buildFlatFacetGroup(String category) {

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
            facetGroup.addFacet(buildFacet(fieldName, true));
            facetGroups.add(facetGroup);
            count++;
        }

        return facetGroups;
    }


    private List<FacetGroup> buildCategoryFacetGroup() {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup category = new FacetGroup("Category", true);
        Facet categoryFacet = buildFacet("category", true);
        categoryFacet.setShowIncludeExcludeIcons(false);
        categoryFacet.setAlwaysShowAllFacets(true);
        categoryFacet.setShowAutocompleteBox(false);
        category.addFacet(categoryFacet);
        facetGroups.add(category);


        return facetGroups;
    }

    private List<FacetGroup> buildAnatomyFacetGroup() {
        List<FacetGroup> facetGroups = new ArrayList<>();
        facetGroups.add(buildSingleFacetGroup("Ontology", FieldName.ONTOLOGY.getName(), true));
        FacetGroup statusGroup = buildSingleFacetGroup("Term Status", FieldName.TERM_STATUS.getName(), true);
        statusGroup.setRootOnly(true);
        facetGroups.add(statusGroup);
        return facetGroups;
    }

    private List<FacetGroup> buildDiseaseFacetGroup() {
        List<FacetGroup> facetGroups = new ArrayList<>();

        facetGroups.add(buildSingleFacetGroup("Gene", FieldName.GENE.getName(), true));

        FacetGroup diseaseModel = new FacetGroup("Disease Model", true);
        diseaseModel.addFacet(buildFacet(FieldName.FISH.getName(), true));
        diseaseModel.addFacet(buildFacet(FieldName.CONDITIONS.getName(), true));
        facetGroups.add(diseaseModel);

        return facetGroups;
    }


    /*  This is just a temporary method, should be replaced by further configuration */
    private List<FacetGroup> buildGeneFacetGroup() {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup type = new FacetGroup("Type", true);
        type.addFacet(buildFacet("type", true));
        facetGroups.add(type);

        FacetGroup expressedIn = new FacetGroup("Expression", true);
        expressedIn.addFacet(buildFacet(ANATOMY_TF.getName(), true));
        expressedIn.addFacet(buildFacet(FieldName.STAGE.getName(), false));
        facetGroups.add(expressedIn);


        facetGroups.add(buildPhenotypeBlock());


        FacetGroup diseaseModel = new FacetGroup("Human Disease");
        diseaseModel.addFacet(buildFacet(DISEASE.getName(), true));
        facetGroups.add(diseaseModel);


        FacetGroup geneOntology = new FacetGroup("Gene Ontology");
        geneOntology.addFacet(buildFacet(BIOLOGICAL_PROCESS_TF.getName(), true));
        geneOntology.addFacet(buildFacet(MOLECULAR_FUNCTION_TF.getName(), true));
        geneOntology.addFacet(buildFacet(CELLULAR_COMPONENT_TF.getName(), true));
        facetGroups.add(geneOntology);

        FacetGroup location = new FacetGroup("Location", false);
        Facet locationFacet = buildFacet(CHROMOSOME.getName(), true);
        locationFacet.setAlwaysShowAllFacets(true);
        location.addFacet(locationFacet);
        facetGroups.add(location);

        return facetGroups;

    }

    private List<FacetGroup> buildExpressionFacetGroup() {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup expressedGene = new FacetGroup("Expressed Gene", true);

        expressedGene.addFacet(buildFacet(FieldName.ZEBRAFISH_GENE.getName(),
                Category.EXPRESSIONS.getFacetQueriesForField(FieldName.ZEBRAFISH_GENE),
                true));
        expressedGene.addFacet(buildFacet(FieldName.REPORTER_GENE.getName(),
                Category.EXPRESSIONS.getFacetQueriesForField(FieldName.REPORTER_GENE),
                false));

        facetGroups.add(expressedGene);

        facetGroups.add(buildSingleFacetGroup("Expressed In Anatomy", EXPRESSION_ANATOMY_TF.getName(), true));
        facetGroups.add(buildSingleFacetGroup("Stage", "stage", true));
        facetGroups.add(buildSingleFacetGroup("Has Image", "has_image", true));
        FacetGroup wildtypeGroup = buildSingleFacetGroup("Is Wildtype and Clean", "is_wildtype", false);

        wildtypeGroup.setRootOnly(true);
        facetGroups.add(wildtypeGroup);
        facetGroups.add(buildSingleFacetGroup("Assay", "assay", false));


        FacetGroup genotype = buildSingleFacetGroup("Genotype", FieldName.GENOTYPE_FULL_NAME.getName(),
                Category.EXPRESSIONS.getFacetQueriesForField(FieldName.GENOTYPE),
                false);

        facetGroups.add(genotype);

        facetGroups.add(buildSingleFacetGroup("Sequence Targeting Reagent (STR)", "sequence_targeting_reagent", false));
        facetGroups.add(buildSingleFacetGroup("Conditions", FieldName.CONDITIONS.getName(), false));


        return facetGroups;
    }


    private List<FacetGroup> buildFeatureFacetGroup() {
        List<FacetGroup> facetGroups = new ArrayList<>();

        facetGroups.add(buildSingleFacetGroup("Type", "type", true));
        facetGroups.add(buildSingleFacetGroup("Affected Genomic Region", "affected_gene", true));
        facetGroups.add(buildPhenotypeBlock());
        //todo: need this in the index still?
        facetGroups.add(buildSingleFacetGroup("Consequence", "rna_consequence", false));
        facetGroups.add(buildSingleFacetGroup("Mutagen", "mutagen", false));
        facetGroups.add(buildSingleFacetGroup("Source", "source", false));
        facetGroups.add(buildSingleFacetGroup("Lab of Origin", "lab_of_origin", false));
        facetGroups.add(buildSingleFacetGroup("Institution", "institution", false));
        //screen used to be here, removed as a result of case 11323
        //facetGroups.add(buildSingleFacetGroup("Screen", "screen", false,  fqMap));

        return facetGroups;
    }

    private List<FacetGroup> buildReporterLineFacetGroup() {
        List<FacetGroup> facetGroups = new ArrayList<>();

        facetGroups.add(buildSingleFacetGroup("Reporter Gene", REPORTER_GENE.getName(), true));

        FacetGroup expressionAnatomy = new FacetGroup("Expression Anatomy", true);
        expressionAnatomy.addFacet(buildFacet(EXPRESSION_ANATOMY_TF.getName(), true));
        facetGroups.add(expressionAnatomy);


        facetGroups.add(buildSingleFacetGroup("Regulatory Region", REGULATORY_REGION.getName(), true));
        facetGroups.add(buildSingleFacetGroup("Stage", STAGE.getName(), true));


        return facetGroups;
    }

    private List<FacetGroup> buildFishFacetGroup() {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup affectedGene = new FacetGroup("Affected Genomic Region", true);
        affectedGene.addFacet(buildFacet(AFFECTED_GENE.getName(), true));
        facetGroups.add(affectedGene);

        FacetGroup modelOf = new FacetGroup("Is Model Of", true);
        modelOf.addFacet(buildFacet(DISEASE.getName(), true));
        facetGroups.add(modelOf);

        FacetGroup expressionAnatomy = new FacetGroup("Expression Anatomy", true);
        expressionAnatomy.addFacet(buildFacet(EXPRESSION_ANATOMY_TF.getName(), true));
        facetGroups.add(expressionAnatomy);

        facetGroups.add(buildPhenotypeBlock());

        FacetGroup str = new FacetGroup("Sequence Targeting Reagent (STR)");
        str.addFacet(buildFacet(SEQUENCE_TARGETING_REAGENT.getName(), true));
        facetGroups.add(str);

        FacetGroup construct = new FacetGroup("Construct");
        construct.addFacet(buildFacet(CONSTRUCT.getName(), true));
        facetGroups.add(construct);

        FacetGroup sequenceAlteration = new FacetGroup("Mutation / Tg");
        sequenceAlteration.addFacet(buildFacet(SEQUENCE_ALTERATION.getName(), true));
        facetGroups.add(sequenceAlteration);

        FacetGroup background = new FacetGroup("Background");
        background.addFacet(buildFacet(BACKGROUND.getName(), true));
        facetGroups.add(background);

        return facetGroups;

    }


    private List<FacetGroup> buildFigureFacetGroup() {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup expressionAnatomy = new FacetGroup("Expression Anatomy", true);
        expressionAnatomy.addFacet(buildFacet(EXPRESSION_ANATOMY_TF.getName(), true));
        facetGroups.add(expressionAnatomy);

        FacetGroup expressedGene = new FacetGroup("Expressed Gene", true);
        expressedGene.addFacet(buildFacet("zebrafish_gene", true));
        expressedGene.addFacet(buildFacet("reporter_gene", true));
        facetGroups.add(expressedGene);

        facetGroups.add(buildPhenotypeBlock());

        FacetGroup construct = new FacetGroup("Construct");
        construct.addFacet(buildFacet(CONSTRUCT.getName(), true));
        facetGroups.add(construct);


        FacetGroup author = new FacetGroup("Registered Author");
        author.addFacet(buildFacet("registered_author", true));
        facetGroups.add(author);

        facetGroups.add(buildSingleFacetGroup("Has Image", "has_image", true));

        return facetGroups;
    }

    private List<FacetGroup> buildPhenotypeFacetGroup() {
        List<FacetGroup> facetGroups = new ArrayList<>();

        facetGroups.add(buildSingleFacetGroup("Phenotypic Gene", GENE.getName(), true));
        facetGroups.add(buildSingleFacetGroup("Phenotype Statement", PHENOTYPE_STATEMENT.getName(), true));

        facetGroups.add(buildSingleFacetGroup("Stage", STAGE.getName(), true));

        //FacetGroup phenotype = new FacetGroup("Manifests In", false);
        FacetGroup phenotype = new FacetGroup("Manifests In", true);
        phenotype.addFacet(buildFacet(ANATOMY_TF.getName(), true));
        phenotype.addFacet(buildFacet(BIOLOGICAL_PROCESS_TF.getName(), false));
        phenotype.addFacet(buildFacet(MOLECULAR_FUNCTION_TF.getName(), false));
        phenotype.addFacet(buildFacet(CELLULAR_COMPONENT_TF.getName(), false));
        phenotype.addFacet(buildFacet(MISEXPRESSED_GENE.getName(), false));
        facetGroups.add(phenotype);
        //facetGroups.add(buildSingleFacetGroup("Genes With Altered Expression","genes_with_altered_expression",false,  filterQuerySelectionMap));
        facetGroups.add(buildSingleFacetGroup("Sequence Targeting Reagent (STR)", SEQUENCE_TARGETING_REAGENT.getName(), false));
        facetGroups.add(buildSingleFacetGroup("Is Monogenic", "is_monogenic", false));
        facetGroups.add(buildSingleFacetGroup("Conditions", FieldName.CONDITIONS.getName(), false));

        facetGroups.add(buildSingleFacetGroup("Has Image", "has_image", false));
        //todo: stage, conditions, author, data_source

        return facetGroups;
    }


    private List<FacetGroup> buildPublicationFacetGroup() {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup curation = new FacetGroup("Curation", true);
        curation.setRootOnly(true);
        curation.addFacet(buildFacet("topic", false));
        curation.addFacet(buildFacet("curation_status", false));
        curation.addFacet(buildFacet("publication_status", false));
        curation.addFacet(buildFacet("location", false));
        curation.addFacet(buildFacet("owner", false));

        facetGroups.add(curation);

        facetGroups.add(buildSingleFacetGroup("Gene", "gene", true));
        facetGroups.add(buildSingleFacetGroup("Mutation / Tg", "sequence_alteration", true));
        facetGroups.add(buildSingleFacetGroup("Human Disease", "disease", true));
        facetGroups.add(buildSingleFacetGroup("Registered Author", "registered_author", true));
        facetGroups.add(buildSingleFacetGroup("Journal", "journal", false));
        facetGroups.add(buildSingleFacetGroup("Keyword", "keyword", false));
        facetGroups.add(buildSingleFacetGroup("MeSH Term", MESH_TERM.getName(), false));
        facetGroups.add(buildSingleFacetGroup("Publication Type", "publication_type", false));

        FacetGroup publishedDateGroup = new FacetGroup("Publication Date", false);

        publishedDateGroup.setFacetQueries(buildDateFacetQueries(filterQuerySelectionMap));
        facetGroups.add(publishedDateGroup);
        return facetGroups;
    }

    private List<FacetGroup> buildJournalFacetGroup() {
        List<FacetGroup> facetGroups = new ArrayList<>();

        FacetGroup accessionGroup = new FacetGroup("Accession", true);
        Facet accession = buildFacet(FieldName.RELATED_ACCESSION.getName(), true);
        accession.setAlwaysShowAllFacets(true);
        accessionGroup.addFacet(accession);
        facetGroups.add(accessionGroup);
        return facetGroups;
    }

    private List<FacetQuery> buildFacetQueries(List<FacetQueryEnum> facetQueryEnumList) {

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
                        facetQuery.setUrl(baseUrl + "&fq=" + URLEncoder.encode(facetQueryEnum.getQuery(), "UTF-8"));
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

    private List<FacetQuery> buildDateFacetQueries(Map<String, Boolean> filterQuerySelectionMap) {

        List<FacetQuery> unselectedFacetQueries = new ArrayList<>();
        List<FacetQuery> selectedFacetQueries = new ArrayList<>();

        Map<String, String> publicationDateQueries = getPublicationDateQueries();
        for (String year : publicationDateQueries.keySet()) {
            String query = publicationDateQueries.get(year);
            Integer count = response.getFacetQuery().get(query);
            if (count != null && count > 0) {
                FacetQuery facetQuery = new FacetQuery();
                facetQuery.setLabel(year);
                facetQuery.setCount(count);
                //the map tells us if it's a selected value or not
                if (filterQuerySelectionMap.containsKey(query) && filterQuerySelectionMap.get(query))
                    facetQuery.setSelected(true);
                else
                    facetQuery.setSelected(false);

                if (facetQuery.getSelected()) {
                    facetQuery.setUrl(SolrService.getBreadBoxUrl(query, baseUrl));
                    selectedFacetQueries.add(facetQuery);
                } else {
                    try {
                        facetQuery.setUrl(baseUrl + "&fq=" + URLEncoder.encode(query, "UTF-8"));
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

    private static final String DATE_QUERY_TEMPLATE = "date:[{START} TO {END}}";
    private static final String DATE_TIME = "{YEAR}-01-01T00:00:00Z";

    public static Map<String, String> getPublicationDateQueries() {
        Map<String, String> queryMap = new LinkedHashMap<>();
        queryMap.put("Last 30 Days", "date:[NOW/DAY-30DAYS TO NOW/DAY]");
        queryMap.put("Last 90 Days", "date:[NOW/DAY-90DAYS TO NOW/DAY]");
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        // going back further in time will result in an error
        // as the HTTP header size then will be exceeded (8kB) from Jetty.
        for (int year = currentYear; year > 1950; year--) {
            String startTime = DATE_TIME.replace("{YEAR}", year + "");
            String endTime = DATE_TIME.replace("{YEAR}", (year + 1) + "");
            String query = DATE_QUERY_TEMPLATE.replace("{START}", startTime);
            query = query.replace("{END}", endTime);
            queryMap.put("" + year, query);
        }
        return queryMap;
    }

    private FacetGroup buildSingleFacetGroup(String label, String fieldName,
                                            boolean openByDefault) {
        return buildSingleFacetGroup(label, fieldName, new ArrayList<FacetQueryEnum>(), openByDefault);
    }

    private FacetGroup buildSingleFacetGroup(String label, String fieldName, List<FacetQueryEnum> facetQueryEnumList,
                                            boolean openByDefault) {
        FacetGroup group = new FacetGroup(label, openByDefault);
        group.addFacet(buildFacet(fieldName, true));
        group.setFacetQueries(buildFacetQueries(facetQueryEnumList));
        return group;
    }

    private FacetGroup buildPhenotypeBlock() {
        FacetGroup phenotype = new FacetGroup("Phenotype", true);
        phenotype.addFacet(buildFacet(AFFECTED_ANATOMY_TF.getName(), true));
        phenotype.addFacet(buildFacet(AFFECTED_BIOLOGICAL_PROCESS_TF.getName(), false));
        phenotype.addFacet(buildFacet(AFFECTED_MOLECULAR_FUNCTION_TF.getName(), false));
        phenotype.addFacet(buildFacet(AFFECTED_CELLULAR_COMPONENT_TF.getName(), false));
        phenotype.addFacet(buildFacet(PHENOTYPE_STATEMENT.getName(), false));
        phenotype.addFacet(buildFacet(MISEXPRESSED_GENE.getName(), false));
        return phenotype;
    }


    private Facet buildFacet(String fieldName,
                            boolean openByDefault) {
        return buildFacet(fieldName, new ArrayList<FacetQueryEnum>(), openByDefault);
    }

    private Facet buildFacet(String fieldName,
                            List<FacetQueryEnum> facetQueryEnumList,
                            boolean openByDefault) {

        FacetField facetField = response.getFacetField(fieldName);

        if (facetField == null)
            return null;

        Facet facet = new Facet(facetField);

        facet.setFacetQueries(buildFacetQueries(facetQueryEnumList));

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
                String value = nameValuePair.getValue().replace("\"", "");
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

    private FacetValue buildFacetValue(FacetField facetField, FacetField.Count count, boolean selected, String baseUrl) {
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
    public List<FacetQuery> getFacetQueries() {

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
        } else if (fieldName.equals(FieldName.CATEGORY.getName())) {
            Collections.sort(values, new FacetCategoryComparator<>());
        } else if (fieldName.equals(FieldName.STAGE.getName())) {
            Collections.sort(values, new FacetStageComparator<>());
        }
    }

}
