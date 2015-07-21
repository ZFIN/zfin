package org.zfin.fish.presentation;


import org.apache.commons.lang.StringUtils;
import org.zfin.fish.FishSearchResult;
import org.zfin.fish.MutationType;
import org.zfin.fish.WarehouseSummary;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.ontology.Term;
import org.zfin.mutant.Fish;

import java.util.*;

/**
 * Form bean used for the antibody search page.
 */
public class FishSearchFormBean extends PaginationBean {

    public static final String SHOW_ALL = "showAll";

    private FishSearchResult fishSearchResult;
    private String geneOrFeatureName;
    // default value: show all records in regards to filter elements
    private String filter1 = SHOW_ALL;
    private WarehouseSummary summary;


    public FishSearchResult getFishSearchResult() {
        return fishSearchResult;
    }

    public void setFishSearchResult(FishSearchResult fishSearchResult) {
        this.fishSearchResult = fishSearchResult;
    }

    public WarehouseSummary getSummary() {
        return summary;
    }

    public void setSummary(WarehouseSummary summary) {
        this.summary = summary;
    }

    public String getMutationType() {
        return mutationType;
    }

    public void setMutationType(String mutationType) {
        this.mutationType = mutationType;
    }

    public String getFilter1() {
        return filter1;
    }

    public String getAogoTermNames() {
        return aogoTermNames;
    }

    public void setAogoTermNames(String aogoTermNames) {
        this.aogoTermNames = aogoTermNames;
    }

    public String getAogoTermIDs() {
        return aogoTermIDs;
    }

    public void setAogoTermIDs(String aogoTermIDs) {
        this.aogoTermIDs = aogoTermIDs;
    }

    public void setFilter1(String filter1) {

        this.filter1 = filter1;
    }

    private String mutationType;

    private Term term;
    private String anatomyTermNames;
    private String anatomyTermIDs;
    private String goTermNames;
    private String goTermIDs;
    private String aogoTermNames;
    private String aogoTermIDs;

    private boolean includeSubstructures;

    public String getGoTermNames() {
        return goTermNames;
    }

    public void setGoTermNames(String goTermNames) {
        this.goTermNames = goTermNames;
    }

    public String getGoTermIDs() {
        return goTermIDs;
    }

    public void setGoTermIDs(String goTermIDs) {
        this.goTermIDs = goTermIDs;
    }

    // by default sort by best match
    private String sortBy = SortBy.BEST_MATCH.toString();

    private String fishID;


    public String getFishID() {
        return fishID;
    }

    public void setFishID(String fishID) {
        this.fishID = fishID;
    }

    public String getGeneOrFeatureName() {
        return geneOrFeatureName;
    }

    public void setGeneOrFeatureName(String geneOrFeatureName) {
        this.geneOrFeatureName = geneOrFeatureName.trim();
    }

    public boolean isMorphantsOnly() {
        return StringUtils.equals(filter1, "morphantsOnly");
    }


    public boolean isExcludeMorphants() {
        return StringUtils.equals(filter1, "excludeMorphants");
    }

    public boolean isTransgenicsOnly() {
        return StringUtils.equals(filter1, "tgOnly");
    }

    public boolean isExcludeTransgenics() {
        return StringUtils.equals(filter1, "excludeTg");
    }

    public boolean isShowAll() {
        return StringUtils.equals(filter1, SHOW_ALL);
    }

    public boolean isShowAllMutantFish() {
        return StringUtils.equals(filter1, SHOW_ALL) &&
                !hasMutationTypeSelected() &&
                StringUtils.isEmpty(geneOrFeatureName) &&
                StringUtils.isEmpty(anatomyTermIDs) &&
                StringUtils.isEmpty(goTermIDs);
    }


    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public String getAnatomyTermNames() {
        return anatomyTermNames;
    }

    public List<String> getTermNames() {
        if (StringUtils.isEmpty(anatomyTermNames))
            return null;
        String[] names = anatomyTermNames.split("\\|");
        List<String> termNameList = Arrays.asList(names);
        return termNameList;
    }

