package org.zfin.feature;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "dna_mutation_term")
public class DnaMutationTerm implements Serializable {

    @Id
    @Column(name = "dmt_term_zdb_id")
    private String dnaMutationTermId;
    @Column(name = "dmt_term_display_name")
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDnaMutationTermId() {
        return dnaMutationTermId;
    }

    public void setDnaMutationTermId(String dnaMutationTermId) {
        this.dnaMutationTermId = dnaMutationTermId;
    }
}
