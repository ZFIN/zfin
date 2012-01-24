package org.zfin.fish.presentation;


import org.apache.commons.lang.StringUtils;
import org.zfin.fish.MutationType;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.ontology.Term;

import java.util.*;

/**
 * Form bean used for the antibody search page.
 */
public class FishSearchFormBean extends PaginationBean {

    public static final String SHOW_ALL = "showAll";

    private List<Fish> fishList;
    private String geneOrFeatureName;
    // default value: show all records in regards to filter elements
    private String filter1 = SHOW_ALL;


    public String getMutationType() {
        return mutationType;
    }

    public void setMutationType(String mutationType) {
        this.mutationType = mutationType;
    }

    public String getFilter1() {
        return filter1;
    }

    public void setFilter1(String filter1) {
        this.filter1 = filter1;
    }

    private String mutationType;

    private Term term;
    private String anatomyTermNames;
    private String anatomyTermIDs;

    private boolean includeSubstructures;
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
        this.geneOrFeatureName = geneOrFeatureName;
    }


    public List<Fish> getFishList() {
        return fishList;
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
                StringUtils.isEmpty(anatomyTermIDs);
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

    public void setFishList(List<Fish> fishList) {
        this.fishList = fishList;

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
        mutationTypeList.put(MutationType.SMALL_DELETION.getName(), FeatureTypeEnum.SEQUENCE_VARIANT.getDisplay());
        mutationTypeList.put(MutationType.COMPLEX.getName(), FeatureTypeEnum.COMPLEX_SUBSTITUTION.getDisplay());
        mutationTypeList.put(MutationType.TRANSLOCATION.getName(), FeatureTypeEnum.TRANSLOC.getDisplay());
        mutationTypeList.put(MutationType.UNSPECIFIED.getName(), FeatureTypeEnum.UNSPECIFIED.getDisplay());

        return mutationTypeList;
    }


    public List<Integer> getRecordsPerPageList() {
        return Arrays.asList(20,50,100,200);
    }

}



