package org.zfin.fish.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAlias;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.presentation.Fish;
import org.zfin.framework.presentation.MatchingText;
import org.zfin.framework.presentation.MatchingTextType;
import org.zfin.framework.search.SearchCriterion;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.mutant.PhenotypeService;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.Term;
import org.zfin.util.MatchType;
import org.zfin.util.MatchingService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Service class to provide methods for retrieving fish records from the data warehouse.
 */
public class FishMatchingService {

    private MatchingService service;
    private Fish fish;

    public FishMatchingService(Fish fish) {
        this.fish = fish;
    }

    /**
     * This method checks which of the criteria matched the retrieve antibody.
     * Currently, only antibody name and alias as well as Gene abbreviation and previous
     * name are supported.
     *
     * @param criteria fish search criteria
     * @return matching text collection
     */
    public Set<MatchingText> getMatchingText(FishSearchCriteria criteria) {
        if (criteria == null)
            return null;
        // no contains.
        MatchType[] honoredMatchTypes = {MatchType.EXACT, MatchType.EXACT_WORD, MatchType.STARTS_WITH_WORDS, MatchType.STARTS_WITH};
        service = new MatchingService(honoredMatchTypes);
        if (criteria.getGeneOrFeatureNameCriteria().hasValues()) {
            checkGeneFeatureMatches(criteria);
            addMatchingConstruct(criteria.getGeneOrFeatureNameCriteria().getValue());
        }
        if (criteria.getPhenotypeAnatomyCriteria().hasValues())
            checkAllTermMatches(criteria.getPhenotypeAnatomyCriteria());
        // mark as transgenic
        if (criteria.getRequireTransgenicsCriteria().isTrue())
            service.addMatchingOnFilter(MatchingTextType.TRANSGENIC, true);
        if (criteria.getRequireMorphantsCriteria().isTrue())
            service.addMatchingOnFilter(MatchingTextType.MORPHANT, true);
        if (criteria.getMutationTypeCriteria().hasValues())
            service.addMatchingOnFilter(MatchingTextType.MUTATION_TYPE, true, criteria.getMutationTypeCriteria().getValue());
        return service.getMatchingTextList();
    }

    private void addMatchingConstruct(String value) {
        String genoID = fish.getGenotype().getID();
        List<GenotypeFeature> genotypeFeatures = getMutantRepository().getGenotypeFeaturesByGenotype(genoID);
        for (GenotypeFeature genoFeature : genotypeFeatures) {
            Feature feature = genoFeature.getFeature();
            Set<FeatureMarkerRelationship> featureMarkerRelationships = feature.getConstructs();
            if (featureMarkerRelationships == null)
                continue;
            for (FeatureMarkerRelationship rel : featureMarkerRelationships) {
                Marker construct = rel.getMarker();
                service.addMatchingType(MatchType.CONTAINS);
                if (checkMarkerMatch(value, construct, MatchingTextType.CONSTRUCT_ABBREVIATION, MatchingTextType.CONSTRUCT_NAME, MatchingTextType.CONSTRUCT_ALIAS))
                    break;
                // ensure to remove Contains for anything else but constructs
                service.removeMatchingType(MatchType.CONTAINS);
                Set<MarkerRelationship> firstRelatedMarker = construct.getFirstMarkerRelationships();
                for (MarkerRelationship relatedMarkerRel : firstRelatedMarker) {
                    Marker firstMarker = relatedMarkerRel.getFirstMarker();
                    if (firstMarker.equals(construct)) {
                        Marker relatedMarker = relatedMarkerRel.getSecondMarker();
                        if (checkMarkerMatch(value, relatedMarker, MatchingTextType.RELATED_MARKER_ABBREVIATION, MatchingTextType.RELATED_MARKER_NAME, MatchingTextType.RELATED_MARKER_ALIAS)) {
                            String appendix = "[";
                            appendix += relatedMarkerRel.getMarkerRelationshipType().getSecondToFirstLabel();
                            appendix += " " + construct.getAbbreviation() + "]";
                            service.addAppendixToLastMatch(appendix);
                            break;
                        }
                    }

                }
/*
                Set<MarkerRelationship> secondRelatedMarker = construct.getSecondMarkerRelationships();
                for(MarkerRelationship relatedMarker: secondRelatedMarker){
                    int i = 0;
                }
*/
            }
        }
        service.removeMatchingType(MatchType.CONTAINS);
    }

