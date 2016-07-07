package org.zfin.search.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.infrastructure.ActiveData;
import org.zfin.mapping.GenomeLocation;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.OrthologyPresentationBean;
import org.zfin.marker.presentation.SequencePageInfoBean;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.presentation.FishModelDisplay;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.service.OntologyService;
import org.zfin.orthology.presentation.OrthologyPresentationRow;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;
import org.zfin.search.FieldName;
import org.zfin.search.presentation.SearchResult;

import java.lang.reflect.Field;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getLinkageRepository;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;


@Service
public class RelatedDataService {

    public static final String EXPRESSION = "Expression";
    public static final String PHENOTYPE = "Phenotype";
    public static final String SEQUENCES = "Sequences";
    public static final String GBROWSE = "GBrowse";
    public static final String ORTHOLOGY = "Orthology";
    public static final String GENOME_BROWSER = "Genome Browser";
    public static final String DISEASE_MODELS = "Disease Models";
    public static final String CONSTRUCT_MAP = "Construct Map";
    public static final String GENES_WITH_GO = "Genes Annotated with this GO Term";
    public static final String GENES_CAUSING_PHENOTYPE = "Genes With Phenotype";
    public static final String GENES_EXPRESSED = "Genes Expressed in this Structure";
    public static final String RELATED_GENE = "Related zebrafish gene";
    public static final String LAB = "Lab";

    @Autowired
    private SolrService solrService;

    private String[] featureRelatedDataCategories = {Category.PUBLICATION.getName(), Category.FISH.getName(), GENOME_BROWSER};
    private String[] constructRelatedDateCategories = {CONSTRUCT_MAP, Category.MUTANT.getName(), Category.PUBLICATION.getName()};
    private String[] markerCloneRelatedDateCategories = {SEQUENCES, EXPRESSION, GBROWSE, Category.PUBLICATION.getName()};
    private String[] antibodyRelatedDataCategories = {EXPRESSION, Category.PUBLICATION.getName()};
    private String[] anatomyGoRelatedDataCategories = {GENES_WITH_GO, GENES_CAUSING_PHENOTYPE, GENES_EXPRESSED};
    private String[] geneRelatedDataCategories = {EXPRESSION, PHENOTYPE, Category.DISEASE.getName(), Category.MUTANT.getName(), SEQUENCES, GENOME_BROWSER, ORTHOLOGY, Category.PUBLICATION.getName()};
    private String[] pubRelatedDataCategories = {Category.GENE.getName(), EXPRESSION, PHENOTYPE, Category.DISEASE.getName(), Category.MUTANT.getName(), Category.CONSTRUCT.getName(), Category.SEQUENCE_TARGETING_REAGENT.getName(), Category.ANTIBODY.getName(), ORTHOLOGY};
    private String[] diseaseRelatedDataCategories = {RELATED_GENE, DISEASE_MODELS, Category.PUBLICATION.getName()};

    private String entityName;