    public void setAnatomyTermNames(String anatomyTermNames) {
        this.anatomyTermNames = anatomyTermNames;
    }

    public String getAnatomyTermIDs() {
        return anatomyTermIDs;
    }

    public void setAnatomyTermIDs(String anatomyTermIDs) {
        this.anatomyTermIDs = anatomyTermIDs;
    }


    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isBestMatchSort() {
        return sortBy.equals(SortBy.BEST_MATCH.toString());
    }

    public boolean isGeneSortAscending() {
        return sortBy.equals(SortBy.GENES.toString());
    }

    public boolean isGeneSortDescending() {
        return sortBy.equals(SortBy.GENES_REVERSE.toString());
    }

    public boolean isFeatureSortAscending() {
        return sortBy.equals(SortBy.FEATURES.toString());
    }

    public boolean isFeatureSortDescending() {
        return sortBy.equals(SortBy.FEATURES_REVERSE.toString());
    }

    public boolean isIncludeSubstructures() {
        return includeSubstructures;
    }

    public void setIncludeSubstructures(boolean includeSubstructures) {
        this.includeSubstructures = includeSubstructures;
    }

    public boolean hasMutationTypeSelected() {
        if (mutationType == null)
            return false;
        if (mutationType.equalsIgnoreCase("Select") || mutationType.equalsIgnoreCase("Any"))
            return false;
        return true;
    }

    /**
     * Accessor that returns true if any of the matching text fields are used in the search form.
     * //ToDo: Formalize this so it is more reusable on other search forms.
     *
     * @return boolean
     */

    public enum Type {
        SEARCH
    }


    public Map<String, String> getMutationTypeList() {
        LinkedHashMap<String, String> mutationTypeList = new LinkedHashMap<String, String>();
        mutationTypeList.put("Any", "Any");
        mutationTypeList.put(MutationType.POINT_MUTATION.getName(), FeatureTypeEnum.POINT_MUTATION.getDisplay());
        mutationTypeList.put(MutationType.DEFICENCY.getName(), FeatureTypeEnum.DEFICIENCY.getDisplay());
        mutationTypeList.put(MutationType.TRANSGENIC_INSERTION.getName(), FeatureTypeEnum.TRANSGENIC_INSERTION.getDisplay());
        mutationTypeList.put(MutationType.INSERTION.getName(), FeatureTypeEnum.INSERTION.getDisplay());
        mutationTypeList.put(MutationType.SMALL_DELETION.getName(), FeatureTypeEnum.DELETION.getDisplay());

        mutationTypeList.put(MutationType.INVERSION.getName(), FeatureTypeEnum.INVERSION.getDisplay());
        mutationTypeList.put(MutationType.UNKNOWN.getName(), FeatureTypeEnum.SEQUENCE_VARIANT.getDisplay());
        mutationTypeList.put(MutationType.COMPLEX.getName(), FeatureTypeEnum.COMPLEX_SUBSTITUTION.getDisplay());
        mutationTypeList.put(MutationType.TRANSLOCATION.getName(), FeatureTypeEnum.TRANSLOC.getDisplay());
        mutationTypeList.put(MutationType.UNSPECIFIED.getName(), FeatureTypeEnum.UNSPECIFIED.getDisplay());
        mutationTypeList.put(MutationType.INDEL.getName(), FeatureTypeEnum.INDEL.getDisplay());
        return mutationTypeList;
    }

    public Map<String, String> getOntologyTypeList() {
        LinkedHashMap<String, String> ontologyTypeList = new LinkedHashMap<String, String>();
        ontologyTypeList.put("AO", "AO");
        ontologyTypeList.put("GO", "GO");
        return ontologyTypeList;
    }


    public List<Integer> getRecordsPerPageList() {
        return Arrays.asList(20, 50, 100, 200);
    }

}



