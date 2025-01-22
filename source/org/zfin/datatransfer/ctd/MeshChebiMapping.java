package org.zfin.datatransfer.ctd;

import lombok.Data;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.zfin.framework.VocabularyTerm;

import jakarta.persistence.*;

@Entity
@Table(name = "mesh_chebi_mapping")
@Data
public class MeshChebiMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mcm_id")
    private long id;
    @Column(name = "mcm_mesh_id")
    private String meshID;
    @Column(name = "mcm_mesh_name")
    private String meshName;
    @Column(name = "mcm_chebi_id")
    private String chebiID;

    @Column(name = "mcm_cas_id")
    private String casID;

    @Column(name = "mcm_cas_name")
    private String casName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mcm_predicate_id")
    private VocabularyTerm predicate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mcm_mapping_justification_id")
    private VocabularyTerm mappingJustification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mcm_inference_method_id")
    private VocabularyTerm inferenceMethod;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeshChebiMapping that = (MeshChebiMapping) o;
        return new EqualsBuilder().append(getMeshID(), that.getMeshID()).append(getChebiID(), that.getChebiID()).append(getCasID(), that.getCasID()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getMeshID()).append(getChebiID()).append(getCasID()).toHashCode();
    }
}
