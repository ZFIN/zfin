package org.zfin.expression.presentation;

import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.util.NumberAwareStringComparator;

/**
 */
public class ExpressedStructurePresentation implements ProvidesLink, Comparable<ExpressedStructurePresentation> {

    private String superTermOntId ;
    private String superTermName ;
    private String subTermOntId ;
    private String subTermName ;
    private transient final static NumberAwareStringComparator comparator = new NumberAwareStringComparator();

    public String getSuperTermOntId() {
        return superTermOntId;
    }

    public void setSuperTermOntId(String superTermOntId) {
        this.superTermOntId = superTermOntId;
    }

    public String getSuperTermName() {
        return superTermName;
    }

    public void setSuperTermName(String superTermName) {
        this.superTermName = superTermName;
    }

    public String getSubTermOntId() {
        return subTermOntId;
    }

    public void setSubTermOntId(String subTermOntId) {
        this.subTermOntId = subTermOntId;
    }

    public String getSubTermName() {
        return subTermName;
    }

    public void setSubTermName(String subTermName) {
        this.subTermName = subTermName;
    }

    @Override
    public int compareTo(ExpressedStructurePresentation eep) {
        if (eep == null) return -1 ;
        return comparator.compare(getSuperTermName(), eep.getSuperTermName());
    }

    private String getSuperTermLink(){
        return "<a href='/action/ontology/term-detail?termID="+ superTermOntId + "'>" +
                superTermName +"</a>" +
                "<a class='popup-link data-popup-link' "+
                " href='/action/ontology/term-detail-popup?termID="+ superTermOntId+ "'></a>";
    }

    @Override
    public String getLink(){
        if(subTermOntId==null){
            return getSuperTermLink();
        }
        else{
            return "<a href='/action/ontology/post-composed-term-detail?"+
                    "superTermID="+ superTermOntId +
                    "&subTermID="+subTermOntId+"'>" +
                    superTermName + "&nbsp;"+ subTermName + "</a>" +
                    "<a class='popup-link data-popup-link' "+
                    " href='/action/ontology/post-composed-term-detail-popup?"+
                    "superTermID="+ superTermOntId+ "&subTermID="+subTermOntId+"'></a>";
        }
    }

    @Override
    public String getLinkWithAttribution() {
        // not really implemented for this type
        return getLink();
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLinkWithAttribution();
    }
}
