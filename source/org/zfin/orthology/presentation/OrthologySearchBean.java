package org.zfin.orthology.presentation;

import org.zfin.orthology.*;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.util.FilterType;
import org.zfin.criteria.ZfinCriteria;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Form bean that is used for the orthology web search
 */

/*
 Todo: Need to refactor this to abstract the number of species
 to make this more scaleable if we add more species. We do not want to
 hard-code each species.
*/
public class OrthologySearchBean extends PaginationBean implements Serializable {

    private boolean isSearch = false;
    // default ordering for the result set:
    private String ordering = "Zebrafish.symbol";
    private boolean anyComparisonSpecies = true;

    private boolean includeHuman = false;
    private boolean includeMouse = false;
    private boolean includeFly = false;

    private SpeciesCriteriaBean zebrafishCriteriaBean;
    private SpeciesCriteriaBean humanCriteriaBean;
    private SpeciesCriteriaBean mouseCriteriaBean;
    private SpeciesCriteriaBean flyCriteriaBean;

    private List<Orthologs> orthologies;
    private List<String> geneSymbolValues;
    private List<String> chromosomeFilterValues;
    private ZfinCriteria basicCriteria;

    public OrthologySearchBean() {
        zebrafishCriteriaBean = new SpeciesCriteriaBean();
        zebrafishCriteriaBean.setName(Species.ZEBRAFISH.toString());
        humanCriteriaBean = new SpeciesCriteriaBean();
        humanCriteriaBean.setName(Species.HUMAN.toString());
        mouseCriteriaBean = new SpeciesCriteriaBean();
        mouseCriteriaBean.setName(Species.MOUSE.toString());
        flyCriteriaBean = new SpeciesCriteriaBean();
        flyCriteriaBean.setName(Species.FLY.toString());
        initGeneSymbolValues();
        initChromosomeFilterValues();
    }

    private void initChromosomeFilterValues() {
        chromosomeFilterValues = new ArrayList<String>();
        chromosomeFilterValues.add(FilterType.EQUALS.getName());
        chromosomeFilterValues.add(FilterType.LIST.getName());
/*      for future implementation
        chromosomeFilterValues.add(FilterType.RANGE.getNumber());
*/
    }

    private void initGeneSymbolValues() {
        geneSymbolValues = new ArrayList<String>();
        geneSymbolValues.add(FilterType.CONTAINS.getName());
        geneSymbolValues.add(FilterType.BEGINS.getName());
        geneSymbolValues.add(FilterType.EQUALS.getName());
        geneSymbolValues.add(FilterType.ENDS.getName());
    }

    public String getOrdering() {
        return ordering;
    }

    public void setOrdering(String ordering) {
        this.ordering = ordering;
    }

    public void setOrthologies(List<Orthologs> orthologies) {
        this.orthologies = orthologies;
    }

    public List<Orthologs> getOrthologies() {
        return orthologies;
    }

    public boolean isSearch() {
        return isSearch;
    }

    public boolean isIncludeHuman() {
        return includeHuman;
    }

    public void setIncludeHuman(boolean includeHuman) {
        isSearch = true;
        this.includeHuman = includeHuman;
    }

    public boolean isIncludeMouse() {
        return includeMouse;
    }

    public void setIncludeMouse(boolean includeMouse) {
        isSearch = true;
        this.includeMouse = includeMouse;
    }

    public boolean isIncludeFly() {
        return includeFly;
    }

    public void setIncludeFly(boolean includeFly) {
        isSearch = true;
        this.includeFly = includeFly;
    }

    public SpeciesCriteriaBean getZebrafishCriteriaBean() {
        return zebrafishCriteriaBean;
    }

    public void setZebrafishCriteriaBean(SpeciesCriteriaBean zebrafishCriteriaBean) {
        this.zebrafishCriteriaBean = zebrafishCriteriaBean;
    }

    public SpeciesCriteriaBean getHumanCriteriaBean() {
        return humanCriteriaBean;
    }

    public void setHumanCriteriaBean(SpeciesCriteriaBean humanCriteriaBean) {
        this.humanCriteriaBean = humanCriteriaBean;
    }

    public SpeciesCriteriaBean getMouseCriteriaBean() {
        return mouseCriteriaBean;
    }

