package org.zfin.mutant;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.expression.Experiment;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.RibbonType;
import org.zfin.gwt.curation.dto.DiseaseAnnotationDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.mutant.presentation.FishModelDisplay;
import org.zfin.mutant.presentation.PhenotypeDisplay;
import org.zfin.mutant.presentation.PhenotypeDisplayFishComparator;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;
import org.zfin.ontology.service.OntologyService;
import org.zfin.ontology.service.RibbonService;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationAuthorComparator;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Service class that deals with Phenotype-related logic
 */
@Service
public class PhenotypeService {
    @Autowired
    private FigureRepository figureRepository;

    @Autowired
    private RibbonService ribbonService;

    public static final String ANATOMY = "ANATOMY";
    public static final String GO = "GO";

    /**
     * Return a map of phenotype descriptions, comma-delimited, and grouped by ontology for a given
     * anatomy structure.
     *
     * @param fishExperiment Genotype Experiment
     * @param anatomyItem    Anatomy Term
     * @return HashMap
     */
    public static Map<String, Set<String>> getPhenotypesGroupedByOntology(FishExperiment fishExperiment, GenericTerm anatomyItem) {
        if (fishExperiment == null || anatomyItem == null || fishExperiment.getPhenotypeExperiments() == null) {
            return null;
        }

        Map<String, Set<String>> map = new TreeMap<>(new PhenotypeComparator());

        for (PhenotypeExperiment phenotype : fishExperiment.getPhenotypeExperiments()) {
            for (PhenotypeStatement phenoStatement : phenotype.getPhenotypeStatements()) {
                GenericTerm subTerm = phenoStatement.getEntity().getSubterm();
                if (StringUtils.equals(phenoStatement.getEntity().getSuperterm().getZdbID(), anatomyItem.getZdbID()) ||
                        (subTerm != null && StringUtils.equals(subTerm.getZdbID(), anatomyItem.getZdbID()))) {
                    StringBuilder keyBuilder = new StringBuilder(50);
                    if (subTerm != null) {
                        keyBuilder.append(phenoStatement.getEntity().getSubterm().getTermName());
                        Term anatomyTerm = phenoStatement.getEntity().getSuperterm();
                        keyBuilder.append(":");
                        keyBuilder.append(anatomyTerm.getTermName());
                    } else {
                        keyBuilder.append(ANATOMY);
                    }

                    String termName = phenoStatement.getQuality().getTermName();
                    StringBuilder termNameBuilder = new StringBuilder(50);
                    String tag = phenoStatement.getTag();
                    if (termName.equals(GenericTerm.QUALITY) && tag.equals(PhenotypeStatement.Tag.ABNORMAL.toString())) {
                        termNameBuilder.append(PhenotypeStatement.Tag.ABNORMAL.toString());
                    } else if (tag != null && tag.equals(PhenotypeStatement.Tag.NORMAL.toString())) {
                        continue;
                    } else {
                        termNameBuilder.append(termName);
                    }

                    Set<String> phenotypes = map.get(keyBuilder.toString());
                    if (phenotypes == null) {
                        phenotypes = new TreeSet<>();
                    }
                    phenotypes.add(termNameBuilder.toString());
                    map.put(keyBuilder.toString(), phenotypes);
                }
            }
        }
        return map;
    }

    /**
     * Retrieve a list of phenotype statements that contain the given term
     * in any position (E1 or E2) in a given genotype experiment
     *
     * @param genoExperiment Genotype Experiment
     * @return list of phenotype statements
     */
    public static Set<PhenotypeStatement> getPhenotypeStatements(FishExperiment genoExperiment) {
        return getPhenotypeStatements(genoExperiment, null);
    }

    /**
     * Retrieve a list of phenotype statements that contain the given term
     * in any position (E1 or E2) in a given genotype experiment
     *
     * @param genoExperiment Genotype Experiment
     * @param term           Term
     * @return list of phenotype statements
     */
    public static Set<PhenotypeStatement> getPhenotypeStatements(FishExperiment genoExperiment, GenericTerm term) {
        if (genoExperiment == null) {
            return null;
        }

        boolean includeAll = term == null;
        Set<PhenotypeStatement> phenoStatements = new HashSet<>(5);
        for (PhenotypeExperiment phenox : genoExperiment.getPhenotypeExperiments()) {
            for (PhenotypeStatement statement : phenox.getPhenotypeStatements()) {
                if (includeAll || statement.contains(term)) {
                    phenoStatements.add(statement);
                }
            }
        }
        // since I do not want to change the equals() method to ignore the PK id
        // I have to create a distinct list myself.
        Set<PhenotypeStatement> distinctPhenoStatements = new HashSet<>(phenoStatements.size());
        for (PhenotypeStatement statement : phenoStatements) {
            boolean recordFound = false;
            for (PhenotypeStatement distinctStatement : distinctPhenoStatements) {
                if (distinctStatement.equalsByPhenotype(statement)) {
                    recordFound = true;
                    break;
                }
            }
            if (!recordFound) {
                distinctPhenoStatements.add(statement);
            }
        }
        return distinctPhenoStatements;
    }

