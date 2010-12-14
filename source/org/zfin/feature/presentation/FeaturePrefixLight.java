package org.zfin.feature.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class FeaturePrefixLight {

    private String prefix ;
    private String instituteDisplay ;
    private List<LabLight> labList;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getInstituteDisplay() {
        return instituteDisplay;
    }

    public void setInstituteDisplay(String instituteDisplay) {
        this.instituteDisplay = instituteDisplay;
    }

    public List<LabLight> getLabList() {
        return labList;
    }

    public void addLabLight(LabLight lab){
        if(this.labList==null){
            this.labList = new ArrayList<LabLight>() ;
        }
        this.labList.add(lab) ;
    }

}
