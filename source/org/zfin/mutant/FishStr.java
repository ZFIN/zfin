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
@Table(name = "FISH_STR")
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
}
