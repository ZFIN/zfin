package org.zfin.marker.presentation;

import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.MarkerAlias;

import java.util.ArrayList;
import java.util.Collection;

public class MarkerAliasBean {

    private String zdbID;
    private String alias;
    private Collection<MarkerReferenceBean> references;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Collection<MarkerReferenceBean> getReferences() {
        return references;
    }

    public void setReferences(Collection<MarkerReferenceBean> references) {
        this.references = references;
    }

    public static MarkerAliasBean convert(MarkerAlias alias) {
        MarkerAliasBean bean = new MarkerAliasBean();
        bean.setZdbID(alias.getZdbID());
        bean.setAlias(alias.getAlias());
        Collection<MarkerReferenceBean> references = new ArrayList<>();
        for (PublicationAttribution reference : alias.getPublications()) {
            MarkerReferenceBean referenceBean = new MarkerReferenceBean();
            referenceBean.setZdbID(reference.getSourceZdbID());
            referenceBean.setTitle(reference.getPublication().getTitle());
            references.add(referenceBean);
        }
        bean.setReferences(references);
        return bean;
    }
}