    /**
     * Checks for matches of multiple query term matches against a fish's
     * phenotype.
     *
     * @param criterion term criterion
     */
    private void checkAllTermMatches(SearchCriterion criterion) {
        if (criterion == null || criterion.getValues() == null)
            return;
        // loop over all terms entered in the search form
        for (String queryTermID : criterion.getValues()) {
            checkSingleTermMatch(queryTermID);
        }
    }

    /**
     * Checks a query term id with all phenotypes of a given fish, i.e.
     * for all genotype experiments a fish is associated to.
     *
     * @param queryTermID query term id
     */
    private void checkSingleTermMatch(String queryTermID) {
        // no phenotype associated
        if (fish.getGenotypeExperimentIDs() == null)
            return;
        // loop over all genotype experiments for a given fish
        for (String genoxID : fish.getGenotypeExperimentIDs()) {
            List<PhenotypeStatement> phenotypeStatementList = getPhenotypeRepository().getPhenotypeStatements(genoxID);
            if (phenotypeStatementList != null) {
                Set<Term> allPhenotypeTerms = new HashSet<Term>();
                for (PhenotypeStatement phenotypeStatement : phenotypeStatementList) {
                    allPhenotypeTerms.addAll(PhenotypeService.getAllAnatomyTerms(phenotypeStatement));
                }
                compareQueryTermWithPhenotypeTermList(queryTermID, allPhenotypeTerms);
            }
        }
    }

    /**
     * checks if a query term matches any of the terms for a given phenotype statement.
     * Currently, only checks for AO matches.
     *
     * @param queryTermID    query term id
     * @param phenotypeTerms set of phenotype terms
     */
    private void compareQueryTermWithPhenotypeTermList(String queryTermID, Set<Term> phenotypeTerms) {
        // if an exact match is found continue otherwise check if
        // query term is a substructure of
        if (service.checkExactTermMatches(queryTermID, phenotypeTerms))
            return;
        service.checkSubstructureTermMatches(queryTermID, phenotypeTerms);
    }

    private void checkGeneFeatureMatches(FishSearchCriteria criteria) {
        SearchCriterion geneOrFeatureNameCriteria = criteria.getGeneOrFeatureNameCriteria();
        if (geneOrFeatureNameCriteria != null) {
            addMatchingGene(geneOrFeatureNameCriteria.getValue());
            addMatchingFeatures(geneOrFeatureNameCriteria.getValue());
            addMatchingMorpholinos(geneOrFeatureNameCriteria.getValue());
        }

    }

    private void addMatchingGene(String geneNameField) {
        if (geneNameField == null || StringUtils.isEmpty(geneNameField))
            return;
        geneNameField = geneNameField.toLowerCase().trim();
        if(addMatchingGeneSingleWord(geneNameField))
            return;
        if (geneNameField.contains(" ")) {
            String[] tokens = geneNameField.split(" ");
            for (String token : tokens)
                addMatchingGeneSingleWord(token);
        }
    }

    private boolean addMatchingGeneSingleWord(String geneNameField) {
        List<ZfinEntity> genes = fish.getAffectedGenes();
        if (CollectionUtils.isNotEmpty(genes)) {
            // the loop exists for the first match as this is enough!
            for (ZfinEntity entity : genes) {
                if (entity == null)
                    continue;
                Marker gene = getMarkerRepository().getMarkerByID(entity.getID());
                if (gene == null)
                    continue;
                if (checkMarkerMatch(geneNameField, gene, MatchingTextType.AFFECTED_GENE_ABBREVIATION, MatchingTextType.AFFECTED_GENE_NAME, MatchingTextType.AFFECTED_GENE_ALIAS))
                    return true;
            }
        }
        return false;
    }