    /**
     * Returns obsoleted terms from the possible 5-term combination.
     *
     * @param phenotypeStatement phenotype statement
     * @return list of obsoleted terms
     */
    public static Set<GenericTerm> getObsoleteTerm(PhenotypeStatement phenotypeStatement) {
        Set<GenericTerm> obsoletedTerms = new HashSet<>(3);
        if (phenotypeStatement.getEntity().getSuperterm().isObsolete()) {
            obsoletedTerms.add(phenotypeStatement.getEntity().getSuperterm());
        } else if (phenotypeStatement.getEntity().getSubterm() != null && phenotypeStatement.getEntity().getSubterm().isObsolete()) {
            obsoletedTerms.add(phenotypeStatement.getEntity().getSubterm());
        } else if (phenotypeStatement.getQuality() != null && phenotypeStatement.getQuality().isObsolete()) {
            obsoletedTerms.add(phenotypeStatement.getQuality());
        } else if (phenotypeStatement.getRelatedEntity() != null) {
            PostComposedEntity entity = phenotypeStatement.getRelatedEntity();
            if (entity.getSuperterm() != null && entity.getSuperterm().isObsolete()) {
                obsoletedTerms.add(entity.getSuperterm());
            }
            if (entity.getSubterm() != null && entity.getSubterm().isObsolete()) {
                obsoletedTerms.add(entity.getSubterm());
            }
        }
        return obsoletedTerms;
    }

    /**
     * Returns obsoleted terms from the possible 5-term combination.
     *
     * @param phenotypeStatement phenotype statement
     * @return list of obsoleted terms
     */
    public static Set<GenericTerm> getSecondaryTerm(PhenotypeStatement phenotypeStatement) {
        Set<GenericTerm> secondaryTerms = new HashSet<>(3);
        if (phenotypeStatement.getEntity().getSuperterm().isSecondary()) {
            secondaryTerms.add(phenotypeStatement.getEntity().getSuperterm());
        } else if (phenotypeStatement.getEntity().getSubterm() != null && phenotypeStatement.getEntity().getSubterm().isSecondary()) {
            secondaryTerms.add(phenotypeStatement.getEntity().getSubterm());
        } else if (phenotypeStatement.getQuality() != null && phenotypeStatement.getQuality().isSecondary()) {
            secondaryTerms.add(phenotypeStatement.getQuality());
        } else if (phenotypeStatement.getRelatedEntity() != null) {
            PostComposedEntity entity = phenotypeStatement.getRelatedEntity();
            if (entity.getSuperterm() != null && entity.getSuperterm().isSecondary()) {
                secondaryTerms.add(entity.getSuperterm());
            }
            if (entity.getSubterm() != null && entity.getSubterm().isSecondary()) {
                secondaryTerms.add(entity.getSubterm());
            }
        }
        return secondaryTerms;
    }

    public static String getSubstructureName(PhenotypeStatement phenotypeStatement, Term parentTerm) {
        PostComposedEntity entity = phenotypeStatement.getEntity();
        if (entity != null) {
            GenericTerm superterm = entity.getSuperterm();
            if (superterm != null) {
                if (getOntologyRepository().isParentChildRelationshipExist(parentTerm, superterm)) {
                    return superterm.getTermName();
                }
            }
            Term subterm = entity.getSubterm();
            if (subterm != null) {
                if (getOntologyRepository().isParentChildRelationshipExist(parentTerm, subterm)) {
                    return subterm.getTermName();
                }
            }
        }
        PostComposedEntity relatedEntity = phenotypeStatement.getRelatedEntity();
        if (relatedEntity != null) {
            GenericTerm superterm = relatedEntity.getSuperterm();
            if (superterm != null) {
                if (getOntologyRepository().isParentChildRelationshipExist(parentTerm, superterm)) {
                    return superterm.getTermName();
                }
            }
            Term subterm = relatedEntity.getSubterm();
            if (subterm != null) {
                if (getOntologyRepository().isParentChildRelationshipExist(parentTerm, subterm)) {
                    return subterm.getTermName();
                }
            }
        }
        return null;
    }

