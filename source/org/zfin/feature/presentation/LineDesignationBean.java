package org.zfin.feature.presentation;

import org.apache.log4j.Logger;
import org.hibernate.validator.constraints.NotEmpty;
import org.zfin.people.LabFeaturePrefix;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;


/**
 * Bean contains info for the LineDesignationController.
 */
public class LineDesignationBean {

    private Logger logger = Logger.getLogger(LineDesignationBean.class) ;

    private List<LabFeaturePrefixRow> labFeaturePrefixRows = null ;
    private List<FeaturePrefixLight> featurePrefixLightList = null ;

    @NotEmpty(message="Lab Prefix cannot be blank")
    private String lineDesig;
    @NotEmpty(message="Lab Location cannot be blank")
    private String lineLocation;

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
                labFeaturePrefixRow.addLab(new LabEntry(labFeaturePrefix.getLab(),labFeaturePrefix.getCurrentDesignation()));
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
                            new LabEntry(labFeaturePrefix.getLab(),labFeaturePrefix.getCurrentDesignation()));
                }
            }
            if(labFeaturePrefixRow.getLocations()!=null
                    && labFeaturePrefixRow.getLocations().size()>0
                    && labFeaturePrefix.getCurrentDesignation()
                    && labFeaturePrefix.getLab().getAddress()!=null
                    && labFeaturePrefixRow.getLocations().contains(labFeaturePrefix.getLab().getAddress())){
                logger.error("multiple locations for line designation: "
                        + labFeaturePrefix.getFeaturePrefix().getPrefixString()
                        + " and lab: " + labFeaturePrefix.getLab().getName()
                );
            }
            if(labFeaturePrefix.getCurrentDesignation() && labFeaturePrefix.getLab().isActive()){
                if(labFeaturePrefix.getFeaturePrefix().getInstitute()==null){
                    logger.error("Lab has null address: "
                            + labFeaturePrefix.getLab());
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

    public String getLineDesig() {
          return lineDesig;
      }

      public void setLineDesig(String lineDesig) {
          this.lineDesig = lineDesig;
      }

      public String getLineLocation() {
          return lineLocation;
      }

      public void setLineLocation(String lineLocation) {
          this.lineLocation = lineLocation;
      }


}
