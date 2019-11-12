package org.zfin.marker;


import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;

public class SequenceFeature {

    @JsonView(View.API.class)
    public String zdbID;

    public String getNameOrder() {
        return nameOrder;
    }

    public void setNameOrder(String nameOrder) {
        this.nameOrder = nameOrder;
    }

    public String nameOrder;


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name;
}