    public List<String> getRelatedDataLinks(SearchResult result) {

        entityName = result.getName();
        String id = result.getId();
        String category = result.getCategory();

        List<String> links = new ArrayList<>();
        String gBrowseLink;
        if (StringUtils.equals(category, Category.GENE.getName())
                || (StringUtils.equals(category, Category.MUTANT.getName()) && (StringUtils.startsWith(result.getName(), "la0")))) {

            //String gBrowseLink = getGBrowseLink(id);
            if (ActiveData.isValidActiveData(id, ActiveData.Type.TSCRIPT)) {
                List<Marker> markerList = getMarkerRepository().getTranscriptByZdbID(id).getAllRelatedMarker();
                int numberOfTargetGenes = 0;
                for (Marker marker : markerList) {
                    if (marker.getMarkerType().getType().name().equals(ActiveData.Type.GENE.name())) {
                        id = marker.getZdbID();
                        numberOfTargetGenes++;
                    }
                }
                if (numberOfTargetGenes > 1) {
                    return null;
                }
                gBrowseLink = makeLink(GENOME_BROWSER, "/" + ZfinPropertiesEnum.GBROWSE_ZV9_PATH_FROM_ROOT + "?name=" + id);
                links.add(gBrowseLink);
            }


            if (ActiveData.isValidActiveData(id, ActiveData.Type.GENE)) {
                List<MarkerGenomeLocation> genomeLocations = getLinkageRepository().getGenomeLocation(getMarkerRepository().getMarkerByID(id));
                for (MarkerGenomeLocation genomeLocation : genomeLocations) {
                    if (genomeLocation.getSource() == GenomeLocation.Source.ZFIN) {


                        gBrowseLink = makeLink(GENOME_BROWSER, "/" + ZfinPropertiesEnum.GBROWSE_ZV9_PATH_FROM_ROOT + "?name=" + id);
                        links.add(gBrowseLink);
                    }
                }
            }
        }


        if (StringUtils.equals(category, Category.REPORTER_LINE.getName())) {
            links.addAll(getRelatedDataForReporterLine(id));
        }

        if (!(id.contains("EFG"))) {
            if (StringUtils.equals(category, Category.GENE.getName())) {
                Marker marker = getMarkerRepository().getMarkerByID(id);
                if (!ActiveData.isValidActiveData(id, ActiveData.Type.TSCRIPT)) {
                    OrthologyPresentationBean orthologyEvidenceBean = MarkerService.getOrthologyEvidence(marker);
                    if (orthologyEvidenceBean != null) {
                        List<OrthologyPresentationRow> markerList = orthologyEvidenceBean.getOrthologs();
                        if (CollectionUtils.isNotEmpty(markerList)) {
                            links.add(getOrthologyLink(id));
                        }
                    }
                }
                addSequenceLink(links, marker);
            }
        }

            //Special case here, so that the ZFIN orthology pub doesn't get an orthlogy link, because the page will take 10 minutes to load!
            if (StringUtils.equals(category, Category.PUBLICATION.getName())
                    && StringUtils.equals(result.getHasOrthology(), "true")
                    && !StringUtils.equals(result.getId(), "ZDB-PUB-030905-1")) {
                links.add(getOrthoListLink(id));
            }

            links.addAll(getXrefLinks(result));


        if (StringUtils.equals(category, Category.MUTANT.getName())) {
            links = sortLinks(links, featureRelatedDataCategories);
        }
        if (StringUtils.equals(category, Category.MARKER.getName())) {
            if (ActiveData.isValidActiveData(id, ActiveData.Type.BAC) || ActiveData.isValidActiveData(id, ActiveData.Type.PAC)
                    || ActiveData.isValidActiveData(id, ActiveData.Type.CDNA) || ActiveData.isValidActiveData(id, ActiveData.Type.EST)) {
                addSequenceLink(links, id);
            }
            links = sortLinks(links, markerCloneRelatedDateCategories);
        }
        if (StringUtils.equals(category, Category.CONSTRUCT.getName())) {
            Marker marker = getMarkerRepository().getMarkerByID(id);
            if (marker != null) {
                Set<Figure> figures = marker.getFigures();
                if (CollectionUtils.isNotEmpty(figures)) {
                    Figure markerFigure = figures.iterator().next();
                    Image img = markerFigure.getImg();
                    if (img != null) {
                        String link = makeLink(CONSTRUCT_MAP, "/" + img.getZdbID());
                        links.add(link);
                    }
                }
            }

            links = sortLinks(links, constructRelatedDateCategories);
        }
        if (StringUtils.equals(category, Category.GENE.getName())) {
            links = sortLinks(links, geneRelatedDataCategories);
        }
        if (StringUtils.equals(category, Category.ANTIBODY.getName())) {
            links = sortLinks(links, antibodyRelatedDataCategories);
        }
        if (StringUtils.equals(category, Category.PUBLICATION.getName())) {
            links = sortLinks(links, pubRelatedDataCategories);
        }
        if (StringUtils.equals(category, Category.ANATOMY.getName())) {
            getRelatedDataForAnatomyGO(links, id);
            links = sortLinks(links, anatomyGoRelatedDataCategories);
        }
        if (StringUtils.equals(category, Category.DISEASE.getName())) {
            GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByOboID(id);
            List<FishModelDisplay> models = OntologyService.getDiseaseModelsWithFishModel(term);
            if (CollectionUtils.isNotEmpty(models)) {
                links.add(getDiseaseModelsLink(id, models.size()));
            }
            links = sortLinks(links, diseaseRelatedDataCategories);
        }

        return links;
    }

