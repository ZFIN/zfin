package org.zfin.antibody.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.UIFieldTransformer;
import org.zfin.util.FilterType;

/**
 * This class is used to contain the search criteria for antibody searches.
 */
public class AntibodySearchCriteria extends Antibody {

    private String antigenGeneName;
    private FilterType antibodyNameFilterType;
    private FilterType antigenNameFilterType;
    private String assay;
    private int resultsPerPage;
    private boolean zircOnly;
    private PaginationBean paginationBean;
    private AnatomyItem anatomyTerm;
    private DevelopmentStage startStage;
    private DevelopmentStage endStage;
    private String anatomyTermsString;
    private boolean includeSubstructures;
    private boolean anatomyEveryTerm;
    private String antigenName;

    public static final String ANY = "Any";
    // instance variable to cache the outcome
    private Boolean stageDefinedEvaluated;

    public String getAssay() {
        return assay;
    }

    public void setAssay(String assay) {
        this.assay = assay;
    }

    public FilterType getAntibodyNameFilterType() {
        return antibodyNameFilterType;
    }

    public void setAntibodyNameFilterType(FilterType antibodyNameFilterType) {
        this.antibodyNameFilterType = antibodyNameFilterType;
    }

    public FilterType getAntigenNameFilterType() {
        return antigenNameFilterType;
    }

    public void setAntigenNameFilterType(FilterType antigenNameFilterType) {
        this.antigenNameFilterType = antigenNameFilterType;
    }

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public void setResultsPerPage(int resultsPerPage) {
        this.resultsPerPage = resultsPerPage;
    }

    public String getAntigenGeneName() {
        return antigenGeneName;
    }

    public void setAntigenGeneName(String antigenGeneName) {
        this.antigenGeneName = UIFieldTransformer.transformTextEntryFieldValue(antigenGeneName);
    }

    public boolean isZircOnly() {
        return zircOnly;
    }

    public void setZircOnly(boolean zircOnly) {
        this.zircOnly = zircOnly;
    }

    public PaginationBean getPaginationBean() {
        return paginationBean;
    }

    public void setPaginationBean(PaginationBean paginationBean) {
        this.paginationBean = paginationBean;
    }

    public DevelopmentStage getStartStage() {
        if (startStage == null) {
            startStage = new DevelopmentStage();
            return startStage;
        }
        return startStage;
    }

    public void setStartStage(DevelopmentStage startStage) {
        this.startStage = startStage;
    }

    public DevelopmentStage getEndStage() {
        if (endStage == null) {
            endStage = new DevelopmentStage();
            return endStage;
        }
        return endStage;
    }

    public void setEndStage(DevelopmentStage endStage) {
        this.endStage = endStage;
    }

    public String getAnatomyTermsString() {
        return anatomyTermsString;
    }

    public void setAnatomyTermsString(String anatomyTermsString) {
        this.anatomyTermsString = anatomyTermsString;
    }


    /**
     * Apply stage filter if a zdb ID is provided and
     * start != zygote AND
     * end   != adult
     *
     * @return boolean
     */
    public boolean isStageDefined() {
        if (stageDefinedEvaluated != null)
            return stageDefinedEvaluated;

        DevelopmentStage startStageFilter = getStartStage();
        DevelopmentStage endStageFilter = getEndStage();
        if ((startStageFilter != null && !StringUtils.isEmpty(startStageFilter.getZdbID()) ||
                endStageFilter != null && !StringUtils.isEmpty(endStageFilter.getZdbID()))) {
            if (startStageFilter != null && startStageFilter.getZdbID().equals(DevelopmentStage.ZYGOTE_STAGE_ZDB_ID) &&
                    endStageFilter != null && endStageFilter.getZdbID().equals(DevelopmentStage.ADULT_STAGE_ZDB_ID)) {
                stageDefinedEvaluated = false;
                return false;
            } else {
                stageDefinedEvaluated = true;
                return true;
            }
        } else {
            stageDefinedEvaluated = false;
            return false;
        }
    }

    // ToDo: Need to come up with a more generic way: register field names and then apply
    // transformer.
    public void setName(String name) {
        super.setName(UIFieldTransformer.transformTextEntryFieldValue(name));
    }

    public boolean isAnatomyDefined() {
        return !StringUtils.isEmpty(anatomyTermsString);
    }

    public boolean isHostSpeciesDefined() {
        return !StringUtils.isEmpty(getHostSpecies()) && !getHostSpecies().equals(ANY);
    }

    public boolean isAssaySearch() {
        return !StringUtils.isEmpty(getAssay()) && !getAssay().equals(ANY);
    }

    public String getAntigenName() {
        return antigenName;
    }

    public void setAntigenName(String antigenName) {
        this.antigenName = antigenName;
    }

    public boolean isIncludeSubstructures() {
        return includeSubstructures;
    }

    public void setIncludeSubstructures(boolean includeSubstructures) {
        this.includeSubstructures = includeSubstructures;
    }

    public boolean isAnatomyEveryTerm() {
        return anatomyEveryTerm;
    }

    public void setAnatomyEveryTerm(boolean anatomyEveryTerm) {
        this.anatomyEveryTerm = anatomyEveryTerm;
    }

    public AnatomyItem getAnatomyTerm() {
        return anatomyTerm;
    }

    public void setAnatomyTerm(AnatomyItem anatomyTerm) {
        this.anatomyTerm = anatomyTerm;
    }

    /**
     * This parses the entries from a multi-ao term submission text area field.
     *
     * @return string array
     */
    public String[] getAnatomyTerms() {
        if (StringUtils.isEmpty(anatomyTermsString))
            return null;

        String[] array;
        array = anatomyTermsString.split(",");

        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

    /**
     * Checks if any of the filter are used.
     *
     * @return boolean
     */
    public boolean isAny() {
        if (!StringUtils.isEmpty(getName()) && getAntibodyNameFilterType() != null)
            return true;
        if (!StringUtils.isEmpty(getAntigenGeneName()) && getAntigenNameFilterType() != null)
            return true;
        if (isAnatomyDefined())
            return true;
        if (isStageDefined())
            return true;
        if (isHostSpeciesDefined())
            return true;
        if (!StringUtils.isEmpty(getClonalType()) && !getClonalType().equals(ANY))
            return true;
        if (isZircOnly())
            return true;
        if (isAssaySearch())
            return true;

        return false;
    }
}
