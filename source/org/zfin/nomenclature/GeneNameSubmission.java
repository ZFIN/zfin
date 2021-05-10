package org.zfin.nomenclature;

import java.util.ArrayList;
import java.util.List;

public class GeneNameSubmission extends NameSubmission {

    private String geneSymbol;
    private String geneName;
    private String otherNames;
    private String genBankID;
    private String sequence;
    private String chromosome;

    private List<HomologyInfo> homologyInfoList = new ArrayList<>();

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(String otherNames) {
        this.otherNames = otherNames;
    }

    public String getGenBankID() {
        return genBankID;
    }

    public void setGenBankID(String genBankID) {
        this.genBankID = genBankID;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public List<HomologyInfo> getHomologyInfoList() {
        return homologyInfoList;
    }

    public void setHomologyInfoList(List<HomologyInfo> homologyInfoList) {
        this.homologyInfoList = homologyInfoList;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("Gene Name: ").append(geneName).append("\n")
                .append("Gene Symbol: ").append(geneSymbol).append("\n")
                .append("Gene - Other Names: ").append(otherNames).append("\n\n")
                .append("GenBank ID: ").append(genBankID).append("\n")
                .append("Sequence: ").append(sequence).append("\n")
                .append("Chromosome: ").append(chromosome).append("\n\n");
        for (int i = 0; i < homologyInfoList.size(); i++) {
            HomologyInfo info = homologyInfoList.get(i);
            sb.append("Homology ").append(i + 1).append(" Species: ").append(info.getSpecies()).append("\n")
                    .append("Homology ").append(i + 1).append(" Gene: ").append(info.getGeneSymbol()).append("\n")
                    .append("Homology ").append(i + 1).append(" Database ID: ").append(info.getDatabaseID()).append("\n\n");
        }
        return sb.toString();
    }

    @Override
    public String getSubjectLine() {
        return "Gene Submission: " + this.getGeneSymbol();
    }
}
