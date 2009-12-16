package org.zfin.mutant;


public class FeatureAssay {

    private Feature featAssayFeature;
    private Mutagen mutagen;
    private String zdbID;

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

    public enum Mutagen {
        NOT_SPECIFIED("Not Specified"),
        DNA("DNA"),
        ENU("ENU"),
        TMP("TMP"),
        G_RAYS("g-rays"),
        SPONTANEOUS("spontaneous");

//        RENAMED_THROUGH_THE_NOMENCLATURE_PIPELINE("renamed through the nomenclature pipeline");

        private String value;

        Mutagen(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }

    public enum Mutagee {
        NOT_SPECIFIED("Not Specified"),
        ADULT_FEMALES("adult females"),
        ADULT_MALES("adult males"),
        EMBRYOS("embryos"),
        SPERM("sperm");

//        RENAMED_THROUGH_THE_NOMENCLATURE_PIPELINE("renamed through the nomenclature pipeline");

        private String value;

        Mutagee(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }
}
