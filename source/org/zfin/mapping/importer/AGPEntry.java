package org.zfin.mapping.importer;

import javax.persistence.*;

/**
 * TODO: put this in liquibase file & rename table:
 *
 * DROP TABLE IF EXISTS "public"."temp_grcz11_agp";
 * CREATE TABLE "public"."temp_grcz11_agp" (
 *   "bin" SERIAL PRIMARY KEY,
 *   "chromosome" varchar(255) COLLATE "pg_catalog"."default",
 *   "chromosome_start" int8,
 *   "chromosome_end" int8,
 *   "ix" varchar(8),
 *   "component_type" varchar(8) COLLATE "pg_catalog"."default",
 *   "component_id" varchar(255) COLLATE "pg_catalog"."default",
 *   "fragment_start" int8,
 *   "fragment_end" int8,
 *   "strand" varchar(8) COLLATE "pg_catalog"."default",
 *   "gap_length" int8,
 *   "gap_type" varchar(255) COLLATE "pg_catalog"."default",
 *   "linkage" bool,
 *   "linkage_evidence" varchar(255) COLLATE "pg_catalog"."default"
 * )
 * ;
 *
 */


@Entity
@Table(name = "temp_grcz11_agp")
public class AGPEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int bin;

    @Column
    public String chromosome;

    @Column(name="chromosome_start")
    public int chromosomeStart;

    @Column(name = "chromosome_end")
    public int chromosomeEnd;

    @Column(name = "ix")
    public String ix;

    @Column(name = "component_type")
    public String componentType;

    @Column(name = "component_id")
    public String componentID;

    @Column(name = "fragment_start")
    public int fragmentStart;

    @Column(name = "fragment_end")
    public int fragmentEnd;

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
        this.chromosomeStart = Integer.parseInt(segs[1]);
        this.chromosomeEnd = Integer.parseInt(segs[2]);
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
                this.fragmentStart = Integer.parseInt(segs[6]);
                this.fragmentEnd = Integer.parseInt(segs[7]);
                this.isGap = false;
            }
        }
    }
    
    public String toString() {
        String buffer = "";
        buffer += this.chromosome + "\t" +
                this.chromosomeStart + "\t" +
                this.chromosomeEnd + "\t" +
                this.ix + "\t" +
                this.componentType + "\t";

        if (this.isGap) {
                buffer += this.gapLength + "\t" +
                        this.gapType + "\t" +
                        (this.linkage ? "yes" : "no") + "\t" +
                        this.linkageEvidence;
        } else {
                buffer += this.componentID + "\t" +
                        this.fragmentStart + "\t" +
                        this.fragmentEnd + "\t" +
                        (this.orientation == ORIENTATION.FWD ? "+" : "-");
        }
        return buffer;
    }

}
