package org.zfin.search.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.Marker;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.orthology.repository.OrthologyPresentationRow;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.search.Category;
import org.zfin.search.FieldName;
import org.zfin.search.presentation.SearchResult;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getOrthologyRepository;


@Service
public class RelatedDataService {

    public static final String EXPRESSION = "Expression";
    public static final String PHENOTYPE = "Phenotype";
    public static final String SEQUENCES = "Sequences";
    public static final String GBROWSE = "GBrowse";
    public static final String ORTHOLOGY = "Orthology";
    public static final String GENOME_BROWSER = "Genome Browser";

    @Autowired
    private SolrService solrService;

    private String[] featureRelatedDataCategories = {Category.PUBLICATION.getName(), Category.FISH.getName(), GENOME_BROWSER};
    private String[] constructRelatedDateCategories = {"Construct Map", Category.MUTANT.getName(), Category.PUBLICATION.getName()};
    private String[] markerCloneRelatedDateCategories = {SEQUENCES, EXPRESSION, GBROWSE, Category.PUBLICATION.getName()};
    private String[] antibodyRelatedDataCategories = {EXPRESSION, Category.PUBLICATION.getName()};
    private String[] anatomyGoRelatedDataCategories = {GENES_WITH_GO, GENES_CAUSING_PHENOTYPE, GENES_EXPRESSED};
    private String[] geneRelatedDataCategories = {EXPRESSION, PHENOTYPE, Category.DISEASE.getName(), Category.MUTANT.getName(), SEQUENCES, GENOME_BROWSER, ORTHOLOGY, Category.PUBLICATION.getName()};
    private String[] pubRelatedDataCategories = {Category.GENE.getName(), EXPRESSION,PHENOTYPE, Category.DISEASE.getName(), Category.MUTANT.getName(), Category.CONSTRUCT.getName(),Category.SEQUENCE_TARGETING_REAGENT.getName(),Category.ANTIBODY.getName(),ORTHOLOGY};
    public static final String GENES_WITH_GO = "Genes Annotated with this GO Term";
    public static final String GENES_CAUSING_PHENOTYPE = "Genes Causing Phenotype";
    public static final String GENES_EXPRESSED = "Genes Expressed in this Structure";


    private String entityName;

    public List<String> getRelatedDataLinks(SearchResult result) {

        entityName = result.getName();
        String id = result.getId();
        String category = result.getCategory();

        List<String> links = new ArrayList<>();

        if (StringUtils.equals(category, Category.GENE.getName())
                || (StringUtils.equals(category, Category.MUTANT.getName()) && (StringUtils.startsWith(result.getName(), "la0")))) {

            String gBrowseLink = getGBrowseLink(id);

            if (!(id.contains("EFG"))) {
                if (!(entityName.contains("WITHDRAWN")))
                    if (gBrowseLink != null)
                        links.add(gBrowseLink);
            }
        }



        if (!(id.contains("EFG"))) {

            if (StringUtils.equals(category, Category.GENE.getName())) {
                if (!ActiveData.isValidActiveData(id, ActiveData.Type.TSCRIPT)) {
                    List<OrthologyPresentationRow> markerList=getOrthologyRepository().getOrthologyForGene(getMarkerRepository().getGeneByID(id));
                    if (CollectionUtils.isNotEmpty(markerList)) {
                        links.add(getOrthologyLink(id));
                    }
                }
                String link = "<a href=/action/marker/sequence/view/" + id + ">" + SEQUENCES + "</a>";
                links.add(link);
            }
        }



        //Special case here, so that the ZFIN orthology pub doesn't get an orthlogy link, because the page will take 10 minutes to load!
        if (StringUtils.equals(category, Category.PUBLICATION.getName())
                && StringUtils.equals(result.getHasOrthology(), "true")
                && !StringUtils.equals(result.getId(),"ZDB-PUB-030905-1")) {
            links.add(getOrtholistLink(id));
        }


        links.addAll(getXrefLinks(id));


        if (StringUtils.equals(category, Category.MUTANT.getName()))
            links = sortLinks(links, featureRelatedDataCategories);
        if (StringUtils.equals(category, Category.MARKER.getName())) {
            if (ActiveData.isValidActiveData(id, ActiveData.Type.BAC) || ActiveData.isValidActiveData(id, ActiveData.Type.PAC)
                    || ActiveData.isValidActiveData(id, ActiveData.Type.CDNA) || ActiveData.isValidActiveData(id, ActiveData.Type.EST)) {
                String link = "<a href=/action/marker/sequence/view/" + id + ">" + SEQUENCES + "</a>";
                links.add(link);
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
                        String link = "<a href=/" + img.getZdbID() + ">Construct Map</a>";
                        links.add(link);
                    }
                }
            }

