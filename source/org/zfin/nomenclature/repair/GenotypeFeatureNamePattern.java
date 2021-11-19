package org.zfin.nomenclature.repair;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenotypeFeatureNamePattern {
    /**
     * Parses a feature name as represented in a genotype display name. For example, "atoh1c<sup>fh367Tg/fh367Et</sup>"
     * It currently matches a large number of features, but not all. Useful for validating the ordering in genotype display name
     * @param subject
     * @return
     */
    public static GenotypeFeatureName parseFeatureName (String subject) {
        GenotypeFeatureName output = null ;

        String featureRegEx = getFeatureRegularExpression();

        Pattern p = Pattern.compile(featureRegEx);
        Matcher m = p.matcher(subject);
        if (m.matches()) {
            String geneRootName = m.group(1);
            String transGene = m.group(2);
            String geneHeterozygous = m.group(3);
            String secondGene = m.group(4);
            String secondGeneTransGene = m.group(5);
            String geneFirstAllele = m.group(6);
            String geneFirstAlleleTransGene = m.group(7);
            String geneSecondAlleleHeterozygous = m.group(8); //either "+" or null
            String geneSecondAllele = m.group(9); //could be null
            String geneSecondAlleleTransGene = m.group(10); //could be null
            output = new GenotypeFeatureName(geneRootName,
                    transGene,
                    geneHeterozygous!=null,
                    secondGene,
                    secondGeneTransGene,
                    geneFirstAllele,
                    geneSecondAllele,
                    geneFirstAlleleTransGene,
                    geneSecondAlleleTransGene,
                    geneSecondAlleleHeterozygous != null,
                    subject);
        }
        return output;
    }

    //TODO: use something like antlr to validate with a CFG?
    private static String getFeatureRegularExpression() {
        //TODO: can we make this stricter?
        String geneRootNameRegex = "[a-z\\-\\.\\d:_]+";

        String transGeneIndicatorRegex = "(?:(?:Tg)|(?:Et)|(?:Gt))";

        //TODO: can we make this stricter also?
        String geneFirstAlleleRegex = "(?:" + "([a-z\\d]*)" + "(" + transGeneIndicatorRegex + ")?" + ")";
        String geneAlleleSeparatorRegex = "\\/";
        String geneSecondAlleleRegex = "(?:" + "(\\+)" + "|" + geneFirstAlleleRegex  + ")";
        String geneBothAllelesRegex = geneFirstAlleleRegex + geneAlleleSeparatorRegex + "?" + geneSecondAlleleRegex + "?";
        String geneAllelesSuperTitleRegex = "<sup>" + geneBothAllelesRegex + "<\\/sup>";

        String secondGeneRegex = "(?:" + "(\\+)" + "|(" + geneRootNameRegex  + "))";
        String geneRegex = "^(" + geneRootNameRegex + ")" + "(" + transGeneIndicatorRegex + ")?" +
                            "(?:" + geneAlleleSeparatorRegex + "(?:" + secondGeneRegex + ")" + "(" + transGeneIndicatorRegex + ")?" + ")?" +
                            "(?:" + geneAllelesSuperTitleRegex + ")?"  + "$";
        return geneRegex;
    }

}
