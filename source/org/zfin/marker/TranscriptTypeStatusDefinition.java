package org.zfin.marker;

public class TranscriptTypeStatusDefinition {

    private Long id ;
    private TranscriptType type;
    private TranscriptStatus status ;
    private String definition;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TranscriptType getType() {
        return type;
    }

    public void setType(TranscriptType type) {
        this.type = type;
    }

    public TranscriptStatus getStatus() {
        return status;
    }

    public void setStatus(TranscriptStatus status) {
        this.status = status;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public enum Type {
        ANTISENSE("antisense"),
        V_GENE("V-gene"),
        ABBERANT_PROCESSED_TRANSCRIPT("abberant processed transcript"),
        PSEUDOGENIC_TRANSCRIPT("pseudogenic transcript"),
        NCRNA("ncRNA"),
        MRNA("mRNA"),
        MIRNA("miRNA"),
        TRANSPOSABLE_ELEMENT("transposable element"),
        TRANSCRIPT("transcript")
        ;

        private String value;

        Type(String value) {
            this.value = value;
        }

        public String toString(){
            return value;
        }

        public static Type getTranscriptType(String type) {
            for (Type t : TranscriptTypeStatusDefinition.Type.values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No transcript type of string " + type + " found.");
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("TranscriptType");
        sb.append("\n");
        sb.append("id: ").append(getId());
        sb.append("\n");
        sb.append("status: ").append(getType());
        sb.append("\n");
        sb.append("display: ").append(getDefinition());
        sb.append("\n");
        return sb.toString();
    }
}