            links = sortLinks(links, constructRelatedDateCategories);
        }
        if (StringUtils.equals(category, Category.GENE.getName()))
            links = sortLinks(links, geneRelatedDataCategories);
        if (StringUtils.equals(category, Category.ANTIBODY.getName()))
            links = sortLinks(links, antibodyRelatedDataCategories);
        if (StringUtils.equals(category, Category.PUBLICATION.getName()))
             links = sortLinks(links, pubRelatedDataCategories);
        if (StringUtils.equals(category, Category.ANATOMY.getName())) {
            getRelatedDataForAnatomyGO(links, id);
            links = sortLinks(links, anatomyGoRelatedDataCategories);
        }
        return links;
    }

    private void getRelatedDataForAnatomyGO(List<String> links, String id) {

        Ontology ontology = OntologyManager.getInstance().getOntologyForTerm(id);
        if (Ontology.isGoOntology(ontology))
            getGoAnnotationData(links, id, FieldName.getFieldName(ontology));
        createAffectedPhenotypeData(links, id, FieldName.getAffectedFieldName(ontology));
        createExpressedGenesData(links, id, FieldName.EXPRESSED_IN_TF);
    }

    private void createAffectedPhenotypeData(List<String> links, String id, String fieldName) {
        QueryResponse response = getQueryResponse(fieldName);

        FacetField category = response.getFacetField("category");
        if (category != null && category.getValues() != null)
            for (FacetField.Count count : category.getValues()) {
                if (count.getName().equals(Category.GENE.getName())) {
                    Properties properties = new Properties();
                    properties.put(fieldName, entityName);
                    links.add(createHyperLink(id, category.getName(), count.getName(), count.getCount(), GENES_CAUSING_PHENOTYPE, false, properties).toString());
                }
            }
    }

    private void createExpressedGenesData(List<String> links, String id, FieldName fieldName) {
        QueryResponse response = getQueryResponse(fieldName.getName());

        FacetField category = response.getFacetField("category");
        if (category != null && category.getValues() != null)
            for (FacetField.Count count : category.getValues()) {
                if (count.getName().equals(Category.GENE.getName())) {
                    Properties properties = new Properties();
                    properties.put(fieldName.getName(), entityName);
                    links.add(createHyperLink(id, category.getName(), count.getName(), count.getCount(), GENES_EXPRESSED, false, properties).toString());
                }
            }
    }

    private void getGoAnnotationData(List<String> links, String id, String fieldName) {
        QueryResponse response = getQueryResponse(fieldName);

        FacetField category = response.getFacetField("category");
        if (category != null && category.getValues() != null)
            for (FacetField.Count count : category.getValues()) {
                if (count.getName().equals(Category.GENE.getName())) {
                    Properties properties = new Properties();
                    properties.put(fieldName, entityName);
                    links.add(createHyperLink(id, category.getName(), count.getName(), count.getCount(), GENES_WITH_GO, false, properties).toString());
                }
            }
    }

    private QueryResponse getQueryResponse(String ontologyName) {
        String field = "category";
        SolrServer server = SolrService.getSolrServer("prototype");
        SolrQuery query = new SolrQuery();
        //look for the term name in an OR over multiple fields
        query.addFilterQuery(ontologyName + ":\"" + entityName + "\"");
        //only look for genes
        query.addFilterQuery("category:\"" + Category.GENE.getName() + "\"");
        query.setRows(0);
        query.setHighlight(false);
        query.setFacet(true);
        query.setFacetLimit(100);
        query.addFacetField(field);

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
            if (link == null)
                continue;
            int index = 0;
            for (String relatedDatum : relatedDataFeatureCategories) {
                if (link.contains(relatedDatum))
                    linkMap.put(index, link);
                index++;
            }
        }
        for (Integer linkID : linkMap.keySet()) {
            returnList.add(linkMap.get(linkID));
        }
        return returnList;
    }


    public List<String> getXrefLinks(String id) {
        List<String> links = new ArrayList<>();

        QueryResponse response = solrService.getRelatedDataResponse(id);

        FacetField facetField = response.getFacetField("category");

        if (facetField != null && facetField.getValues() != null)
            for (FacetField.Count count : facetField.getValues()) {
                StringBuilder link = createHyperLink(id, facetField.getName(), count.getName(), count.getCount());
                links.add(link.toString());

            }
        return links;
    }

    protected StringBuilder createHyperLink(String id, String facetFieldName, String categoryName,
                                            long categoryCount, String hyperlinkName, boolean isXref,
                                            Properties properties) {
        //this is an unpleasant hack, I need to stuff the expression popup link in here, so it's a little hijack...
        if (id.startsWith("ZDB-GENE") && StringUtils.equals(categoryName, "Expression")) {
            return getGeneExpressionPopupLink(id, categoryCount);
        } else if (id.startsWith("ZDB-GENE") && StringUtils.equals(categoryName, "Phenotype")) {
            return getGenePhenotypePopupLink(id, categoryCount);

        } else if (id.startsWith("ZDB-FIG") && StringUtils.equals(categoryName, "Publication")) {
            StringBuilder link = new StringBuilder();
            return link.append("");

        } else {
            if (StringUtils.isEmpty(hyperlinkName))

                hyperlinkName = categoryName;
            if (id.startsWith("ZDB-PERS") && StringUtils.equals(categoryName, "Community")) {
                hyperlinkName = "Labs";
            }
            StringBuilder link = new StringBuilder();
            link.append("<a href=\"/prototype?q=");
            String fq = SolrService.encode(facetFieldName + ":\"" + categoryName + "\"");
            link.append("&fq=");
            link.append(fq);
            if (isXref) {
                link.append("&fq=xref%3A%22");
                link.append(id);
            } else {
                link.append("&fq=").append(properties.propertyNames().nextElement()).append("%3A%22");
                link.append(properties.elements().nextElement());
            }
            link.append("%22");
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

    protected StringBuilder createHyperLink(String id, String facetFieldName, String categoryName, long categoryCount) {
        return createHyperLink(id, facetFieldName, categoryName, categoryCount, null, true, null);
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


    //todo: probably this should move to GBrowseService, but I don't want to refactor that in this branch till I can grab trunk again...
    public String getGBrowseLink(String id) {

        if (ActiveData.isValidActiveData(id, ActiveData.Type.TSCRIPT)) {
            List<Marker> markerList = getMarkerRepository().getTranscriptByZdbID(id).getAllRelatedMarker();
            int numberOfTargetGenes = 0;
            for (Marker marker : markerList)
                if (marker.getMarkerType().getType().name().equals(ActiveData.Type.GENE.name())) {
                    id = marker.getZdbID();
                    numberOfTargetGenes++;
                }
            if (numberOfTargetGenes > 1)
                return null;
        }
        return "<a href=\"" + "/" + ZfinPropertiesEnum.GBROWSE_PATH_FROM_ROOT + "?name=" + id + "\">Genome Browser</a>";
    }


    public String getOrthologyLink(String id) {


        return "<a href=\"" + "/action/marker/" + id + "/orthology-detail\">Orthology</a>";
    }

    public String getOrtholistLink(String id) {
        return "<a href=\"" + "/action/publication/" + id + "/orthology-list\">Orthology</a>";
    }

}
