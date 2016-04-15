package org.zfin.feature.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureDnaMutationDetail;
import org.zfin.feature.presentation.MutationDetailsPresentation;
import org.zfin.ontology.GenericTerm;
import org.zfin.sequence.ReferenceDatabase;

@Service
public class MutationDetailsConversionService {

    public MutationDetailsPresentation convert(Feature feature) {
        MutationDetailsPresentation details = new MutationDetailsPresentation();
        details.setMutationType(getMutationTypeStatement(feature));
        details.setDnaChangeStatement(getDnaMutationStatement(feature));
        return details;
    }

    public String getMutationTypeStatement(Feature feature) {
        return feature.getType().getDisplay();
    }

    public String getDnaMutationStatement(Feature feature) {
        FeatureDnaMutationDetail dnaChange = feature.getFeatureDnaMutationDetail();
        StringBuilder statement = new StringBuilder();
        switch (feature.getType()) {
            case POINT_MUTATION:
                statement.append(pointMutationStatement(dnaChange));
                break;
            case DELETION:
                statement.append(deletionStatement(dnaChange));
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

    private String pointMutationStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null || dnaChange.getDnaMutationTerm() == null) {
            return "";
        }
        return dnaChange.getDnaMutationTerm().getDisplayName();
    }

    private String deletionStatement(FeatureDnaMutationDetail dnaChange) {
        if (dnaChange == null || dnaChange.getNumberRemovedBasePair() == null) {
            return "";
        }
        return "-" + dnaChange.getNumberRemovedBasePair() + " bp";
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

        GenericTerm term = dnaChange.getGeneLocalizationTerm();
        String statement = geneLocalizationStatement(dnaChange);
        if (StringUtils.isEmpty(statement)) {
            return "";
        }
        String preposition = "in";
        if (term != null && term.getOboID() != null && term.getOboID().equals("SO:0001421")) {
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
        GenericTerm term = dnaChange.getGeneLocalizationTerm();
        if (term == null || term.getOboID() == null) {
            return exonOrIntronLocation(dnaChange);
        } else {
            switch (term.getOboID()) {
                case "SO:0000163":
                    return "splice donor site" + exonOrIntronLocation(dnaChange, " of ");
                case "SO:0000164":
                    return "splice acceptor site" + exonOrIntronLocation(dnaChange, " of ");
                case "SO:0001421":
                    return spliceJunctionLocation(dnaChange) + "splice junction";
                case "SO:0000167":
                    return "promotor";
                case "SO:0000318":
                    return "start codon";
                case "SO:0000204":
                    return "5' UTR";
                case "SO:0000205":
                    return "3' UTR";
                case "SO:0000165":
                    return "enhancer";
            }
        }
        return "";
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

        if (dnaChange.getExonNumber() != null) {
            return preposition + "exon " + dnaChange.getExonNumber();
        }
        if (dnaChange.getIntronNumber() != null) {
            return preposition + "intron " + dnaChange.getIntronNumber();
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
            return "exon " + dnaChange.getExonNumber() + " - intron " + dnaChange.getIntronNumber() + " ";
        } else {
            return "intron " + dnaChange.getIntronNumber() + " - exon " + dnaChange.getExonNumber() + " ";
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

        if (dnaChange.getDnaPositionStart() == null) {
            return "";
        }

        if (dnaChange.getDnaPositionEnd() == null) {
            return "at position " + dnaChange.getDnaPositionStart();
        }

        return "from position " + dnaChange.getDnaPositionStart() + " to " + dnaChange.getDnaPositionEnd();
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

        ReferenceDatabase refDb = dnaChange.getReferenceDatabase();
        if (refDb == null) {
            return "";
        }

        return "in " + refDb.getForeignDB().getDisplayName() + ":" + dnaChange.getDnaSequenceReferenceAccessionNumber();
    }

}
