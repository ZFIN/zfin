package org.zfin.feature.presentation;

public class MutationDetailsPresentation {

    private String dnaChangeStatement;
    private String transcriptChangeStatement;
    private String proteinChangeStatement;

    public String getDnaChangeStatement() {
        return dnaChangeStatement;
    }

    public void setDnaChangeStatement(String dnaChangeStatement) {
        this.dnaChangeStatement = dnaChangeStatement;
    }

    public String getTranscriptChangeStatement() {
        return transcriptChangeStatement;
    }

    public void setTranscriptChangeStatement(String transcriptChangeStatement) {
        this.transcriptChangeStatement = transcriptChangeStatement;
    }

    public String getProteinChangeStatement() {
        return proteinChangeStatement;
    }

    public void setProteinChangeStatement(String proteinChangeStatement) {
        this.proteinChangeStatement = proteinChangeStatement;
    }
}
