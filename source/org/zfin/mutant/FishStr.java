package org.zfin.mutant;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Convenience Class to map many-to-many table
 */
@Entity
@Table(name = "fish_str")
public class FishStr implements Serializable {

    @Id
    @Column(name = "fishstr_fish_zdb_id")
    private String fishID;
    @Id
    @Column(name = "fishstr_str_zdb_id")
    private String strID;

    public String getFishID() {
        return fishID;
    }

    public void setFishID(String fishID) {
        this.fishID = fishID;
    }

    public String getStrID() {
        return strID;
    }

    public void setStrID(String strID) {
        this.strID = strID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FishStr fishStr = (FishStr) o;

        if (fishID != null ? !fishID.equals(fishStr.fishID) : fishStr.fishID != null) return false;
        return !(strID != null ? !strID.equals(fishStr.strID) : fishStr.strID != null);

    }

    @Override
    public int hashCode() {
        int result = fishID != null ? fishID.hashCode() : 0;
        result = 31 * result + (strID != null ? strID.hashCode() : 0);
        return result;
    }
}