    public static boolean hasStructureOrSubstructure(GenericTerm anatomyItem, PhenotypeStatement statement) {
        PostComposedEntity entity = statement.getEntity();
        if (getOntologyRepository().isParentChildRelationshipExist(anatomyItem, entity.getSuperterm())) {
            return true;
        }
        if (entity.getSubterm() != null && getOntologyRepository().isParentChildRelationshipExist(anatomyItem, entity.getSubterm())) {
            return true;
        }

        PostComposedEntity relatedEntity = statement.getRelatedEntity();
        if (relatedEntity == null) {
            return false;
        }
        if (getOntologyRepository().isParentChildRelationshipExist(anatomyItem, relatedEntity.getSuperterm())) {
            return true;
        }
        if (entity.getSubterm() != null && getOntologyRepository().isParentChildRelationshipExist(anatomyItem, relatedEntity.getSubterm())) {
            return true;
        }

        return false;
    }

    public static List<Publication> getPublicationList(GenericTerm disease, Fish fish, String orderBy) {
        List<Publication> publicationList = getPublicationList(disease, fish);
        if (publicationList == null) {
            return null;
        }
        orderPublications(publicationList, orderBy);
        return publicationList;
    }

    public static List<Publication> getPublicationList(GenericTerm disease, Fish fish) {
        List<FishModelDisplay> model = OntologyService.getDiseaseModelsWithFishModel(disease);
        if (CollectionUtils.isEmpty(model)) {
            return null;
        }
        Set<Publication> publicationSet = new HashSet<>();
        for (FishModelDisplay display : model) {
            if (display.getFishModel().getFish().equals(fish)) {
                publicationSet.addAll(display.getPublications());
            }
        }
        return new ArrayList<>(publicationSet);
    }

    public static List<Publication> getPublicationList(GenericTerm disease, FishExperiment fishExperiment, String orderBy) {
        List<Publication> publicationList = getPublicationList(disease, fishExperiment);
        if (publicationList == null) {
            return null;
        }
        orderPublications(publicationList, orderBy);
        return publicationList;
    }

    public static List<Publication> getPublicationList(GenericTerm disease, FishExperiment fishExperiment) {
        List<FishModelDisplay> model = OntologyService.getDiseaseModelsWithFishModel(disease);
        if (CollectionUtils.isEmpty(model)) {
            return null;
        }
        Set<Publication> publicationSet = new HashSet<>();
        for (FishModelDisplay display : model) {
            if (display.getFishModel().equals(fishExperiment)) {
                publicationSet.addAll(display.getPublications());
            }
        }
        return new ArrayList<>(publicationSet);
    }

    public static List<DiseaseAnnotationDTO> getDiseaseModelDTOs(String publicationID) {
        List<DiseaseAnnotation> diseaseAnnotationList = getPhenotypeRepository().getHumanDiseaseModels(publicationID);

        List<DiseaseAnnotationDTO> dtoList = new ArrayList<>();
        for (DiseaseAnnotation diseaseAnnotation : diseaseAnnotationList) {
            dtoList.add(DTOConversionService.convertToDiseaseModelDTO(diseaseAnnotation));
        }

        return dtoList;
    }

    private static void orderPublications(List<Publication> publications, String orderBy) {
        if (orderBy != null && orderBy.equalsIgnoreCase("author")) {
            Collections.sort(publications, new PublicationAuthorComparator());
        }
        if (StringUtils.isEmpty(orderBy) || orderBy.equalsIgnoreCase("date")) {
            Collections.sort(publications);
        }
    }

    public static Set<PhenotypeStatementWarehouse> getPhenotypeObserved(Fish fish, GenericTerm term, boolean includeSubstructures) {
        if (fish == null) {
            return null;
        }

        List<PhenotypeStatementWarehouse> phenotypeStatementList = getMutantRepository().getPhenotypeObserved(term, fish, includeSubstructures);
        // since I do not want to change the equals() method to ignore the PK id
        // I have to create a distinct list myself.
        Set<PhenotypeStatementWarehouse> distinctPhenoStatements = new HashSet<>(phenotypeStatementList.size());
        for (PhenotypeStatementWarehouse statement : phenotypeStatementList) {
            boolean recordFound = false;
            for (PhenotypeStatementWarehouse distinctStatement : distinctPhenoStatements) {
                if (distinctStatement.equalsByPhenotype(statement)) {
                    recordFound = true;
                    break;
                }
            }
            if (!recordFound) {
                distinctPhenoStatements.add(statement);
            }
        }
        return distinctPhenoStatements;
    }

