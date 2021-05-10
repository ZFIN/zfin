package org.zfin.infrastructure;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * This class holds a replaced zdbID and its replacement ZDBID including the
 * old name.
 */
@Entity
@Table(name = "zdb_replaced_data")
public class ReplacementZdbID implements Serializable {

    @Id
    @Column(name = "zrepld_old_zdb_id")
    private String oldZdbID;
    @Id
    @Column(name = "zrepld_new_zdb_id")
    private String replacementZdbID;
    @Column(name = "zrepld_old_name")
    private String oldName;

    public String getOldZdbID() {
        return oldZdbID;
    }

    public void setOldZdbID(String oldZdbID) {
        this.oldZdbID = oldZdbID;
    }

    public String getReplacementZdbID() {
        return replacementZdbID;
    }

    public void setReplacementZdbID(String replacementZdbID) {
        this.replacementZdbID = replacementZdbID;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getCompositeKey() {
        return oldZdbID + replacementZdbID;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ReplacementZdbID))
            return false;

        ReplacementZdbID replacementZdbID = (ReplacementZdbID) o;
        return replacementZdbID.getCompositeKey().equals(this.getCompositeKey());
    }

    public int hashCode() {
        return getCompositeKey().hashCode();
    }

}
