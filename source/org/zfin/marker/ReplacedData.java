
package org.zfin.marker;

import org.hibernate.annotations.DiscriminatorFormula;

import javax.persistence.*;

@Entity
@Table(name = "zdb_replaced_data")
@DiscriminatorFormula("CASE get_obj_type(zrepld_new_zdb_id)" +
        "                                    WHEN 'GENE' THEN 'Marker'" +
        "                                    WHEN 'GENEP' THEN 'Marker'" +
        "                                    WHEN 'ALT' THEN 'Feature'" +
        "                                    ELSE             'Marker'" +
        "                                 END")
public class ReplacedData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zrepld_pk_id")
    private long ID;
    @Column(name = "zrepld_old_name")
    private String oldName;
    @Column(name = "zrepld_old_zdb_id")
    // Always just an old ID not being found in the database
    private String oldID;
    @Column(name = "zrepld_new_zdb_id", insertable = false, updatable = false)
    private String newID;

    public long getID() {
        return ID;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getOldID() {
        return oldID;
    }

    public void setOldID(String oldID) {
        this.oldID = oldID;
    }

    public String getNewID() {
        return newID;
    }

    public void setNewID(String newID) {
        this.newID = newID;
    }
}
