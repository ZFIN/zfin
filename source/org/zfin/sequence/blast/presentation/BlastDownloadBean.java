package org.zfin.sequence.blast.presentation;

/**
 */
public class BlastDownloadBean {

    public enum Action {
        MORPHOLINO,
        GENBANK_ALL,
        GENBANK_CDNA,
        GENBANK_XPAT_CDNA,
        GENOMIC_REFSEQ,
        GENOMIC_GENBANK,
    }


    private Action action ;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
