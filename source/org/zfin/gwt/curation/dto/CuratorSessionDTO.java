package org.zfin.gwt.curation.dto;

import java.io.Serializable;

//import java.io.Serializable;

/**
 * GWT data transfer object corresponding to {@link org.zfin.people.CuratorSession}.
 */
public class CuratorSessionDTO implements Serializable {

    private String publicationZdbID;
    private String curatorZdbID;

    // these are inherited
    protected String field;
    protected String value;

    public String getPublicationZdbID() {
        return publicationZdbID;
    }

    public void setPublicationZdbID(String publicationZdbID) {
        this.publicationZdbID = publicationZdbID;
    }

    public String getCuratorZdbID() {
        return curatorZdbID;
    }

    public void setCuratorZdbID(String curatorZdbID) {
        this.curatorZdbID = curatorZdbID;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