    /**
     * Match on abbreviation, name and alias
     *
     * @param geneNameField
     * @param gene
     * @param matchingTypes need to be three components: abbreviation, name and alias
     * @return
     */
    private boolean checkMarkerMatch(String geneNameField, Marker gene, MatchingTextType... matchingTypes) {
        if (service.addMatchingText(geneNameField, gene.getAbbreviation(), matchingTypes[0]).equals(MatchType.EXACT))
            return true;
        if (service.addMatchingText(geneNameField, gene.getName(), matchingTypes[1]).equals(MatchType.EXACT))
            return true;
        Set<MarkerAlias> prevNames = gene.getAliases();
        if (prevNames != null) {
            // loop until the first match is encountered
            for (MarkerAlias prevName : prevNames) {
                if (service.addMatchingText(geneNameField, prevName.getAlias(), matchingTypes[2], gene.getAbbreviation()).equals(MatchType.EXACT))
                    break;
            }
        }
        return false;
    }

    private void addMatchingMorpholinos(String geneNameField) {
        if (geneNameField == null || StringUtils.isEmpty(geneNameField))
            return;
        geneNameField = geneNameField.toLowerCase().trim();
        List<ZfinEntity> genes = fish.getMorpholinos();
        if (CollectionUtils.isNotEmpty(genes)) {
            // the loop exists for the first match as this is enough!
            for (ZfinEntity entity : genes) {
                Marker morpholino = getMarkerRepository().getMarkerByID(entity.getID());
                // name and abbreviation is the same for morpholinos
                if (service.addMatchingText(geneNameField, morpholino.getName(), MatchingTextType.MORPHOLINO_NAME).equals(MatchType.EXACT))
                    break;
                Set<MarkerAlias> prevNames = morpholino.getAliases();
                if (prevNames != null) {
                    // loop until the first match is encountered
                    for (MarkerAlias prevName : prevNames) {
                        if (service.addMatchingText(geneNameField, prevName.getAlias(), MatchingTextType.MORPHOLINO_ALIAS).equals(MatchType.EXACT))
                            break;
                    }
                }
            }
        }
    }

    private void addMatchingFeatures(String geneNameField) {
        if (geneNameField == null || StringUtils.isEmpty(geneNameField))
            return;
        geneNameField = geneNameField.toLowerCase().trim();
        List<ZfinEntity> features = fish.getFeatures();
        addMatchingFeature(geneNameField, features);
        if (geneNameField.contains(" ")) {
            String[] tokens = geneNameField.split(" ");
            for (String token : tokens)
                addMatchingFeature(token, features);
        }

    }

    private void addMatchingFeature(String geneNameField, List<ZfinEntity> features) {
        if (CollectionUtils.isNotEmpty(features)) {
            // the loop exists for the first match as this is enough!
            for (ZfinEntity entity : features) {
                Feature feature = getFeatureRepository().getFeatureByID(entity.getID());
                if (feature == null)
                    continue;
                if (service.addMatchingText(geneNameField, feature.getAbbreviation(), MatchingTextType.FEATURE_ABBREVIATION).equals(MatchType.EXACT))
                    break;
                if (service.addMatchingText(geneNameField, feature.getName(), MatchingTextType.FEATURE_NAME).equals(MatchType.EXACT))
                    break;
                Set<FeatureAlias> prevNames = feature.getAliases();
                if (prevNames != null) {
                    // loop until the first exact match is encountered
                    for (FeatureAlias prevName : prevNames) {
                        if (service.addMatchingText(geneNameField, prevName.getAlias(), MatchingTextType.FEATURE_ALIAS).equals(MatchType.EXACT))
                            break;
                    }
                }
            }
        }
    }


}
