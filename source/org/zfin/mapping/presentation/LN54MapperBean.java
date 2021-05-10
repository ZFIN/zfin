package org.zfin.mapping.presentation;

import org.apache.commons.lang.StringUtils;

/**
 */
public class LN54MapperBean {
    private String name ;
    private String email ;
    private String scoringVector;
    private String markerName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getScoringVector() {
        return scoringVector;
    }

    public void setScoringVector(String scoringVector) {
        // clean the string
        scoringVector = StringUtils.deleteWhitespace(scoringVector) ;
        this.scoringVector = scoringVector;
    }

    public String getMarkerName() {
        return markerName;
    }

    public void setMarkerName(String markerName) {
        this.markerName = markerName;
    }
}
