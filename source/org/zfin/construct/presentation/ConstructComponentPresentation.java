package org.zfin.construct.presentation;

import org.zfin.construct.ConstructCuration;
import org.zfin.construct.ConstructComponent;
import org.zfin.gwt.root.dto.DBLinkDTO;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.marker.presentation.PreviousNameLight;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;

import java.util.List;
import java.util.Set;

/**
 * Created by Prita on 5/23/2014.
 */
public class ConstructComponentPresentation {
    private String constructDisplayName;
    private String constructComments;
    private String constructType;
    private String constructComponentCategory;
    private String constructComponentName;


    public ConstructCuration getConstruct() {
        return construct;
    }

    public void setConstruct(ConstructCuration construct) {
        this.construct = construct;
    }



    private int constructComponentOrder;
    private int constructCassetteNumber;

    public List<ConstructComponent> getConstructComponent() {
        return constructComponent;
    }

    public void setConstructComponent(List<ConstructComponent> constructComponent) {
        this.constructComponent = constructComponent;
    }

    private String constructZdbID;


    public List<PreviousNameLight> getConstructAliases() {
        return constructAliases;
    }



    public List<NoteDTO> getConstructCuratorNotes() {
        return constructCuratorNotes;
    }

    public void setConstructCuratorNotes(List<NoteDTO> constructCuratorNotes) {
        this.constructCuratorNotes = constructCuratorNotes;
    }

    public void setConstructAliases(List<PreviousNameLight> constructAliases) {
        this.constructAliases = constructAliases;
    }


    private String constructComponentType;

    public List<DBLinkDTO> getConstructSequences() {
        return constructSequences;
    }

    public int maxCassettes;
    private String curatorNotes;



    private ConstructCuration construct;


    private List<ConstructComponent> constructComponent;
    private List<NoteDTO> constructCuratorNotes;
    private List<DBLinkDTO> constructSequences;
    private List<PreviousNameLight> constructAliases;

    public void setConstructSequences(List<DBLinkDTO> constructSequences) {
        this.constructSequences = constructSequences;
    }


    public String getCuratorNotes() {
        return curatorNotes;
    }

    public void setCuratorNotes(String curatorNotes) {
        this.curatorNotes = curatorNotes;
    }

    public String getConstructAlias() {
        return constructAlias;
    }

    public void setConstructAlias(String constructAlias) {
        this.constructAlias = constructAlias;
    }

    private String constructAlias;


    public String getConstructComponentType() {
        return constructComponentType;
    }

    public void setConstructComponentType(String constructComponentType) {
        this.constructComponentType = constructComponentType;
    }

    public String getConstructDisplayName() {
        return constructDisplayName;
    }

    public void setConstructDisplayName(String constructDisplayName) {
        this.constructDisplayName = constructDisplayName;
    }

    public String getConstructComments() {
        return constructComments;
    }

    public void setConstructComments(String constructComments) {
        this.constructComments = constructComments;
    }

    public String getConstructType() {
        return constructType;
    }

    public void setConstructType(String constructType) {
        this.constructType = constructType;
    }

    public String getConstructComponentCategory() {
        return constructComponentCategory;
    }

    public void setConstructComponentCategory(String constructComponentCategory) {
        this.constructComponentCategory = constructComponentCategory;
    }

    public int getConstructComponentOrder() {
        return constructComponentOrder;
    }

    public int getMaxCassettes() {
        return maxCassettes;
    }

    public void setMaxCassettes(int maxCassettes) {
        this.maxCassettes = maxCassettes;
    }

    public void setConstructComponentOrder(int constructComponentOrder) {
        this.constructComponentOrder = constructComponentOrder;
    }

    public String getConstructComponentName() {
        return constructComponentName;
    }

    public void setConstructComponentName(String constructComponentName) {
        this.constructComponentName = constructComponentName;
    }

    public int getConstructCassetteNumber() {
        return constructCassetteNumber;
    }

    public void setConstructCassetteNumber(int constructCassetteNumber) {
        this.constructCassetteNumber = constructCassetteNumber;
    }

    public String getConstructZdbID() {
        return constructZdbID;
    }

    public void setConstructZdbID(String constructZdbID) {
        this.constructZdbID = constructZdbID;
    }
}

