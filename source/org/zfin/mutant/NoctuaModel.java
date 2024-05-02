package org.zfin.mutant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
