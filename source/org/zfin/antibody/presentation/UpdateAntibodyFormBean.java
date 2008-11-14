package org.zfin.antibody.presentation;

import org.zfin.antibody.Antibody;

/**
 * Created by IntelliJ IDEA.
 * User: Prita Mani
 * Date: Apr 17, 2008
 * Time: 4:14:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateAntibodyFormBean {
public static final String NEW_AB_NAME = "antibodyNewName";
public static final String AB_RENAMEPUB_ZDB_ID = "antibodyRenamePubZdbID";

private String antibodyRenamePubZdbID;
private String antibodyNewName;
private String antibodyRenameComments;
    
private Antibody antibody;
private boolean createAlias;

public boolean isCreateAlias() {
    return createAlias;
}

    public void setCreateAlias(boolean createAlias) {
        this.createAlias = createAlias;
    }

    public String getAntibodyRenamePubZdbID() {
        return antibodyRenamePubZdbID;
    }

    public void setAntibodyRenamePubZdbID(String antibodyRenamePubZdbID) {
        this.antibodyRenamePubZdbID = antibodyRenamePubZdbID;
    }

    public String getAntibodyNewName() {
        return antibodyNewName;
    }

    public void setAntibodyNewName(String antibodyNewName) {
        this.antibodyNewName = antibodyNewName;
    }

    public String getAntibodyRenameComments() {
        return antibodyRenameComments;
    }

    public void setAntibodyRenameComments(String antibodyRenameComments) {
        this.antibodyRenameComments = antibodyRenameComments;
    }

    public Antibody getAntibody() {
        if (antibody == null) {
            antibody = new Antibody();
        }
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }
}
