package org.zfin.feature;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.Mutagee;
import org.zfin.gwt.root.dto.Mutagen;

import javax.persistence.*;

@Entity
@Table(name = "feature_assay")
public class FeatureAssay implements IsSerializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "featassay_pk_id")
    private int pkid;
    @Column(name = "featassay_mutagen")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.gwt.root.dto.Mutagen")})
    private Mutagen mutagen;
    @Column(name = "featassay_mutagee")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.gwt.root.dto.Mutagee")})
    private Mutagee mutagee;
    @ManyToOne
    @JoinColumn(name = "featassay_feature_zdb_id")
    private Feature feature;

    public int getPkid() {
        return pkid;
    }

    public void setPkid(int pkid) {
        this.pkid = pkid;
    }

    public Mutagen getMutagen() {
        return mutagen;
    }

    public void setMutagen(Mutagen mutagen) {
        this.mutagen = mutagen;
    }

    public Mutagee getMutagee() {
        return mutagee;
    }

    public void setMutagee(Mutagee mutagee) {
        this.mutagee = mutagee;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
}