    private void getRelatedDataForAnatomyGO(List<String> links, String id) {

        Ontology ontology = OntologyManager.getInstance().getOntologyForTerm(id);
        if (Ontology.isGoOntology(ontology)) {
            getGoAnnotationData(links, id, FieldName.getFieldName(ontology));
            createExpressedGenesData(links, id, FieldName.EXPRESSED_IN_TF);
            createAffectedPhenotypeData(links, id, FieldName.CELLULAR_COMPONENT_TF);
            createAffectedPhenotypeData(links, id, FieldName.getAffectedFieldName(ontology));
        }
        createAffectedPhenotypeData(links, id, FieldName.getAffectedFieldName(ontology));
        createExpressedGenesData(links, id, FieldName.ANATOMY_TF);
    }

    private List<String> getRelatedDataForReporterLine(String id) {
        List<String> links = new ArrayList<>();
        
        String expressionLink = getCategoryLink(Category.EXPRESSIONS, Category.EXPRESSIONS.getName(), FieldName.FISH, FieldName.EXPERIMENTAL_CONDITIONS.getName() + ":\"standard or control\"");
        if (StringUtils.isNotEmpty(expressionLink)) {
            links.add(expressionLink);
        }

        String phenotypeLink = getCategoryLink(Category.PHENOTYPE, Category.PHENOTYPE.getName(), FieldName.FISH, FieldName.EXPERIMENTAL_CONDITIONS.getName() + ":\"standard or control\"");
        if (StringUtils.isNotEmpty(phenotypeLink)) {
            links.add(phenotypeLink);
        }

        return links;
    }

    private String getCategoryLink(Category category, String label, FieldName fieldName, String... filterQueries) {

        QueryResponse response = getQueryResponse(fieldName, category, filterQueries);
        FacetField facetField = response.getFacetField(FieldName.CATEGORY.getName());

        String link = null;

        if (facetField != null && facetField.getValues() != null) {
            for (FacetField.Count count : facetField.getValues()) {
                Properties properties = new Properties();
                properties.put(fieldName.getName(), entityName);
                link = createHyperLink("", FieldName.CATEGORY.getName(), count.getName(), count.getCount(), label, false, properties, filterQueries).toString();
            }
        }

        return link;

    }

    private void createAffectedPhenotypeData(List<String> links, String id, FieldName fieldName) {
        QueryResponse response = getQueryResponse(fieldName, Category.GENE);

        FacetField category = response.getFacetField("category");
        if (category != null && category.getValues() != null) {
            for (FacetField.Count count : category.getValues()) {
                if (count.getName().equals(Category.GENE.getName())) {
                    Properties properties = new Properties();
                    properties.put(fieldName.getName(), entityName);
                    links.add(createHyperLink(id, category.getName(), count.getName(), count.getCount(), GENES_CAUSING_PHENOTYPE, false, properties).toString());
                }
            }
        }
    }

    private void createExpressedGenesData(List<String> links, String id, FieldName fieldName) {
        QueryResponse response = getQueryResponse(fieldName, Category.GENE);

        FacetField category = response.getFacetField("category");
        if (category != null && category.getValues() != null) {
            for (FacetField.Count count : category.getValues()) {
                if (count.getName().equals(Category.GENE.getName())) {
                    Properties properties = new Properties();
                    properties.put(fieldName.getName(), entityName);
                    links.add(createHyperLink(id, category.getName(), count.getName(), count.getCount(), GENES_EXPRESSED, false, properties).toString());
                }
            }
        }
    }

    private void getGoAnnotationData(List<String> links, String id, FieldName field) {
        QueryResponse response = getQueryResponse(field, Category.GENE);

        FacetField category = response.getFacetField("category");
        if (category != null && category.getValues() != null) {
            for (FacetField.Count count : category.getValues()) {
                if (count.getName().equals(Category.GENE.getName())) {
                    Properties properties = new Properties();
                    properties.put(field.getName(), entityName);
                    links.add(createHyperLink(id, category.getName(), count.getName(), count.getCount(), GENES_WITH_GO, false, properties).toString());
                }
            }
        }
    }

    private QueryResponse getQueryResponse(FieldName field, Category category) {
        return getQueryResponse(field, category, (String[]) null);
    }

