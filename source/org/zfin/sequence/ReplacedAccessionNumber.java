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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReplacedAccessionNumber that = (ReplacedAccessionNumber) o;

        if (newAccessionNumber != null ? !newAccessionNumber.equals(that.newAccessionNumber) : that.newAccessionNumber != null)
            return false;
        if (oldAccessionNumber != null ? !oldAccessionNumber.equals(that.oldAccessionNumber) : that.oldAccessionNumber != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = oldAccessionNumber != null ? oldAccessionNumber.hashCode() : 0;
        result = 31 * result + (newAccessionNumber != null ? newAccessionNumber.hashCode() : 0);
        return result;
    }
}