    private static class PhenotypeComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return +1;
            }
            if (o1.equals(ANATOMY) && !o2.equals(ANATOMY)) {
                return -1;
            }
            if (o2.equals(ANATOMY) && !o1.equals(ANATOMY)) {
                return +1;
            }
            return o1.compareTo(o2);
        }

    }

    /**
     * retrieve all distinct Anatomy terms that are referenced in the entity or related entity for a given
     * phenotype statement.
     *
     * @param phenotypeStatement phenotype statement
     * @return list of terms
     */
    public static Set<Term> getAllAnatomyTerms(PhenotypeStatement phenotypeStatement) {
        if (phenotypeStatement == null) {
            return null;
        }
        // at most 4 entries
        Set<Term> termList = new HashSet<>(2);
        PostComposedEntity entity = phenotypeStatement.getEntity();
        if (entity != null) {
            addIfAnatomyTerm(termList, entity.getSuperterm());
            addIfAnatomyTerm(termList, entity.getSubterm());
        }
        PostComposedEntity relatedEntity = phenotypeStatement.getRelatedEntity();
        if (relatedEntity != null) {
            addIfAnatomyTerm(termList, relatedEntity.getSuperterm());
            addIfAnatomyTerm(termList, relatedEntity.getSubterm());
        }
        return termList;
    }

    public static Set<Term> getAllGOTerms(PhenotypeStatement phenotypeStatement) {
        if (phenotypeStatement == null) {
            return null;
        }
        // at most 4 entries
        Set<Term> termList = new HashSet<>(2);
        PostComposedEntity entity = phenotypeStatement.getEntity();
        if (entity != null) {
            addIfGOTerm(termList, entity.getSuperterm());
            addIfGOTerm(termList, entity.getSubterm());
        }
        PostComposedEntity relatedEntity = phenotypeStatement.getRelatedEntity();
        if (relatedEntity != null) {
            addIfGOTerm(termList, relatedEntity.getSuperterm());
            addIfGOTerm(termList, relatedEntity.getSubterm());
        }
        return termList;
    }

    private static void addIfAnatomyTerm(Set<Term> termList, GenericTerm superterm) {
        if (superterm != null) {
            if (superterm.getOntology().equals(Ontology.ANATOMY)) {
                termList.add(superterm);
            }
        }
    }

    private static void addIfGOTerm(Set<Term> termList, GenericTerm superterm) {
        if (superterm != null) {
            if (superterm.getOntology().equals(Ontology.GO_BP) || (superterm.getOntology().equals(Ontology.GO_MF)) || (superterm.getOntology().equals(Ontology.GO_CC))) {
                termList.add(superterm);
            }
        }
    }

    /**
     * Create a list of phenotypeDisplay objects organized by phenotype statement first,
     * then by the associated experiment.
     */
    public static List<PhenotypeDisplay> getPhenotypeDisplays(List<PhenotypeStatementWarehouse> phenotypeStatements, String groupBy, String sortBy) {
        if (phenotypeStatements != null && phenotypeStatements.size() > 0) {

            // a map of phenotypeStatement-experiment-publication-concatenated-Ids as keys and display objects as values
            Map<String, PhenotypeDisplay> phenoMap = new HashMap<>();

            for (PhenotypeStatementWarehouse pheno : phenotypeStatements) {


                Figure fig = pheno.getPhenotypeWarehouse().getFigure();
                Publication pub = fig.getPublication();

                FishExperiment fishExp = pheno.getPhenotypeWarehouse().getFishExperiment();
                Experiment exp = fishExp.getExperiment();

                String key;
                String keyPheno = pheno.getShortName();
                if (groupBy.equals("condition")) {
                    if (fishExp.isStandardOrGenericControl()) {
                        key = keyPheno + "standard";
                    } else {
                        key = keyPheno + exp.getZdbID();
                    }

                    if (sortBy.equals("fish")) {
                        key += pheno.getPhenotypeWarehouse().getFishExperiment().getFish().getZdbID();
                    }
                } else if (groupBy.equals("str")) {
                    key = keyPheno;
                } else {
                    key = keyPheno + pheno.getPhenotypeWarehouse().getFishExperiment().getFish().getZdbID();
                }

                PhenotypeDisplay phenoDisplay;

                // if the key not in the map, instantiate a display object and add it to the map
                // otherwise, get the display object from the map
                if (!phenoMap.containsKey(key)) {
                    phenoDisplay = new PhenotypeDisplay(pheno);
                    phenoDisplay.setPhenoStatement(pheno);

                    SortedMap<Publication, SortedSet<Figure>> figuresPerPub = new TreeMap<>();
                    SortedSet<Figure> figures = new TreeSet<>();
                    figures.add(fig);
                    figuresPerPub.put(pub, figures);

                    phenoDisplay.setFiguresPerPub(figuresPerPub);

                    phenoMap.put(key, phenoDisplay);
                } else {
                    phenoDisplay = phenoMap.get(key);

                    if (phenoDisplay.getFiguresPerPub().containsKey(pub)) {
                        phenoDisplay.getFiguresPerPub().get(pub).add(fig);
                    } else {
                        SortedSet<Figure> figures = new TreeSet<>();
                        figures.add(fig);
                        phenoDisplay.getFiguresPerPub().put(pub, figures);
                    }
                }
            }

            List<PhenotypeDisplay> phenoDisplays = new ArrayList<>(phenoMap.size());

            if (phenoMap.values().size() > 0) {
                phenoDisplays.addAll(phenoMap.values());
                if (sortBy.equals("phenotypeStatement")) {
                    Collections.sort(phenoDisplays);
                } else {
                    Collections.sort(phenoDisplays, new PhenotypeDisplayFishComparator());
                }
            }

            return phenoDisplays;

        } else {
            return null;
        }
    }

    /**
     * Create a list of FigureSummaryDisplay objects for a given genotype
     */
    public static List<FigureSummaryDisplay> getPhenotypeFigureSummaryForGenotype(Genotype genotype) {
        List<Figure> phenotypeFigures = RepositoryFactory.getPhenotypeRepository().getPhenotypeFiguresForGenotype(genotype);
        List<FigureSummaryDisplay> figureSummaryDisplays = new ArrayList<>(phenotypeFigures.size());
        Set<Figure> figures = new HashSet<>(phenotypeFigures.size());

        for (Figure figure : phenotypeFigures) {
            FigureSummaryDisplay figureSummaryDisplay = new FigureSummaryDisplay();
            if (figures.add(figure)) {
                figureSummaryDisplay.setFigure(figure);
                figureSummaryDisplay.setPublication(figure.getPublication());
                List<PhenotypeStatementWarehouse> phenotypeStatements = RepositoryFactory.getPhenotypeRepository().getPhenotypeStatementsForFigureAndGenotype(figure, genotype);
                figureSummaryDisplay.setPhenotypeStatementList(FishService.getDistinctPhenotypeStatements(phenotypeStatements));
                figureSummaryDisplays.add(figureSummaryDisplay);
                if (!figure.isImgless()) {
                    figureSummaryDisplay.setImgCount(figure.getImages().size());
                    figureSummaryDisplay.setThumbnail(figure.getImages().iterator().next().getThumbnail());
                }
            }
        }

        return figureSummaryDisplays;

    }


    public JsonResultResponse<Image> getPhenotypeImages(String geneId, String termId, boolean isOther, String phenotypeIds, boolean excludeEaps, boolean excludeSTRs,
                                                        Pagination pagination) throws IOException, SolrServerException {
        JsonResultResponse<Image> response = new JsonResultResponse<>();

        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/phenotype-annotation");
        query.addFilterQuery(FieldName.MONOGENIC_GENE_ZDB_ID + ":" + geneId);
        query.addFilterQuery("has_image:true");
        if (excludeEaps) {
            query.addFilterQuery("is_eap:false");
        }
        if (excludeSTRs) {
            query.addFilterQuery("has_str:false");
        }
        ribbonService.addRibbonTermQuery(query, RibbonType.PHENOTYPE, termId, isOther);
        if (StringUtils.isNotEmpty(phenotypeIds)) {
            query.addFilterQuery(Arrays.stream(phenotypeIds.split(","))
                    .map(phenotypeId -> "id:psg-" + phenotypeId)
                    .collect(Collectors.joining(" OR ")));
        }
        String imageFieldName = FieldName.IMG_ZDB_ID.getName();
        query.addFacetField(imageFieldName);
        query.setParam("facet.limit", Integer.toString(pagination.getLimit()));
        query.setParam("facet.offset", Integer.toString(pagination.getStart()));
        query.setGetFieldStatistics("{!countDistinct=true}" + imageFieldName);

        QueryResponse queryResponse = SolrService.getSolrClient().query(query);
        List<String> imageIds = queryResponse
                .getFacetField(imageFieldName)
                .getValues()
                .stream()
                .map(FacetField.Count::getName)
                .collect(Collectors.toList());
        response.setTotal(queryResponse.getFieldStatsInfo().get(imageFieldName).getCountDistinct());
        response.setResults(figureRepository.getImages(imageIds));
        return response;
    }

}


