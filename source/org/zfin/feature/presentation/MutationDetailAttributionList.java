package org.zfin.feature.presentation;

import org.zfin.feature.Feature;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListBean;

import java.util.Set;

public class MutationDetailAttributionList extends PublicationListBean {

    private Feature feature;
    private Set<Publication> publications;
    private Type type;

    public MutationDetailAttributionList() {
    }

    public MutationDetailAttributionList(Feature feature, Type type) {
        this.feature = feature;
        this.type = type;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setPublications(Set<Publication> attributions) {
        this.publications = attributions;
    }

    @Override
    public Set<Publication> getPublications() {
        return publications;
    }

    public enum Type {
        DNA("dna"),
        TRANSCRIPT("transcript"),
        PROTEIN("protein");

        private String value;

        Type(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static Type fromString(String value) {
            for (Type type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return null;
        }
    }
}
