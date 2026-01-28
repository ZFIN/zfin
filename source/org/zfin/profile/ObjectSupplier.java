package org.zfin.profile;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.annotations.DiscriminatorFormula;
import org.zfin.framework.api.View;

import java.io.Serializable;

/**
 * Main domain object for lab and company info
 */
@Entity
@Table(name = "int_data_supplier")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("CASE get_obj_type(idsup_data_zdb_id) " +
        "WHEN 'ATB' THEN 'Marker' " +
        "WHEN 'BAC' THEN 'Marker' " +
        "WHEN 'CDNA' THEN 'Marker' " +
        "WHEN 'EST' THEN 'Marker' " +
        "WHEN 'FISH' THEN 'Fish  ' " +
        "WHEN 'FOSMID' THEN 'Marker' " +
        "WHEN 'PAC' THEN 'Marker' " +
        "WHEN 'GENO' THEN 'Genoty' " +
        "WHEN 'ALT' THEN 'Featur' " +
        "WHEN 'TALEN' THEN 'Marker' " +
        "WHEN 'CRISPR' THEN 'Marker' " +
        "ELSE             'other-' " +
        "END")
@IdClass(ObjectSupplierId.class)
@Setter
@Getter
public abstract class ObjectSupplier implements Serializable {

    @Id
    @Column(name = "idsup_data_zdb_id")
    protected String dataZdbID;

    @Id
    @ManyToOne(targetEntity = Organization.class)
    @JoinColumn(name = "idsup_supplier_zdb_id")
    @JsonView(View.API.class)
    protected Organization organization;

    @Column(name = "idsup_acc_num")
    protected String accNum;

    @Column(name = "idsup_avail_state")
    protected String availState;

    @JsonView(View.API.class)
    public String getOrderURL() {
        if (organization.getOrganizationOrderURL() != null && organization.getOrganizationOrderURL().getUrlPrefix() != null && accNum != null)
            return organization.getOrganizationOrderURL().getUrlPrefix() + accNum;
        return null;
    }

    public int hashCode() {
        int num = 39;
        if (organization != null)
            num += organization.hashCode();
        if (dataZdbID != null)
            num += dataZdbID.hashCode();
        return num;
    }

    /**
     * This method assumes that dataZdbID and supplierZdbID are not null.
     * Otherwise this method throws an exception.
     *
     * @param o Object
     * @return boolean
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof ObjectSupplier supplier))
            return false;

        if (organization == null)
            throw new RuntimeException("Organization is null but should not!");
        if (dataZdbID == null)
            throw new RuntimeException("dataZdbID is null but should not!");

        return dataZdbID.equals(supplier.getDataZdbID()) &&
                ObjectUtils.equals(organization, supplier.getOrganization());
    }

}