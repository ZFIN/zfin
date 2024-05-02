package org.zfin.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * This class holds a replaced zdbID and its replacement ZDBID including the
 * old name.
 */
@Entity
@Table(name = "withdrawn_data")
public class WithdrawnZdbID implements Serializable {

    @Id
    @Column(name = "wd_old_zdb_id")
    private String wdoldZdbID;
    @Id
    @Column(name = "wd_new_zdb_id")
    private String wdnewZdbID;

    public String getWdoldZdbID() {
        return wdoldZdbID;
    }

    public void setWdoldZdbID(String wdoldZdbID) {
        this.wdoldZdbID = wdoldZdbID;
    }

    public String getWdnewZdbID() {
        return wdnewZdbID;
    }

    public void setWdnewZdbID(String wdnewZdbID) {
        this.wdnewZdbID = wdnewZdbID;
    }

    public String getCompositeKey() {
        return wdoldZdbID + wdnewZdbID;
    }

    public boolean equals(Object o) {
        if (!(o instanceof WithdrawnZdbID wdnewZdbID))
            return false;

        return wdnewZdbID.getCompositeKey().equals(this.getCompositeKey());
    }

    public int hashCode() {
        return getCompositeKey().hashCode();
    }

}
