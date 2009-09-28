package org.zfin.marker;

import java.util.List;
import java.util.ArrayList;

public class TranscriptType {

    private Long id ;
    private Type type;
    private String display;
    private String order;
    private String definition;
    private boolean isIndented;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public boolean isIndented() {
        return isIndented;
    }

    public void setIndented(boolean indented) {
        isIndented = indented;
    }

    public enum Type {
        V_GENE("V-gene"),
        ABERANT_PROCESSED_TRANSCRIPT("aberant processed transcript"),
        ANTISENSE("antisense"),
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

        public String toString(){
            return value;
        }

        public static Type getTranscriptType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No transcript type of string " + type + " found.");
        }

        public static List<TranscriptStatus.Status> getStatusList(Type transcriptType){
            List<TranscriptStatus.Status> statuses = new ArrayList<TranscriptStatus.Status>() ;
            if(transcriptType==Type.MIRNA){
                statuses.add(TranscriptStatus.Status.NONE) ;
                statuses.add(TranscriptStatus.Status.PUBLISHED) ;
                statuses.add(TranscriptStatus.Status.MICRORNA_REGISTRY) ;
            }
            else
            if(transcriptType==Type.PIRNA || transcriptType==Type.PRE_MIRNA || transcriptType== Type.RRNA || transcriptType==Type.SNRNA
                    || transcriptType==Type.SNORNA || transcriptType==Type.SCRNA || transcriptType==Type.TRNA){
                statuses.add(TranscriptStatus.Status.NONE) ;
                statuses.add(TranscriptStatus.Status.PUBLISHED) ;
            }
            else{
                for(TranscriptStatus.Status status: TranscriptStatus.Status.values()){
                    if(status!=TranscriptStatus.Status.MICRORNA_REGISTRY){
                        statuses.add(status) ;
                    }
                }
            }


            return statuses ;
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
