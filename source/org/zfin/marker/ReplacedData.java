
package org.zfin.marker;

import org.hibernate.annotations.DiscriminatorFormula;

import javax.persistence.Entity;
import javax.persistence.Table;

//@Entity
@Table(name = "zdb_replaced_data")
@DiscriminatorFormula("CASE get_obj_type(zrepld_new_zdb_id)" +
        "                                    WHEN 'GENE' THEN 'Marker'" +
        "                                    WHEN 'GENEP' THEN 'Marker'" +
        "                                    ELSE             'Marker'" +
        "                                 END")
public class ReplacedData {

    private String oldName;
    private String oldID;
    private String newID;

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
