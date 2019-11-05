package org.zfin.mapping;

import org.zfin.infrastructure.PublicationAttribution;

import java.io.Serializable;

public class VariantSequence  {
    private String zdbID;
    private String vseqDataZDB;
    private String vfsTargetSequence;
    private int vfsOffsetStart;
    private int vfsOffsetStop;
    private String vfsVariation;
    private String vfsLeftEnd;
    private String vfsRightEnd;
    private String vfsType;
    private String vfsFlankType;
    private String vfsFlankOrigin;



    public String getZdbID() {
        return zdbID;
    }

    public int getVfsOffsetStart() {
        return vfsOffsetStart;
    }

    public void setVfsOffsetStart(int vfsOffsetStart) {
        this.vfsOffsetStart = vfsOffsetStart;
    }

    public int getVfsOffsetStop() {
        return vfsOffsetStop;
    }

    public void setVfsOffsetStop(int vfsOffsetStop) {
        this.vfsOffsetStop = vfsOffsetStop;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getVseqDataZDB() {
        return vseqDataZDB;
    }

    public void setVseqDataZDB(String vseqDataZDB) {
        this.vseqDataZDB = vseqDataZDB;
    }

    public String getVfsTargetSequence() {
        return vfsTargetSequence;
    }

    public void setVfsTargetSequence(String vfsTargetSequence) {
        this.vfsTargetSequence = vfsTargetSequence;
    }



    public String getVfsVariation() {
        return vfsVariation;
    }

    public void setVfsVariation(String vfsVariation) {
        this.vfsVariation = vfsVariation;
    }

    public String getVfsLeftEnd() {
        return vfsLeftEnd;
    }

    public void setVfsLeftEnd(String vfsLeftEnd) {
        this.vfsLeftEnd = vfsLeftEnd;
    }

    public String getVfsRightEnd() {
        return vfsRightEnd;
    }

    public void setVfsRightEnd(String vfsRightEnd) {
        this.vfsRightEnd = vfsRightEnd;
    }

    public String getVfsType() {
        return vfsType;
    }

    public void setVfsType(String vfsType) {
        this.vfsType = vfsType;
    }

    public String getVfsFlankType() {
        return vfsFlankType;
    }

    public void setVfsFlankType(String vfsFlankType) {
        this.vfsFlankType = vfsFlankType;
    }

    public String getVfsFlankOrigin() {
        return vfsFlankOrigin;
    }

    public void setVfsFlankOrigin(String vfsFlankOrigin) {
        this.vfsFlankOrigin = vfsFlankOrigin;
    }
}
