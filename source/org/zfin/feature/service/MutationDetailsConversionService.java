package org.zfin.feature.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.zfin.feature.*;
import org.zfin.feature.presentation.MutationDetailsPresentation;
import org.zfin.sequence.ReferenceDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class MutationDetailsConversionService {

    private static final String EXON = "exon";
    private static final String INTRON = "intron";
    private static final String BASE_PAIRS = "bp";
    private static final String AMINO_ACIDS = "AA";
    private static final String STOP = "STOP";
    private static final String NET = "net";
    private static final String PLUS = "+";
    private static final String MINUS = "-";

    public MutationDetailsPresentation convert(Feature feature) {
        MutationDetailsPresentation details = new MutationDetailsPresentation();
        details.setMutationType(getMutationTypeStatement(feature));
        details.setDnaChangeStatement(getDnaMutationStatement(feature));
        details.setTranscriptChangeStatement(getTranscriptMutationStatement(feature));
        details.setProteinChangeStatement(getProteinMutationStatement(feature));
        return details;
    }

    public String getMutationTypeStatement(Feature feature) {
        if (feature == null || feature.getType() == null) {
            return "";
        }
        return feature.getType().getDisplay();
    }

    public String getDnaMutationStatement(Feature feature) {
        if (feature == null || feature.getType() == null) {
            return "";
        }
        FeatureDnaMutationDetail dnaChange = feature.getFeatureDnaMutationDetail();
        StringBuilder statement = new StringBuilder();
        switch (feature.getType()) {
            case POINT_MUTATION:
                statement.append(pointMutationStatement(dnaChange));
                break;
            case DELETION:
                statement.append(deletionStatement(dnaChange));
                break;
            case INSERTION:
                statement.append(insertionStatement(dnaChange));
                break;
            case INDEL:
                statement.append(indelStatement(dnaChange));
                break;
            case TRANSGENIC_INSERTION:
                statement.append(transgenicStatement(dnaChange));
                break;
        }
        String localization = geneLocalizationWithPreposition(dnaChange);
        if (StringUtils.isNotEmpty(localization)) {
            statement.append(" ").append(localization);
        }
        String position = positionStatement(dnaChange);
        if (StringUtils.isNotEmpty(position)) {
            statement.append(" ").append(position);
        }
        String refSeq = referenceSequenceStatement(dnaChange);
        if (StringUtils.isNotEmpty(refSeq)) {
            statement.append(" ").append(refSeq);
        }
        return statement.toString();
    }

    public String getTranscriptMutationStatement(Feature feature) {
        Set<FeatureTranscriptMutationDetail> consequences = feature.getFeatureTranscriptMutationDetailSet();
        if (consequences == null) {
            return "";
        }
        List<String> consequenceStatements = new ArrayList<>(consequences.size());
        for (FeatureTranscriptMutationDetail consequence : consequences) {
            consequenceStatements.add(transcriptConsequenceStatement(consequence));
        }
        return StringUtils.join(consequenceStatements, ", ");
    }

    public String getProteinMutationStatement(Feature feature) {
        if (feature == null) {
            return "";
        }
        FeatureProteinMutationDetail proteinConsequence = feature.getFeatureProteinMutationDetail();
        StringBuilder statement = new StringBuilder(aminoAcidChangeStatement(proteinConsequence));
        if (proteinConsequence != null && proteinConsequence.getProteinConsequence() != null) {
            if (StringUtils.isNotEmpty(statement)) {
                statement.append(" ");
            }
            statement.append(proteinConsequence.getProteinConsequence().getDisplayName());
        }
        String position = positionStatement(proteinConsequence);
        if (StringUtils.isNotEmpty(position)) {
            statement.append(" ").append(position);
        }
        String refSeq = referenceSequenceStatement(proteinConsequence);
        if (StringUtils.isNotEmpty(refSeq)) {
            statement.append(" ").append(refSeq);
        }
        return statement.toString();
    }

    private String pointMutationStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null || dnaChange.getDnaMutationTerm() == null) {
            return "";
        }
        return dnaChange.getDnaMutationTerm().getDisplayName();
    }

    private String deletionStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null) {
            return "";
        }
        return addedOrRemovedStatement(null, dnaChange.getNumberRemovedBasePair(), BASE_PAIRS);
    }

    private String insertionStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null) {
            return "";
        }
        return addedOrRemovedStatement(dnaChange.getNumberAddedBasePair(), null, BASE_PAIRS);
    }

    private String indelStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null) {
            return "";
        }
        return addedOrRemovedStatement(dnaChange.getNumberAddedBasePair(), dnaChange.getNumberRemovedBasePair(), BASE_PAIRS, true);
    }

    private String addedOrRemovedStatement(Integer added, Integer removed, String item) {
        return addedOrRemovedStatement(added, removed, item, false);
    }

    private String addedOrRemovedStatement(Integer added, Integer removed, String item, boolean isNet) {
        if (added == null && removed == null) {
            return "";
        }

        String prefix = isNet ? (NET + " ") : "";
        if (added == null) {
            return prefix + MINUS + removed + " " + item;
        }

        if (removed == null) {
            return prefix + PLUS + added + " " + item;
        }

        return PLUS + added + "/" + MINUS + removed + " " + item;
    }

    private String transgenicStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null) {
            return "";
        }

        if (dnaChange.getGeneLocalizationTerm() == null &&
                dnaChange.getExonNumber() == null &&
                dnaChange.getIntronNumber() == null &&
                dnaChange.getDnaPositionStart() == null &&
                dnaChange.getReferenceDatabase() == null) {
            return "";
        }

        return "Insertion";
    }

    /**
     * Produces a full gene localization statement based on the localization term, exon, and intron values
     * including an appropriate preposition at the beginning of the string.
     *
     * @param dnaChange the dna change
     * @return localization statement
     */
    public String geneLocalizationWithPreposition(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null) {
            return "";
        }

        GeneLocalizationTerm term = dnaChange.getGeneLocalizationTerm();
        String statement = geneLocalizationStatement(dnaChange);
        if (StringUtils.isEmpty(statement)) {
            return "";
        }

        // splice junctions are 'at', everything else is 'in'
        String preposition = "in";
        if (term != null && term.getZdbID().equals("ZDB-TERM-130401-1417")) {
            preposition = "at";
        }
        return preposition + " " + statement;
    }

    /**
     * Produces a gene localization statement based on the localization term, exon, and intron values
     * of the FeatureDnaMutationDetail object.
     *
     * @param dnaChange the dna change
     * @return localization statement
     */
    public String geneLocalizationStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null) {
            return  "";
        }
        GeneLocalizationTerm term = dnaChange.getGeneLocalizationTerm();
        if (term == null) {
            return exonOrIntronLocation(dnaChange);
        } else {
            switch (term.getZdbID()) {
                case "ZDB-TERM-130401-166":     // splice donor site
                case "ZDB-TERM-130401-167":     // splice acceptor site
                    return term.getDisplayName() + exonOrIntronLocation(dnaChange, " of ");
                case "ZDB-TERM-130401-1417":    // splice junction
                    return spliceJunctionLocation(dnaChange) + term.getDisplayName();
                default:
                    return term.getDisplayName();
            }
        }
    }

    /**
     * Return a location display string in cases where *either* an exon or intron is expected. Should
     * not be used in cases where both are needed (e.g. a splice junction).
     *
     * @param dnaChange the dna change
     * @return a string which describes the location of either an exon or intron
     */
    private String exonOrIntronLocation(FeatureDnaMutationDetail dnaChange) {
        return exonOrIntronLocation(dnaChange, "");
    }

    /**
     * Return a location display string in cases where *either* an exon or intron is expected. Should
     * not be used in cases where both are needed (e.g. a splice junction). The preposition is prepended
     * to the location string.
     *
     * @param dnaChange the dna change
     * @param preposition a string added to the beginning
     * @return a string which describes the location of either an exon or intron
     */
    private String exonOrIntronLocation(FeatureDnaMutationDetail dnaChange, String preposition) {
        if (dnaChange == null) {
            return "";
        }
        return exonOrIntronLocation(dnaChange.getExonNumber(), dnaChange.getIntronNumber(), preposition);
    }

    private String exonOrIntronLocation(FeatureTranscriptMutationDetail transcriptConsequence) {
        if (transcriptConsequence == null) {
            return "";
        }
        return exonOrIntronLocation(transcriptConsequence.getExonNumber(), transcriptConsequence.getIntronNumber(), "");
    }

    private String exonOrIntronLocation(Integer exon, Integer intron, String preposition) {
        if (exon != null) {
            return preposition + EXON + " " + exon;
        }
        if (intron != null) {
            return preposition + INTRON + " " + intron;
        }
        return "";
    }

    /**
     * Splice junction location should have both and exon and intron value. If not, this method
     * returns a blank string. When the exon number is less than or equal to the intron number, then
     * return "exon X - intron Y", otherwise it's "intron Y - exon X".
     *
     * @param dnaChange the dna change
     * @return a string which describes the location of a splice junction
     */
    private String spliceJunctionLocation(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null) {
            return "";
        }

        if (dnaChange.getExonNumber() == null || dnaChange.getIntronNumber() == null) {
            return "";
        }

        if (dnaChange.getExonNumber() <= dnaChange.getIntronNumber()) {
            return EXON + " " + dnaChange.getExonNumber() + " - " + INTRON + " " + dnaChange.getIntronNumber() + " ";
        } else {
            return INTRON + " " + dnaChange.getIntronNumber() + " - " + EXON + " " + dnaChange.getExonNumber() + " ";
        }
    }

    /**
     * Produces a position statement based on the position start and end values of the
     * FeatureDnaMutationDetail object. If the start is null, a blank string is always returned.
     * If only the start has a value, it produces a string for that position. If start and end
     * have a value, it produces a statement for the range.
     *
     * @param dnaChange the dna change
     * @return position statement
     */
    public String positionStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null) {
            return  "";
        }
        return positionStatement(dnaChange.getDnaPositionStart(), dnaChange.getDnaPositionEnd());
    }

    public String positionStatement(FeatureProteinMutationDetail proteinConsequence) {
        if (proteinConsequence == null) {
            return "";
        }
        return positionStatement(proteinConsequence.getProteinPositionStart(), proteinConsequence.getProteinPositionEnd());
    }

    private String positionStatement(Integer start, Integer end) {
        if (start == null) {
            return "";
        }

        if (end == null) {
            return "at position " + start;
        }

        return "from position " + start + " to " + end;
    }

    /**
     * Produces a statement indicating a sequence of reference in the usual DBNAME:ACCESSION format
     *
     * @param dnaChange the dna change
     * @return reference sequence statement
     */
    public String referenceSequenceStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null) {
            return "";
        }
        return referenceSequenceStatement(dnaChange.getReferenceDatabase(), dnaChange.getDnaSequenceReferenceAccessionNumber());
    }

    public String referenceSequenceStatement(FeatureProteinMutationDetail proteinConsequence) {
        if (proteinConsequence == null) {
            return "";
        }
        return referenceSequenceStatement(proteinConsequence.getReferenceDatabase(), proteinConsequence.getProteinSequenceReferenceAccessionNumber());
    }

    private String referenceSequenceStatement(ReferenceDatabase refDb, String accession) {
        if (refDb == null) {
            return "";
        }

        return "in " + refDb.getForeignDB().getDisplayName() + ":" + accession;
    }

    public String transcriptConsequenceStatement(FeatureTranscriptMutationDetail transcriptDetail) {
        if (transcriptDetail == null || transcriptDetail.getTranscriptConsequence() == null) {
            return "";
        }
        StringBuilder statement = new StringBuilder(transcriptDetail.getTranscriptConsequence().getDisplayName());
        String location = exonOrIntronLocation(transcriptDetail);
        if (StringUtils.isNotEmpty(location)) {
            statement.append(" in ").append(location);
        }
        return statement.toString();
    }

    public String aminoAcidChangeStatement(FeatureProteinMutationDetail proteinConsequence) {
        if (proteinConsequence == null) {
            return "";
        }

        StringBuilder statement = new StringBuilder();
        if (proteinConsequence.getWildtypeAminoAcid() != null) {
            statement.append(proteinConsequence.getWildtypeAminoAcid().getDisplayName()).append(">");
            if (proteinConsequence.getMutantAminoAcid() != null) {
                statement.append(proteinConsequence.getMutantAminoAcid().getDisplayName());
            } else {
                statement.append(STOP);
            }
        }

        String addedOrRemoved = addedOrRemovedStatement(proteinConsequence.getNumberAminoAcidsAdded(),
                proteinConsequence.getNumberAminoAcidsRemoved(), AMINO_ACIDS);
        if (StringUtils.isNotEmpty(addedOrRemoved)) {
            if (StringUtils.isNotEmpty(statement)) {
                statement.append(", ");
            }
            statement.append(addedOrRemoved);
        }
        return statement.toString();
    }
}
