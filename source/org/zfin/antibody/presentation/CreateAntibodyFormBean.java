package org.zfin.antibody.presentation;


public class CreateAntibodyFormBean {

public static final String NEW_AB_NAME = "antibodyName";
public static final String AB_PUBLICATION_ZDB_ID = "antibodyPublicationZdbID";

private String antibodyPublicationZdbID;
private String antibodyName;


public String getAntibodyName() {
    return antibodyName;
}

public void setAntibodyName(String antibodyName) {
        this.antibodyName = antibodyName;
    }

public String getAntibodyPublicationZdbID() {
        return antibodyPublicationZdbID;
    }

public void setAntibodyPublicationZdbID(String antibodyPublicationZdbID) {
        this.antibodyPublicationZdbID = antibodyPublicationZdbID;
    }

    


}







