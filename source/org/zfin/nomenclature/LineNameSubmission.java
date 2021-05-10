package org.zfin.nomenclature;

import java.util.List;

public class LineNameSubmission extends NameSubmission {

    private List<LineInfo> lineDetails;
    private String keepPrivate;

    public List<LineInfo> getLineDetails() {
        return lineDetails;
    }

    public void setLineDetails(List<LineInfo> lineDetails) {
        this.lineDetails = lineDetails;
    }

    public String getKeepPrivate() {
        return keepPrivate;
    }

    public void setKeepPrivate(String keepPrivate) {
        this.keepPrivate = keepPrivate;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("Add data: ").append(keepPrivate).append("\n\n");
        for (int i = 0; i < lineDetails.size(); i++) {
            LineInfo info = lineDetails.get(i);
            sb.append(i + 1).append(". Genetic Background: ").append(info.getBackground()).append("\n")
                    .append(i + 1).append(". Gene/Locus/Construct Name: ").append(info.getGeneName()).append("\n")
                    .append(i + 1).append(". Gene/Locus Symbol: ").append(info.getGeneSymbol()).append("\n")
                    .append(i + 1).append(". Lab Designation: ").append(info.getDesignation()).append("\n")
                    .append(i + 1).append(". Protocol: ").append(info.getProtocol()).append("\n")
                    .append(i + 1).append(". Mutation Type: ").append(info.getMutationType()).append("\n")
                    .append(i + 1).append(". Mutation Details: ").append(info.getMutationDetails()).append("\n")
                    .append(i + 1).append(". CRISPR/TALEN Sequence: ").append(info.getSequence()).append("\n")
                    .append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getSubjectLine() {
        return "Mutant Submission: " + (lineDetails.size() > 0 ? lineDetails.get(0).getDesignation() : "");
    }
}
