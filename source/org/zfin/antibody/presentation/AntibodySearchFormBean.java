package org.zfin.antibody.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.Species;
import org.zfin.anatomy.presentation.BasicAnatomyFormBean;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.expression.Assay;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FilterType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Form bean used for the antibody search page.
 */
public class AntibodySearchFormBean extends BasicAnatomyFormBean {

    private List<Antibody> antibodies;
    private List<AntibodyService> antibodyStats;

    // Form elements
    private AntibodySearchCriteria antibodySearchCriteria;
    private String clonalType;

    public static final String ACTION = "action";
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isSearchResults() {
        return StringUtils.equals(action, Type.SEARCH.toString());
    }

    public String getClonalType() {
        return clonalType;
    }

    public void setClonalType(String clonalType) {
        this.clonalType = clonalType;
    }

    public AntibodySearchCriteria getAntibodyCriteria() {
        return antibodySearchCriteria;
    }

    public void setAntibodyCriteria(AntibodySearchCriteria antibodySearchCriteria) {
        this.antibodySearchCriteria = antibodySearchCriteria;
    }

    public List<Antibody> getAntibodies() {
        return antibodies;
    }

    public void setAntibodies(List<Antibody> antibodies) {
        this.antibodies = antibodies;
    }

    public List<AntibodyService> getAntibodyStats() {
        if (antibodyStats == null) {
            if (antibodies == null)
                return null;
            antibodyStats = new ArrayList<AntibodyService>();
            for (Antibody ab : antibodies) {
                AntibodyService service = new AntibodyService(ab);
                service.setAntibodySerachCriteria(antibodySearchCriteria);
                antibodyStats.add(service);
            }
        }

        return antibodyStats;
    }

    public void setAntibodyStats(List<AntibodyService> antibodyStats) {
        this.antibodyStats = antibodyStats;
    }

    public Map<String, String> getImmunogenOrganismList() {
        LinkedHashMap<String, String> organismList = new LinkedHashMap<String, String>();
        organismList.put("Any", "Any");
        AntibodyRepository ar = RepositoryFactory.getAntibodyRepository();
        List<Species> species = ar.getImmunogenSpeciesList();
        for (Species spec : species)
            organismList.put(spec.getCommonName(), spec.getCommonName());
        return organismList;
    }

    public Map<String, String> getAntigenOrganismList() {
        LinkedHashMap<String, String> antigenList = new LinkedHashMap<String, String>();
        antigenList.put("Any", "Any");
        AntibodyRepository ar = RepositoryFactory.getAntibodyRepository();
        List<Species> species = ar.getUsedHostSpeciesList();
        for (Species spec : species)
            antigenList.put(spec.getCommonName(), spec.getCommonName());
        return antigenList;
    }

    public Map<String, String> getAssayList() {
        LinkedHashMap<String, String> assayList = new LinkedHashMap<String, String>();
        assayList.put(Assay.ANY.getName(), Assay.ANY.getName());
        assayList.put(Assay.IMMUNOHISTOCHEMISTRY.getName(), Assay.IMMUNOHISTOCHEMISTRY.getName());
        assayList.put(Assay.WESTERN_BLOT.getName(), Assay.WESTERN_BLOT.getName());
        assayList.put(Assay.CDNA_CLONES.getName(), Assay.CDNA_CLONES.getName());
        assayList.put(Assay.OTHER.getName(), Assay.OTHER.getName());
        return assayList;
    }

    public Map<String, String> getAntibodyNameFilterTypeList() {
        LinkedHashMap<String, String> assayList = new LinkedHashMap<String, String>();
        assayList.put(FilterType.CONTAINS.getName(), FilterType.CONTAINS.getName());
        assayList.put(FilterType.BEGINS.getName(), FilterType.BEGINS.getName());
        return assayList;
    }

    public Map<String, String> getAntigenNameFilterTypeList() {
        LinkedHashMap<String, String> assayList = new LinkedHashMap<String, String>();
        assayList.put(FilterType.CONTAINS.getName(), FilterType.CONTAINS.getName());
        assayList.put(FilterType.BEGINS.getName(), FilterType.BEGINS.getName());
        return assayList;
    }

    /**
     * Accessor that returns true if any of the mathing text fields are used in the search form.
     * //ToDo: Formalize this so it is more reusalbe on other search forms.
     * @return boolean
     */
    public boolean isMatchingTextSearch() {
        return !StringUtils.isEmpty(antibodySearchCriteria.getAntigenGeneName()) ||
                !StringUtils.isEmpty(antibodySearchCriteria.getName()) ||
                antibodySearchCriteria.isAnatomyDefined();
    }

    public enum Type {
        SEARCH
    }
}
