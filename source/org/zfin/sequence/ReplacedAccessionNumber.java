package org.zfin.sequence;

import java.io.Serializable;

public class ReplacedAccessionNumber implements Serializable {
     private String oldAccessionNumber;
    private String newAccessionNumber;

    public String getOldAccessionNumber() {
        return oldAccessionNumber;
    }

    public void setOldAccessionNumber(String oldAccessionNumber) {
        this.oldAccessionNumber = oldAccessionNumber;
    }

    public String getNewAccessionNumber() {
        return newAccessionNumber;
    }

    public void setNewAccessionNumber(String newAccessionNumber) {
        this.newAccessionNumber = newAccessionNumber;
    }

}
