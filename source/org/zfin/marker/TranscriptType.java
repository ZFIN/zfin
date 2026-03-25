package org.zfin.marker;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transcript_type")
@Setter
@Getter
public class TranscriptType implements Comparable<TranscriptType> {

    @Id
    @Column(name = "tscriptt_pk_id")
    private Long id;

    @Column(name = "tscriptt_type", nullable = false)
    @org.hibernate.annotations.Type(value = org.zfin.framework.StringEnumValueUserType.class,
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.marker.TranscriptType$Type")})
    private Type type;

    @Column(name = "tscriptt_display", nullable = false)
    private String display;

    @Column(name = "tscriptt_order", nullable = false)
    private String order;

    @Column(name = "tscriptt_definition")
    private String definition;

    @Column(name = "tscriptt_indent", nullable = false)
    private boolean isIndented;

    @Column(name = "tscriptt_so_id")
    private String soID;

    public boolean isIndented() {
        return isIndented;
    }

    public void setIndented(boolean indented) {
        isIndented = indented;
    }

    @Override
    public int compareTo(TranscriptType o) {
        return this.display.compareTo(o.getDisplay());
    }

    public enum Type {
        V_GENE("V-gene"),
        ABERRANT_PROCESSED_TRANSCRIPT("aberrant processed transcript"),
        ANTISENSE("antisense"),
        LINCRNA("lincRNA"),
        MRNA("mRNA"),
        MIRNA("miRNA"),
        NCRNA("ncRNA"),
        PIRNA("piRNA"),
        POLYCISTRONIC_TRANSCRIPT("polycistronic transcript"),
        PRE_MIRNA("pre miRNA"),
        PSEUDOGENIC_TRANSCRIPT("pseudogenic transcript"),
        RRNA("rRNA"),
        SCRNA("scRNA"),
        SNRNA("snRNA"),
        SNORNA("snoRNA"),
        TRNA("tRNA"),
        TRANSCRIPT("transcript"),
        TRANSPOSABLE_ELEMENT("transposable element"),
        DISRUPTED_DOMAIN("disrupted domain");

        private String value;

        Type(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public static Type getTranscriptType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            return null ;
        }

        public static List<TranscriptStatus.Status> getStatusList(Type transcriptType) {
            List<TranscriptStatus.Status> statuses = new ArrayList<TranscriptStatus.Status>();
            if (transcriptType == Type.MIRNA) {
                statuses.add(TranscriptStatus.Status.NONE);
                statuses.add(TranscriptStatus.Status.PUBLISHED);
                statuses.add(TranscriptStatus.Status.MICRORNA_REGISTRY);
            } else if (transcriptType == Type.PIRNA || transcriptType == Type.PRE_MIRNA || transcriptType == Type.RRNA || transcriptType == Type.SNRNA
                    || transcriptType == Type.SNORNA || transcriptType == Type.SCRNA || transcriptType == Type.TRNA) {
                statuses.add(TranscriptStatus.Status.NONE);
                statuses.add(TranscriptStatus.Status.PUBLISHED);
            } else {
                for (TranscriptStatus.Status status : TranscriptStatus.Status.values()) {
                    if (status != TranscriptStatus.Status.MICRORNA_REGISTRY) {
                        statuses.add(status);
                    }
                }
            }


            return statuses;
        }
    }


    public String toString() {
        StringBuilder sb = new StringBuilder("TranscriptType");
        sb.append("\n");
        sb.append("id: ").append(getId());
        sb.append("\n");
        sb.append("status: ").append(getType());
        sb.append("\n");
        sb.append("display: ").append(getDisplay());
        sb.append("\n");
        sb.append("order: ").append(getOrder());
        sb.append("\n");
        return sb.toString();
    }
}
