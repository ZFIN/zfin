package org.zfin.infrastructure;

import org.zfin.profile.Lab;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity
@DiscriminatorValue("Lab    ")
public class LabAttribution extends RecordAttribution implements Serializable, Comparable<LabAttribution> {

    @ManyToOne
    @JoinColumn(name = "recattrib_source_zdb_id", insertable = false, updatable = false)
    private Lab lab;

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
        setSourceZdbID(lab.getZdbID());
    }

    /**
     * Compare by lab object.
     *
     * @param labAttribution LabAttribution
     * @return comparison integer
     */
    @Override
    public int compareTo(LabAttribution labAttribution) {
        if (labAttribution == null)
            return -1;
        return lab.compareTo(labAttribution.getLab());
    }
}
