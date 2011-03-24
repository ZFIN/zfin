package org.zfin.anatomy.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.antibody.presentation.AntibodySearchFormBean;
import org.zfin.expression.ExpressionResult;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.PhenotypeStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class ExpressionPhenotypeReportBean extends PaginationBean {

    private String anatomyTermNames;
    private String anatomyTermIDs;
    private String goTermNames;
    private String goTermIDs;
    private boolean includeSubstructures;
    private boolean includeSubstructuresGo;
    public static final String ACTION = "action";
    private String action;
    private boolean expressionReport;

    // submission variable
    private List<ExpressionResult> allExpressions = new ArrayList<ExpressionResult>();
    private List<PhenotypeStatement> allPhenotype = new ArrayList<PhenotypeStatement>();
    private List<MarkerGoTermEvidence> allGoEvidences = new ArrayList<MarkerGoTermEvidence>();

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

    public boolean isIncludeSubstructures() {
        return includeSubstructures;
    }

    public boolean isIncludeSubstructuresGo() {
        return includeSubstructuresGo;
    }

    public void setIncludeSubstructuresGo(boolean includeSubstructuresGo) {
        this.includeSubstructuresGo = includeSubstructuresGo;
    }

    public void setIncludeSubstructures(boolean includeSubstructures) {
        this.includeSubstructures = includeSubstructures;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isSearchResults() {
        return StringUtils.equals(action, AntibodySearchFormBean.Type.SEARCH.toString());
    }

    /**
     * This parses the entries from a multi-ao term submission text area field.
     *
     * @return string array
     */
    public String[] getTermIDs() {
        if (StringUtils.isEmpty(anatomyTermIDs) && StringUtils.isEmpty(goTermIDs))
            return null;
        String[] aoIds = getTermIdsFromSingleList(anatomyTermIDs);
        String[] goIds = getTermIdsFromSingleList(goTermIDs);
        int numOfTerms = 0;
        if (aoIds != null)
            numOfTerms = aoIds.length;
        if (goIds != null)
            numOfTerms += goIds.length;
        List<String> ids = new ArrayList<String>(numOfTerms);
        if (aoIds != null) {
            for (String aoID : aoIds) {
                if (StringUtils.isNotEmpty(aoID))
                    ids.add(aoID);
            }
        }
        if (goIds != null) {
            for (String goID : goIds) {
                if (StringUtils.isNotEmpty(goID))
                    ids.add(goID);
            }
        }
        String[] allTermIds = new String[ids.size()];
        allTermIds = ids.toArray(allTermIds);

        return allTermIds;

    }

    private String[] getTermIdsFromSingleList(String termList) {
        if (termList == null)
            return null;

        String[] array;
        array = termList.split(",");

        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

    public List<ExpressionResult> getAllExpressions() {
        return allExpressions;
    }

    public void setAllExpressions(List<ExpressionResult> allExpressions) {
        this.allExpressions = allExpressions;
        setTotalRecords(allExpressions.size());
    }

    public List<PhenotypeStatement> getAllPhenotype() {
        return allPhenotype;
    }

    public void setAllPhenotype(List<PhenotypeStatement> allPhenotype) {
        this.allPhenotype = allPhenotype;
        setTotalRecords(allPhenotype.size());
    }

    public List<MarkerGoTermEvidence> getAllGoEvidences() {
        return allGoEvidences;
    }

    public void setAllGoEvidences(List<MarkerGoTermEvidence> allGoEvidences) {
        this.allGoEvidences = allGoEvidences;
        setTotalRecords(allGoEvidences.size());
    }

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

    public boolean isExpressionReport() {
        return expressionReport;
    }

    public void setExpressionReport(boolean expressionReport) {
        this.expressionReport = expressionReport;
    }
}
