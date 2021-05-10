package org.zfin.mutant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "noctua_model")
public class NoctuaModel {

    public NoctuaModel(String id) {
        this.id = id;
    }

    public NoctuaModel() {
    }

    @Id
    @Column(name = "nm_id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
