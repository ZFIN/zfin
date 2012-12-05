package org.zfin.mutant.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.Species;
import org.zfin.anatomy.presentation.BasicAnatomyFormBean;

import org.zfin.expression.Assay;
import org.zfin.fish.WarehouseSummary;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.marker.Marker;
import org.zfin.mutant.ConstructSearchCriteria;
import org.zfin.mutant.repository.ConstructService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FilterType;
import org.zfin.ontology.Term;

import java.util.*;

/**

 * Form bean used for the construct search page.
 */
public class ConstructSearchFormBean extends PaginationBean {
    public static final String SHOW_ALL = "showAll";
    private String hasEngineeredRegion;
    private String drivesExpressionOfGene;
    private WarehouseSummary summary;


    public List<Marker> getExpressedGene() {
        return expressedGene;
    }

    public void setExpressedGene(List<Marker> expressedGene) {
        this.expressedGene = expressedGene;
    }

    public WarehouseSummary getSummary() {
        return summary;
    }

    public void setSummary(WarehouseSummary summary) {
        this.summary = summary;
    }

    private String affectedGene;
    private String construct;
    private String filter1=SHOW_ALL;

    public String getAllTypes() {
        return allTypes;
    }

    public void setAllTypes(String allTypes) {
        this.allTypes = allTypes;
    }

    private String allTypes;

    public String getAllEt() {
        return allEt;
    }

    public void setAllEt(String allEt) {
        this.allEt = allEt;
    }

    public String getAllGt() {
        return allGt;
    }

    public void setAllGt(String allGt) {
        this.allGt = allGt;
    }

    public String getAllPt() {
        return allPt;
    }

    public void setAllPt(String allPt) {
        this.allPt = allPt;
    }

    public String getAllTg() {
        return allTg;
    }

    public void setAllTg(String allTg) {
        this.allTg = allTg;
    }
    private String allEt;
    private String allGt;
    private String allPt;
    private String allTg;

    private List<Marker> expressedGene;

    public ConstructService getConstructService() {
        return constructService;
    }

    public void setConstructService(ConstructService constructService) {
        this.constructService = constructService;
    }

    private List<Construct> constructList;
    private Term term;
    private String anatomyTermNames;
    private String anatomyTermIDs;
    private Marker constructObj;

    protected ConstructService constructService;

    public Marker getConstructObj() {
        return constructObj;
    }

    public void setConstructObj(Marker constructObj) {
        this.constructObj = constructObj;
    }

    public ConstructSearchCriteria getConstructSearchCriteria() {
        return constructSearchCriteria;
    }

    public void setConstructSearchCriteria(ConstructSearchCriteria constructSearchCriteria) {
        this.constructSearchCriteria = constructSearchCriteria;
    }

    protected ConstructSearchCriteria constructSearchCriteria;

    public List<Construct> getConstructList() {
        return constructList;
    }

    public void setConstructList(List<Construct> constructList) {
        this.constructList = constructList;
    }

    private String promoterOfGene;

    public String getAffectedGene() {
        return affectedGene;
    }

    public void setAffectedGene(String affectedGene) {
        this.affectedGene = affectedGene;
    }

    public String getConstruct() {
        return construct;
    }

    public void setConstruct(String construct) {
        this.construct = construct;
    }

    public String getFilter1() {
        return filter1;
    }

    public void setFilter1(String filter1) {
        this.filter1 = filter1;
    }
    public boolean isBlOnly() {
        return StringUtils.equals(filter1, "blOnly");
    }
    public boolean isEkkerOnly() {
        return StringUtils.equals(filter1, "ekkerOnly");
    }
    public boolean isAvailableOnly() {
        return StringUtils.equals(filter1, "availableOnly");
    }
    public boolean isEtConstruct() {
        return StringUtils.equals(allEt, "etConstruct");
    }
    public boolean isPtConstruct() {
        return StringUtils.equals(allPt, "ptConstruct");
    }
    public boolean isGtConstruct() {
        return StringUtils.equals(allGt, "gtConstruct");
    }
    public boolean isTgConstruct() {
        return StringUtils.equals(allTg, "tgConstruct");
    }

    public boolean isAllConstructs(){
        return StringUtils.equals(allTypes, "allConstructs");
    }
    public String getHasEngineeredRegion() {
        return hasEngineeredRegion;
    }

    public void setHasEngineeredRegion(String hasEngineeredRegion) {
        this.hasEngineeredRegion = hasEngineeredRegion;
    }

    public String getDrivesExpressionOfGene() {
        return drivesExpressionOfGene;
    }

    public void setDrivesExpressionOfGene(String drivesExpressionOfGene) {
        this.drivesExpressionOfGene = drivesExpressionOfGene;
    }

    public String getPromoterOfGene() {
        return promoterOfGene;
    }

    public void setPromoterOfGene(String promoterOfGene) {
        this.promoterOfGene = promoterOfGene;
    }

    private int resultsPerPage;
    private boolean zircOnly;
    private PaginationBean paginationBean = new PaginationBean();


    public static final String ANY = "Any";
    // instance variable to cache the outcome



    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public void setResultsPerPage(int resultsPerPage) {
        this.resultsPerPage = resultsPerPage;
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

    public List<Integer> getRecordsPerPageList() {
        return Arrays.asList(20, 50, 100, 200);
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
}
