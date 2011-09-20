package org.zfin.marker;

import org.apache.log4j.Logger;

public class TranscriptStatus {

    private static Logger logger = Logger.getLogger(TranscriptStatus.class) ;

    private Long id ;
    private Status status;
    private String display;
    private String order;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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

    public enum Status{
        NONE(null),
        AMBIGUOUS_ORF("ambiguous orf"),
        ARTIFACT("artifact"),
        FRAGMENTED("fragmented"),
        HAPLOTYPIC("haplotypic"),
        KNOWN("known"),
        MICRORNA_REGISTRY("microRNA registry"),
        NOVEL("novel"),
        NMD("NMD"),
        PREDICTED("predicted"),
        PROTEIN_CODING_IN_PROGRESS("protein coding in progress"),
        PUBLISHED("published"),
        PUTATIVE("putative"),
        RETAINED_INTRON("retained intron"),
        TO_BE_EXPERIMENTALLY_CONFIRMED("to be experimentally confirmed"),
        UNKNOWN("unknown"),
        WITHDRAWN_BY_SANGER("Withdrawn by Sanger"),
        VARIANT("variant")
        ;

        private String value;

        Status(String value) {
            this.value = value;
        }

        public String toString(){
            return value;
        }

        public static Status getStatus(String statusString) {
            if(statusString==null || statusString.length()==0){
                return NONE ;
            }
            for (Status status : values()) {
                logger.info("status: "+ status);
                if ( status.toString() != null &&  status.toString().equals(statusString)){
                    return status;
                }
            }
            throw new RuntimeException("No status type of string [" + statusString + "] found.");
        }
    }


    public String toString() {
        StringBuilder sb = new StringBuilder("TranscriptStatus");
        sb.append("\n");
        sb.append("id: ").append(getId());
        sb.append("\n");
        sb.append("status: ").append(getStatus());
        sb.append("\n");
        sb.append("display: ").append(getDisplay());
        sb.append("\n");
        sb.append("order: ").append(getOrder());
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TranscriptStatus)) return false;

        TranscriptStatus that = (TranscriptStatus) o;

        if ( display.equals(that.getDisplay())
             && id.equals(that.getId())
             && status.equals(that.getStatus()) )
            return true;
        else
            return false;
    }

    /**
     * Returns true if the TranscriptStatus are equal, false if they're not.
     *
     * Mostly here for null safety
     *
     * @param A Transcript Status
     * @param B Transcript Status
     * @return boolean
     */
    public static boolean equals(TranscriptStatus A, TranscriptStatus B) {
        if (A == null && B != null) return false;
        if (A != null && B == null ) return false;
        if (A == null && B == null) return true;
        return A.equals(B);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (display != null ? display.hashCode() : 0);
        result = 31 * result + (order != null ? order.hashCode() : 0);
        return result;
    }

}
