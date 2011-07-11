package org.zfin.mutant.presentation;

/**
 */
public class PostComposedPresentationBean implements Comparable<PostComposedPresentationBean>{

    private String superTermName;
    private String superTermZdbId;
    private String subTermZdbId;
    private String superOntologyId;
    private String subOntologyId;
    private String subTermName;

    public String getSuperTermName() {
        return superTermName;
    }

    public void setSuperTermName(String superTermName) {
        this.superTermName = superTermName;
    }

    @Override
    public int compareTo(PostComposedPresentationBean postComposedPresentationBean) {
        int compare = superTermName.compareToIgnoreCase(postComposedPresentationBean.getSuperTermName());
        if(compare!=0) return compare ;

        if(subTermName==null && postComposedPresentationBean.getSubTermName()==null) return 0 ;
        if(subTermName==null && postComposedPresentationBean.getSubTermName()!=null) return 1 ;
        if(subTermName!=null && postComposedPresentationBean.getSubTermName()==null) return -1 ;

        return subTermName.compareToIgnoreCase(postComposedPresentationBean.getSubTermName());

    }

    public void setSuperTermZdbId(String superTermZdbId) {
        this.superTermZdbId = superTermZdbId;
    }

    public void setSubTermZdbId(String subTermZdbId) {
        this.subTermZdbId = subTermZdbId;
    }

    public String getSuperTermZdbId() {
        return superTermZdbId;
    }

    public String getSubTermZdbId() {
        return subTermZdbId;
    }

    public String getSuperOntologyId() {
        return superOntologyId;
    }

    public String getSubOntologyId() {
        return subOntologyId;
    }

    public void setSuperOntologyId(String superOntologyId) {
        this.superOntologyId = superOntologyId;
    }

    public void setSubOntologyId(String subOntologyId) {
        this.subOntologyId = subOntologyId;
    }

    public void setSubTermName(String subTermName) {
        this.subTermName = subTermName;
    }

    public String getSubTermName() {
        return subTermName;
    }
}
