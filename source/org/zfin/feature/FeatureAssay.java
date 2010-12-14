package org.zfin.feature;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.Mutagee;
import org.zfin.gwt.root.dto.Mutagen;


public class FeatureAssay implements IsSerializable {

    private Feature featAssayFeature;
    private Mutagen mutagen;
    private String featzdbID;

    public String getFeatzdbID() {
        return featzdbID;
    }

    public void setFeatzdbID(String featzdbID) {
        this.featzdbID = featzdbID;
    }

    public int getPkid() {
        return pkid;
    }

    public void setPkid(int pkid) {
        this.pkid = pkid;
    }

    private int pkid;


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

    private Mutagee mutagee;

    public Feature getFeatAssayFeature() {
        return featAssayFeature;
    }

    public void setFeatAssayFeature(Feature featAssayFeature) {
        this.featAssayFeature = featAssayFeature;
    }

}
