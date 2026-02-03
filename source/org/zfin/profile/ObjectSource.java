package org.zfin.profile;

import jakarta.persistence.*;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.annotations.DiscriminatorFormula;

import java.io.Serializable;

/**
 */
@Entity
@Table(name = "int_data_source")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("CASE get_obj_type(ids_data_zdb_id) " +
        "WHEN 'ALT' THEN 'Featur' " +
        "WHEN 'GENO' THEN 'Genoty' " +
        "WHEN 'ETCONSTRCT' THEN 'Marker' " +
        "WHEN 'EST' THEN 'Marker' " +
        "WHEN 'GTCONSTRCT' THEN 'Marker' " +
        "WHEN 'TGCONSTRCT' THEN 'Marker' " +
        "ELSE             'other-' " +
        "END")
public class ObjectSource implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ids_pk_id")
    protected long id;

    @Column(name = "ids_data_zdb_id")
    protected String dataZdbID;

    @ManyToOne
    @JoinColumn(name = "ids_source_zdb_id")
    protected Organization organization;


    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
    }


    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
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
        if (!(o instanceof ObjectSource source))
            return false;

        if (organization == null)
            throw new RuntimeException("Organization is null but should not!");
        if (dataZdbID == null)
            throw new RuntimeException("dataZdbID is null but should not!");

        return dataZdbID.equals(source.getDataZdbID()) &&
                ObjectUtils.equals(organization, source.getOrganization());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
