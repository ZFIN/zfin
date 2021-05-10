package org.zfin.marker.presentation;

import org.zfin.sequence.DBLink;

import java.util.Collection;
import java.util.List;

/**
 */
public class ProteinDetail {

        private String upID;
        private String upLength;
    private Collection<DBLink> proteinDBLinks;





    public String getUpID() {
            return upID;
        }

        public void setUpID(String upID) {
            this.upID = upID;
        }

    public String getUpLength() {
        return upLength;
    }

    public void setUpLength(String upLength) {
        this.upLength = upLength;
    }


}