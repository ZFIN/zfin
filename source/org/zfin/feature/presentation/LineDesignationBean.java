package org.zfin.feature.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.profile.LabFeaturePrefix;

import java.util.ArrayList;
import java.util.List;


/**
 * Bean contains info for the LineDesignationController.
 */
public class LineDesignationBean {

    private Logger logger = LogManager.getLogger(LineDesignationBean.class) ;

    private List<LabFeaturePrefixRow> labFeaturePrefixRows = null ;
    private List<FeaturePrefixLight> featurePrefixLightList = null ;


    public List<FeaturePrefixLight> getFeaturePrefixLightList() {
        return featurePrefixLightList;
    }



    public void setFeaturePrefixLightList(List<FeaturePrefixLight> featurePrefixLightList) {
        this.featurePrefixLightList = featurePrefixLightList;
    }

    public void setFeaturePrefixList(List<LabFeaturePrefix> featurePrefixList) {
        labFeaturePrefixRows = new ArrayList<LabFeaturePrefixRow>() ;

        String lastPrefix = "";
        LabFeaturePrefixRow labFeaturePrefixRow = null ;
        for(LabFeaturePrefix labFeaturePrefix : featurePrefixList){

            // if they are the same then, just add the lab
            if(labFeaturePrefix.getFeaturePrefix().getPrefixString().equals(lastPrefix)){
                labFeaturePrefixRow.addLab(new LabEntry(labFeaturePrefix.getOrganization(),labFeaturePrefix.getCurrentDesignation()));
            }
            // if they are different, then create a new record
            else{
                if(labFeaturePrefixRow!=null){
                    labFeaturePrefixRows.add(labFeaturePrefixRow) ;
                }
                labFeaturePrefixRow = null ;
                labFeaturePrefixRow = new LabFeaturePrefixRow();
                labFeaturePrefixRow.setPrefix(labFeaturePrefix.getFeaturePrefix().getPrefixString());
                if(labFeaturePrefix.getLab().isActive()){
                    labFeaturePrefixRow.addLab(
                            new LabEntry(labFeaturePrefix.getOrganization(),labFeaturePrefix.getCurrentDesignation()));
                }
            }
            if(labFeaturePrefixRow.getLocations()!=null
                    && labFeaturePrefixRow.getLocations().size()>0
                    && labFeaturePrefix.getCurrentDesignation()
                    && labFeaturePrefix.getOrganization().getAddress()!=null
                    && labFeaturePrefixRow.getLocations().contains(labFeaturePrefix.getOrganization().getAddress())){
                logger.error("multiple locations for line designation: "
                        + labFeaturePrefix.getFeaturePrefix().getPrefixString()
                        + " and lab: " + labFeaturePrefix.getOrganization().getName()
                );
            }
            if(labFeaturePrefix.getCurrentDesignation() && labFeaturePrefix.getOrganization().isActive()){
                if(labFeaturePrefix.getFeaturePrefix().getInstitute()==null){
                    logger.error("Lab has null address: "
                            + labFeaturePrefix.getOrganization());
                }
                else{
                    labFeaturePrefixRow.addLocation(labFeaturePrefix.getFeaturePrefix().getInstitute());

                }
            }
            lastPrefix = labFeaturePrefix.getFeaturePrefix().getPrefixString();
        }

    }

    public List<LabFeaturePrefixRow> getLabFeaturePrefixRows(){
        return labFeaturePrefixRows;
    }




}