    private QueryResponse getQueryResponse(FieldName field, Category category, String... filterQueries) {

        SolrClient server = SolrService.getSolrClient();
        SolrQuery query = new SolrQuery();
        //look for the term name in an OR over multiple fields
        query.addFilterQuery(field.getName() + ":\"" + entityName + "\"");
        query.addFilterQuery(FieldName.CATEGORY.getName() + ":\"" + category.getName() + "\"");
        query.setRows(0);
        query.setHighlight(false);
        query.setFacet(true);
        query.setFacetLimit(100);
        query.addFacetField(FieldName.CATEGORY.getName());
        if (filterQueries != null && filterQueries.length > 0) {
            for (String filterQuery : filterQueries) {
                query.addFilterQuery(filterQuery);
            }

        }

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }
        return response;
    }

    Logger logger = Logger.getLogger(RelatedDataService.class);

    private List<String> sortLinks(List<String> links, String[] relatedDataFeatureCategories) {
        List<String> returnList = new ArrayList<>(links.size());
        Map<Integer, String> linkMap = new TreeMap<>();
        for (String link : links) {
            if (link == null) {
                continue;
            }
            int index = 0;
            for (String relatedDatum : relatedDataFeatureCategories) {
                if (link.contains(relatedDatum)) {
                    linkMap.put(index, link);
                }
                index++;
            }
        }
        for (Integer linkID : linkMap.keySet()) {
            returnList.add(linkMap.get(linkID));
        }
        return returnList;
    }


    public List<String> getXrefLinks(SearchResult result) {
        List<String> links = new ArrayList<>();

        QueryResponse response = solrService.getRelatedDataResponse(result.getId());

        FacetField facetField = response.getFacetField("category");

        if (facetField != null && facetField.getValues() != null) {
            for (FacetField.Count related : facetField.getValues()) {
                String linkText = LinkDisplay.lookup(result.getCategory(), result.getType(), related.getName());
                StringBuilder link = createHyperLink(result.getId(), facetField.getName(), related.getName(),
                        related.getCount(), linkText, true, null);
                links.add(link.toString());
            }
        }
        return links;
    }

    protected StringBuilder createHyperLink(String id, String facetFieldName, String categoryName,
                                            long categoryCount, String hyperlinkName, boolean isXref,
                                            Properties properties) {
        return createHyperLink(id, facetFieldName, categoryName, categoryCount, hyperlinkName, isXref, properties, (String[]) null);
    }

    protected StringBuilder createHyperLink(String id, String facetFieldName, String categoryName,
                                            long categoryCount, String hyperlinkName, boolean isXref,
                                            Properties properties, String... filterQueries) {
        //this is an unpleasant hack, I need to stuff the expression popup link in here, so it's a little hijack...
        if (id.startsWith("ZDB-GENE") && StringUtils.equals(categoryName, "Expression")) {
            return getGeneExpressionPopupLink(id, categoryCount);
        } else if (id.startsWith("ZDB-GENE") && StringUtils.equals(categoryName, "Phenotype")) {
            return getGenePhenotypePopupLink(id, categoryCount);

        } else if (id.startsWith("ZDB-FIG") && StringUtils.equals(categoryName, "Publication")) {
            StringBuilder link = new StringBuilder();
            return link.append("");

        } else {
            if (StringUtils.isEmpty(hyperlinkName)) {
                hyperlinkName = categoryName;
            }
            StringBuilder link = new StringBuilder();
            link.append("<a href=\"/search?q=");
            String fq = SolrService.encode(facetFieldName + ":\"" + categoryName + "\"");
            link.append("&fq=");
            link.append(fq);
            if (isXref) {
                link.append("&fq=xref%3A%22");
                link.append(id);
            } else {
                //org.zfin.search.FieldName cannot be cast to java.lang.String
                link.append("&fq=").append(properties.propertyNames().nextElement()).append("%3A%22");
                link.append(properties.elements().nextElement());
            }
            link.append("%22");

            if (filterQueries != null && filterQueries.length > 0) {
                for (String filterQuery : filterQueries) {
                    link.append("&fq=" + SolrService.encode(filterQuery));
                }
            }


            link.append("\">");
            link.append(hyperlinkName.replace("\"", ""));
            link.append(" (");
            link.append(categoryCount);

            link.append(") </a>");
            return link;
        }
    }

    private StringBuilder getGenePhenotypePopupLink(String id, Long categoryCount) {
        String domID = id + "-phenotype-modal";

        StringBuilder link = new StringBuilder();
        link.append("<a class =\"related-data-modal\" href=\"/action/quicksearch/phenotype/");
        link.append(id);
        link.append("\" data-toggle=\"modal\" data-target=\"#");
        link.append(domID);
        link.append("\">Phenotype (");
        link.append(categoryCount.toString());
        link.append(") <img class=\"modal-icon\" src=\"/images/popup-link-icon.png\"/></a>");

        return link;
    }

    public StringBuilder getGeneExpressionPopupLink(String id, Long count) {
        //<a href="/action/quicksearch/gene-expression/${result.id}" data-toggle="modal" data-target="#${expressionModalDomID}">Expression</a>

        String domID = id + "-gene-expression-modal";

        StringBuilder link = new StringBuilder();
        link.append("<a class =\"related-data-modal\" href=\"/action/quicksearch/gene-expression/");
        link.append(id);
        link.append("\" data-toggle=\"modal\" data-target=\"#");
        link.append(domID);
        link.append("\">Expression (");
        link.append(count.toString());
        link.append(") <img class=\"modal-icon\" src=\"/images/popup-link-icon.png\"/></a>");

        return link;
    }

    private void addSequenceLink(Collection<String> links, String markerId) {
        addSequenceLink(links, getMarkerRepository().getMarkerByID(markerId));
    }

    private void addSequenceLink(Collection<String> links, Marker marker) {
        if (marker == null) {
            return;
        }

        SequencePageInfoBean sequenceInfo = MarkerService.getSequenceInfoFull(marker);
        if (CollectionUtils.isNotEmpty(sequenceInfo.getDbLinks()) || MapUtils.isNotEmpty(sequenceInfo.getRelatedMarkerDBLinks())) {
            links.add(getSequencesLink(marker.getZdbID()));
        }
    }

    private String getSequencesLink(String id) {
        return makeLink(SEQUENCES, "/action/marker/sequence/view/" + id);
    }

    private String getOrthologyLink(String id) {
        return makeLink(ORTHOLOGY, "/" + id, "orthology");
    }

    private String getOrthoListLink(String id) {
        return makeLink(ORTHOLOGY, "/action/publication/" + id + "/orthology-list");
    }

    private String getDiseaseModelsLink(String id, int count) {
        return makeLink(DISEASE_MODELS + " (" + count + ")", "/" + id, "fish_models");
    }

    private String makeLink(String text, String url) {
        return String.format("<a href=\"%s\">%s</a>", url, text);
    }

    private String makeLink(String text, String url, String anchor) {
        return makeLink(text, url + "#" + anchor);
    }

    /**
     * By default xref related data links will use the category name as the text of the link.
     * If you don't want that behavior, add a value here to override it. The enum value itself
     * doesn't really matter, this just funtions as a static look-up table based on the result's
     * category and type and the related data category.
     */
    private enum LinkDisplay {
        PERSON_LAB(Category.COMMUNITY, "Person", Category.COMMUNITY, LAB),
        DISEASE_RELATED_GENE(Category.DISEASE, null, Category.GENE, RELATED_GENE);

        private Category resultCategory;
        private String resultType;
        private Category relatedCategory;
        private String linkDisplay;

        LinkDisplay(Category resultCategory, String resultType, Category relatedCategory, String linkDisplay) {
            this.resultCategory = resultCategory;
            this.resultType = resultType;
            this.relatedCategory = relatedCategory;
            this.linkDisplay = linkDisplay;
        }

        public static String lookup(String resultCategory, String resultType, String relatedCategory) {
            for (LinkDisplay linkDisplay : values()) {
                if (linkDisplay.resultCategory.getName().equals(resultCategory) &&
                        (linkDisplay.resultType == null || linkDisplay.resultType.equals(resultType)) &&
                        linkDisplay.relatedCategory.getName().equals(relatedCategory)) {
                    return linkDisplay.linkDisplay;
                }
            }
            return relatedCategory;
        }
    }

}
