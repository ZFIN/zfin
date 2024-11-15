package org.zfin.expression.presentation;


import lombok.Getter;
import lombok.Setter;
import org.zfin.marker.Marker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ExpressionSearchCriteria {

    private String authorField;
    private String geneField;
    private String geneZdbID;
    private Marker gene;
    private String targetGeneField;
    private boolean onlyFiguresWithImages;
    private boolean onlyWildtype;
    private boolean onlyReporter;
    private Map<String, String> stages;
    private String startStageId;
    private String endStageId;
    private String assayName;
    private String fish;
    private JournalTypeOption journalType;

    private String anatomyTermNames;
    private String anatomyTermIDs;
    private boolean includeSubstructures;

    private List<GeneResult> geneResults;
    private List<FigureResult> figureResults;
    private List<ImageResult> imageResults;

    private long numFound;
    private long pubCount;
    private Integer rows;
    private Integer page;

    private String linkWithImagesOnly;
    private boolean hasMatchingText;

    private String title;

    public List<String> getAnatomy() {
        if (anatomyTermNames == null || anatomyTermNames.equals("")) { return null; }
        return Arrays.asList(anatomyTermNames.split("\\|"));
    }

    public enum JournalTypeOption {
        DIRECT("Show only direct submission data"),
        PUBLISHED("Show only published literature"),
        ALL("Show all");

        private String label;

        JournalTypeOption(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