    public void setMouseCriteriaBean(SpeciesCriteriaBean mouseCriteriaBean) {
        this.mouseCriteriaBean = mouseCriteriaBean;
    }

    public SpeciesCriteriaBean getFlyCriteriaBean() {
        return flyCriteriaBean;
    }

    public void setFlyCriteriaBean(SpeciesCriteriaBean flyCriteriaBean) {
        this.flyCriteriaBean = flyCriteriaBean;
    }

    public boolean isAnyComparisonSpecies() {
        return anyComparisonSpecies;
    }

    public void setAnyComparisonSpecies(boolean anyComparisonSpecies) {
        this.anyComparisonSpecies = anyComparisonSpecies;
    }

    public List<String> getGeneSymbolValues() {
        return geneSymbolValues;
    }

    public void setGeneSymbolValues(List<String> geneSymbolValues) {
        this.geneSymbolValues = geneSymbolValues;
    }

    public Object getChromosomeFilterValues() {
        return chromosomeFilterValues;  //To change body of created methods use File | Settings | File Templates.
    }


    /**
     * Creates list of criteria business objects to pass to criteria service class
     *
     * @return List of criteria objects
     */
    public List<SpeciesCriteriaBean> getCriteria() {
        List<SpeciesCriteriaBean> criteriaList = new ArrayList<SpeciesCriteriaBean>();

        criteriaList.add(zebrafishCriteriaBean);

        if (isAnyComparisonSpecies()) {
            addAllSpeciesCriteriaBeans(criteriaList);
            return criteriaList;
        }

        if (includeHuman && !isAnyComparisonSpecies()) {
            criteriaList.add(humanCriteriaBean);
        }

        if (includeMouse && !isAnyComparisonSpecies()) {
            criteriaList.add(mouseCriteriaBean);
        }

        if (includeFly && !isAnyComparisonSpecies()) {
            criteriaList.add(flyCriteriaBean);
        }
        return criteriaList;
    }

    private void addAllSpeciesCriteriaBeans(List<SpeciesCriteriaBean> criteriaList) {
        SpeciesCriteriaBean humanCriteria = new SpeciesCriteriaBean();
        humanCriteria.setName(Species.HUMAN.toString());
        humanCriteria.setChromosomeFilterType(FilterType.EQUALS.getName());
        humanCriteria.setGeneSymbolFilterType(FilterType.BEGINS.getName());
        criteriaList.add(humanCriteria);
        SpeciesCriteriaBean mouseCriteria = new SpeciesCriteriaBean();
        mouseCriteria.setName(Species.MOUSE.toString());
        mouseCriteria.setChromosomeFilterType(FilterType.EQUALS.getName());
        mouseCriteria.setGeneSymbolFilterType(FilterType.BEGINS.getName());
        criteriaList.add(mouseCriteria);
        SpeciesCriteriaBean flyCriteria = new SpeciesCriteriaBean();
        flyCriteria.setName(Species.FLY.toString());
        flyCriteria.setChromosomeFilterType(FilterType.EQUALS.getName());
        flyCriteria.setGeneSymbolFilterType(FilterType.BEGINS.getName());
        criteriaList.add(flyCriteria);
    }

    public ZfinCriteria getBasicCriteria() {
        ZfinCriteria criteria = new ZfinCriteria();
        criteria.setFirstRow(getFirstRecord());
        criteria.setMaxDisplayRows(getMaxDisplayRecords());
        criteria.addOrdering(ordering, true);
        criteria.setOrRelationship(isAnyComparisonSpecies());
        return criteria;
    }

    @Override
    public String toString() {
        return "OrthologySearchBean{" +
                "isSearch=" + isSearch +
                ", ordering='" + ordering + '\'' +
                ", anyComparisonSpecies=" + anyComparisonSpecies +
                ", includeHuman=" + includeHuman +
                ", includeMouse=" + includeMouse +
                ", includeFly=" + includeFly +
                ", zebrafishCriteriaBean=" + zebrafishCriteriaBean +
                ", humanCriteriaBean=" + humanCriteriaBean +
                ", mouseCriteriaBean=" + mouseCriteriaBean +
                ", flyCriteriaBean=" + flyCriteriaBean +
                ", orthologies=" + orthologies +
                '}';
    }

}
