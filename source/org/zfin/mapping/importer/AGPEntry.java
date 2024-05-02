package org.zfin.mapping.importer;

import jakarta.persistence.*;

/**
 * Hibernate entity for storing rows from AGP file:
 * https://www.ncbi.nlm.nih.gov/assembly/agp/AGP_Specification_v2.0/
 */
@Entity
@Table(name = "clone_agp_grcz11")
public class AGPEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int cag_pk_id;

    @Column
    public String chromosome;

    @Column(name="object_start")
    public int objectStart;

    @Column(name = "object_end")
    public int objectEnd;

    @Column(name = "ix")
    public String ix;

    @Column(name = "component_type")
    public String componentType;

    @Column(name = "component_id")
    public String componentID;

    @Column(name = "component_start")
    public int componentStart;

    @Column(name = "component_end")
    public int componentEnd;

    @Column(name = "gap_length")
    public int gapLength;

    @Column(name = "gap_type")
    public String gapType;

    @Column(name = "linkage")
    public boolean linkage;

    @Column(name = "linkage_evidence")
    public String linkageEvidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "strand")
    public ORIENTATION orientation;

    @Transient
    public boolean isGap;

    public enum ORIENTATION {
        FWD, REV
    }

    public AGPEntry(){
    }

    public AGPEntry(String agpLine){
        String[] segs = agpLine.trim().split("\t");

        this.chromosome = segs[0];
        this.objectStart = Integer.parseInt(segs[1]);
        this.objectEnd = Integer.parseInt(segs[2]);
        this.ix = segs[3];
        this.componentType = segs[4];

        switch (segs[4]) {
            case "N", "U" -> {
                this.orientation = ORIENTATION.FWD;
                this.isGap = true;
                this.gapLength = Integer.parseInt(segs[5]);
                this.gapType = segs[6];
                this.linkage = "yes".equals(segs[7]);
                this.linkageEvidence = segs[8];
            }
            default -> {
                switch (segs[8]) {
                    case "+" -> this.orientation = ORIENTATION.FWD;
                    case "-" -> this.orientation = ORIENTATION.REV;
                    default -> this.orientation = ORIENTATION.FWD;
                }
                this.componentID = segs[5];
                this.componentStart = Integer.parseInt(segs[6]);
                this.componentEnd = Integer.parseInt(segs[7]);
                this.isGap = false;
            }
        }
    }
    
    public String toString() {
        String buffer = "";
        buffer += this.chromosome + "\t" +
                this.objectStart + "\t" +
                this.objectEnd + "\t" +
                this.ix + "\t" +
                this.componentType + "\t";

        if (this.isGap) {
                buffer += this.gapLength + "\t" +
                        this.gapType + "\t" +
                        (this.linkage ? "yes" : "no") + "\t" +
                        this.linkageEvidence;
        } else {
                buffer += this.componentID + "\t" +
                        this.componentStart + "\t" +
                        this.componentEnd + "\t" +
                        (this.orientation == ORIENTATION.FWD ? "+" : "-");
        }
        return buffer;
    }

}
