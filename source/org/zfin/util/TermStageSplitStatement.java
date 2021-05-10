package org.zfin.util;

import org.apache.commons.lang3.StringUtils;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.GenericTerm;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TermStageSplitStatement {

    private String fileName;
    private int startLine = -1;
    private int endLine;
    private String errorMessage;
    private TermFigureStageRange originalTermFigureStageRange = new TermFigureStageRange();
    private GenericTerm originalTerm = new GenericTerm();
    private List<TermFigureStageRange> termFigureStageRangeList = new ArrayList<TermFigureStageRange>(2);
    private List<String> updateLineList = new ArrayList<String>(2);

    public TermStageSplitStatement(String scriptFile) {
        this.fileName = scriptFile;
    }

    public void addTermStageUpdateLine(String updateLine, int lineNumber) {
        if (startLine == -1)
            startLine = lineNumber;
        endLine = lineNumber;
        updateLineList.add(updateLine);
        parseLoadingInstruction(updateLine);
    }

    private void parseLoadingInstruction(String updateLine) {
        if (updateLine == null)
            return;
        String[] tokens = updateLine.split("\\|");
        if (tokens == null)
            throw new RuntimeException("No delimiting '|' character found");
        if (tokens.length != 4)
            throw new RuntimeException("The following format is expected: old term|figure start stage|figure end stage|new term");
        TermFigureStageRange range = new TermFigureStageRange();
        originalTerm.setOboID(tokens[0]);
        GenericTerm newTerm = new GenericTerm();
        newTerm.setOboID(tokens[3]);
        range.setSuperTerm(newTerm);
        DevelopmentStage start = new DevelopmentStage();
        start.setAbbreviation(tokens[1]);
        range.setStart(start);
        DevelopmentStage end = new DevelopmentStage();
        end.setAbbreviation(tokens[2]);
        range.setEnd(end);
        termFigureStageRangeList.add(range);
        adjustOriginalEntity(range);
    }

    /**
     * The original term figure stage is a combined one from all split statements
     *
     * @param range
     */
    private void adjustOriginalEntity(TermFigureStageRange range) {
        if (originalTermFigureStageRange.getSuperTerm() == null)
            originalTermFigureStageRange.setSuperTerm(originalTerm);
        if (originalTermFigureStageRange.getSuperTerm() != originalTerm)
            throw new RuntimeException("Original term is altered: ");
        // if the first entry use start stage otherwise do not alter
        if (originalTermFigureStageRange.getStart() == null)
            originalTermFigureStageRange.setStart(range.getStart());
        // always move end stage out with last added entry
        originalTermFigureStageRange.setEnd(range.getEnd());
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLocationInfo() {
        StringBuffer buffer = new StringBuffer(10);
        buffer.append("[");
        buffer.append(startLine);
        buffer.append(",");
        buffer.append(endLine);
        buffer.append("]");

        return buffer.toString();
    }

    public List<TermFigureStageRange> getTermFigureStageRangeList() {
        return termFigureStageRangeList;
    }

    public TermFigureStageRange getOriginalTermFigureStageRange() {
        return originalTermFigureStageRange;
    }

    @Override
    public String toString() {
        return "Original: " + originalTermFigureStageRange
                + "lines = " + updateLineList;
    }

    public String getErrorMessage(int value) {
        return StringUtils.replace(errorMessage, "$x", "" + value);
    }

    public boolean isValid() {
        return termFigureStageRangeList != null && termFigureStageRangeList.size() >= 2;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
