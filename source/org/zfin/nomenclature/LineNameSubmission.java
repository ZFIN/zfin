package org.zfin.nomenclature;


import java.util.List;

public class LineNameSubmission extends NameSubmission {

    private String geneticBackground;
    private List<LineInfo> lineDetails;

    public String getGeneticBackground() {
        return geneticBackground;
    }

    public void setGeneticBackground(String geneticBackground) {
        this.geneticBackground = geneticBackground;
    }

    public List<LineInfo> getLineDetails() {
        return lineDetails;
    }

    public void setLineDetails(List<LineInfo> lineDetails) {
        this.lineDetails = lineDetails;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("Genetic Background: ").append(geneticBackground).append("\n");
        for (int i = 0; i < lineDetails.size(); i++) {
            LineInfo info = lineDetails.get(i);
            sb.append(i + 1).append(". Gene/Locus/Construct Name: ").append(info.getGeneName()).append("\n")
                    .append(i + 1).append(". Gene/Locus Symbol: ").append(info.getGeneSymbol()).append("\n")
                    .append(i + 1).append(". Lab designation: ").append(info.getDesignation()).append("\n")
                    .append(i + 1).append(". Protocol: ").append(info.getProtocol()).append("\n")
                    .append(i + 1).append(". Mutation ").append(info.getMutationType()).append("\n\n");
        }
        return sb.toString();
    }
}
