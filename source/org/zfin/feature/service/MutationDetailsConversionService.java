package org.zfin.feature.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.stereotype.Service;
import org.zfin.feature.*;
import org.zfin.feature.presentation.MutationDetailsPresentation;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.ontology.presentation.TermPresentation;
import org.zfin.sequence.ReferenceDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class MutationDetailsConversionService {

    private static final String EXON = "Exon";
    private static final String INTRON = "Intron";
    private static final String BASE_PAIRS = "bp";
    private static final String AMINO_ACIDS = "AA";
    private static final String STOP = "STOP";
    private static final String NET = "Net";
    private static final String INSERTED = "inserted";
    private static final String DELETED = "deleted";
    private static final String ADDED = "added";
    private static final String MISSING = "missing";

    public MutationDetailsPresentation convert(Feature feature) {
        return convert(feature, false);
    }

    public MutationDetailsPresentation convert(Feature feature, boolean showLink) {
        MutationDetailsPresentation details = new MutationDetailsPresentation();
        details.setMutationType(getMutationTypeStatement(feature));
        details.setDnaChangeStatement(getDnaMutationStatement(feature, showLink));
        details.setTranscriptChangeStatement(getTranscriptMutationStatement(feature, showLink));
        details.setProteinChangeStatement(getProteinMutationStatement(feature, showLink));
        return details;
    }

    public String getMutationTypeStatement(Feature feature) {
        if (feature == null || feature.getType() == null) {
            return "";
        }
        return feature.getType().getDisplay();
    }

    public String getDnaMutationStatement(Feature feature) {
        return getDnaMutationStatement(feature, false);
    }

    public String getDnaMutationStatement(Feature feature, boolean showLinks) {
        if (feature == null || feature.getType() == null) {
            return "";
        }
        FeatureDnaMutationDetail dnaChange = feature.getFeatureDnaMutationDetail();
        String change = null;
        switch (feature.getType()) {
            case POINT_MUTATION:
                change = pointMutationStatement(dnaChange);
                break;
            case DELETION:
                change = deletionStatement(dnaChange);
                break;
            case INSERTION:
                change = insertionStatement(dnaChange);
                break;
            case INDEL:
                change = indelStatement(dnaChange);
                break;
            case TRANSGENIC_INSERTION:
                change = transgenicStatement(dnaChange);
                break;
        }
        String position = positionStatement(dnaChange);
        String refSeq = referenceSequenceStatement(dnaChange);
        String localization = geneLocalizationWithPreposition(dnaChange, showLinks);
        return makeSentence(change, position, refSeq, localization);
    }

    public String getTranscriptMutationStatement(Feature feature) {
        return getTranscriptMutationStatement(feature, false);
    }

    public String getTranscriptMutationStatement(Feature feature, boolean showLinks) {
        Set<FeatureTranscriptMutationDetail> consequences = feature.getFeatureTranscriptMutationDetailSet();
        if (consequences == null) {
            return "";
        }
        List<String> consequenceStatements = new ArrayList<>(consequences.size());
        for (FeatureTranscriptMutationDetail consequence : consequences) {
            consequenceStatements.add(transcriptConsequenceStatement(consequence, showLinks));
        }
        return StringUtils.join(consequenceStatements, ", ");
    }

    public String getProteinMutationStatement(Feature feature) {
        return getProteinMutationStatement(feature, false);
    }

    public String getProteinMutationStatement(Feature feature, boolean showLinks) {
        if (feature == null) {
            return "";
        }
        FeatureProteinMutationDetail proteinConsequence = feature.getFeatureProteinMutationDetail();
        String term = null;
        if (proteinConsequence != null && proteinConsequence.getProteinConsequence() != null) {
            term = termDisplay(proteinConsequence.getProteinConsequence(), showLinks);
        }
        String change = aminoAcidChangeStatement(proteinConsequence);
        String position = positionStatement(proteinConsequence);
        String refSeq = referenceSequenceStatement(proteinConsequence);
        if (StringUtils.isNotEmpty(term) && (StringUtils.isNotEmpty(change) || StringUtils.isNotEmpty(position))) {
            term += ":";
        }
        return makeSentence(term, change, position, refSeq);
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
        return addedOrRemovedStatement(null, INSERTED, dnaChange.getNumberRemovedBasePair(), DELETED, BASE_PAIRS);
    }

    private String insertionStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null) {
            return "";
        }
        return addedOrRemovedStatement(dnaChange.getNumberAddedBasePair(), INSERTED, null, DELETED, BASE_PAIRS);
    }

    private String indelStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null) {
            return "";
        }
        return addedOrRemovedStatement(dnaChange.getNumberAddedBasePair(), INSERTED, dnaChange.getNumberRemovedBasePair(), DELETED, BASE_PAIRS, true);
    }

    private String addedOrRemovedStatement(Integer added, String addedTerm, Integer removed, String removedTerm, String item) {
        return addedOrRemovedStatement(added, addedTerm, removed, removedTerm, item, false);
    }

    private String addedOrRemovedStatement(Integer added, String addedTerm, Integer removed, String removedTerm, String item, boolean isNet) {
        if (added == null && removed == null) {
            return "";
        }

        String prefix = isNet ? NET : "";
        if (added == null) {
            return makeSentence(prefix, removed.toString(), item, removedTerm);
        }

        if (removed == null) {
            return makeSentence(prefix, added.toString(), item, addedTerm);
        }

        return makeSentence(added.toString(), item, addedTerm, "/", removed.toString(), item, removedTerm);
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

    public String geneLocalizationWithPreposition(FeatureDnaMutationDetail dnaChange) {
        return geneLocalizationWithPreposition(dnaChange, false);
    }

    /**
     * Produces a full gene localization statement based on the localization term, exon, and intron values
     * including an appropriate preposition at the beginning of the string.
     *
     * @param dnaChange the dna change
     * @return localization statement
     */
    public String geneLocalizationWithPreposition(FeatureDnaMutationDetail dnaChange, boolean showLinks) {
        if (dnaChange == null) {
            return "";
        }

        GeneLocalizationTerm term = dnaChange.getGeneLocalizationTerm();
        String statement = geneLocalizationStatement(dnaChange, showLinks);
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

    public String geneLocalizationStatement(FeatureDnaMutationDetail dnaChange) {
        return geneLocalizationStatement(dnaChange, false);
    }

    /**
     * Produces a gene localization statement based on the localization term, exon, and intron values
     * of the FeatureDnaMutationDetail object.
     *
     * @param dnaChange the dna change
     * @return localization statement
     */
    public String geneLocalizationStatement(FeatureDnaMutationDetail dnaChange, boolean showLinks) {
        if (dnaChange == null || dnaChange.getGeneLocalizationTerm() == null) {
            return  "";
        }
        GeneLocalizationTerm term = dnaChange.getGeneLocalizationTerm();
        switch (term.getZdbID()) {
            case "ZDB-TERM-130401-150":     // exon
            case "ZDB-TERM-130401-191":     // intron
                return exonOrIntronLocation(dnaChange);

            case "ZDB-TERM-130401-166":     // splice donor site
            case "ZDB-TERM-130401-167":     // splice acceptor site
                return termDisplay(term, showLinks, false) +
                        exonOrIntronLocation(dnaChange, " of ") +
                        termPopup(term, showLinks);

            case "ZDB-TERM-130401-1417":    // splice junction
                return spliceJunctionLocation(dnaChange) +
                        termDisplay(term, showLinks);

            default:
                return termDisplay(term, showLinks);
        }
    }

    /**
     * Return a location display string in cases where *either* an exon or intron is expected. Should
     * not be used in cases where both are needed (e.g. a splice junction).
     *
     * @param dnaChange the dna change
     * @return a string which describes the location of either an exon or intron
     */
    public String exonOrIntronLocation(FeatureDnaMutationDetail dnaChange) {
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

    public String exonOrIntronLocation(FeatureTranscriptMutationDetail transcriptConsequence, String preposition) {
        if (transcriptConsequence == null) {
            return "";
        }
        return exonOrIntronLocation(transcriptConsequence.getExonNumber(), transcriptConsequence.getIntronNumber(), preposition);
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

        if (end == null || start == end) {
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
        return transcriptConsequenceStatement(transcriptDetail, false);
    }

    public String transcriptConsequenceStatement(FeatureTranscriptMutationDetail transcriptDetail, boolean showLinks) {
        if (transcriptDetail == null || transcriptDetail.getTranscriptConsequence() == null) {
            return "";
        }
        String termDisplay = termDisplay(transcriptDetail.getTranscriptConsequence(), showLinks, false);
        StringBuilder statement = new StringBuilder(termDisplay);
        String preposition;
        switch (transcriptDetail.getTranscriptConsequence().getZdbID()) {
            case "ZDB-TERM-130401-1568":
            case "ZDB-TERM-130401-1567":
                preposition = " of ";
                break;
            default:
                preposition = " in ";
        }
        String location = exonOrIntronLocation(transcriptDetail, preposition);
        if (StringUtils.isNotEmpty(location)) {
            statement.append(location);
        }
        statement.append(termPopup(transcriptDetail.getTranscriptConsequence(), showLinks));
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

        String addedOrRemoved = addedOrRemovedStatement(proteinConsequence.getNumberAminoAcidsAdded(), ADDED,
                proteinConsequence.getNumberAminoAcidsRemoved(), MISSING, AMINO_ACIDS);
        if (StringUtils.isNotEmpty(addedOrRemoved)) {
            if (StringUtils.isNotEmpty(statement)) {
                statement.append(", ");
            }
            statement.append(addedOrRemoved);
        }
        return statement.toString();
    }

    private String termDisplay(MutationDetailControlledVocabularyTerm term, boolean asLink, boolean showPopup) {
        String display = WordUtils.capitalize(term.getDisplayName());
        if (asLink) {
            display = EntityPresentation.getGeneralHyperLink("/" + term.getTerm().getZdbID(), display);
            if (showPopup) {
                display += termPopup(term, asLink);
            }
        }
        return display;
    }

    private String termDisplay(MutationDetailControlledVocabularyTerm term, boolean asLink) {
        return termDisplay(term, asLink, true);
    }

    private String termPopup(MutationDetailControlledVocabularyTerm term, boolean showLink) {
        if (!showLink) {
            return "";
        }
        return TermPresentation.getPopupLink(term.getTerm());
    }

    private String makeSentence(String... phrases) {
        StringBuilder sentence = new StringBuilder();
        for (String phrase : phrases) {
            if (StringUtils.isBlank(phrase)) {
                continue;
            }
            if (StringUtils.isNotEmpty(sentence)) {
                sentence.append(" ");
            }
            sentence.append(phrase);
        }
        return sentence.toString();
    }
}
