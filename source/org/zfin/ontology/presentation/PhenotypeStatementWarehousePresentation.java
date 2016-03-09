package org.zfin.ontology.presentation;

import org.zfin.mutant.PhenotypeStatementWarehouse;


/**
 * Presentation class for displaying phenotype_statements
 */
public class PhenotypeStatementWarehousePresentation extends TermPresentation {

    private static final String uri = "phenotype/statement/";
    private static final String popupUri = "phenotype/statement-popup/";

    public static String getLink(PhenotypeStatementWarehouse phenotypeStatement, boolean suppressPopupLink, boolean curationLink) {
        if (phenotypeStatement == null) {
            return null;
        }

        if (curationLink) {
            return getCurationLink(phenotypeStatement);
        }

        StringBuilder phenotypeLink = new StringBuilder(100);
        phenotypeLink.append(getTomcatLink(uri, String.valueOf(phenotypeStatement.getId()), phenotypeStatement.getShortName()));
        if (!suppressPopupLink) {
            phenotypeLink.append(getPopupLink(phenotypeStatement));
        }

        return phenotypeLink.toString();
    }

    private static String getCurationLink(PhenotypeStatementWarehouse phenotypeStatement) {
        return getWebdriverLink(CURATION_URI + "&pubcur_c_tab=PHENO&OID=",
                phenotypeStatement.getPhenotypeExperiment().getFigure().getPublication().getZdbID(),
                "edit");
    }

    /**
     * In the context of a phenotype statement, this name sounds a little funny,
     * but the idea is to show the names of all the entities in the correct formatting.
     * <p/>
     * That means it should look like the getLink method in terms of syntax and order,
     * and just differ in that nothing is a link.
     *
     * @param phenotypeStatement PhenotypeStatement
     * @return name
     */
    public static String getName(PhenotypeStatementWarehouse phenotypeStatement) {
        if (phenotypeStatement == null) {
            return null;
        }
        return phenotypeStatement.getShortName();
    }

    /**
     * The content of this link comes in via css, the javascript to turn this into a popup
     * is applied in the footer.  That means all that is happening here is a link with the
     * right class names pointing to the correct url.
     *
     * @param phenotypeStatement
     * @return
     */
    public static String getPopupLink(PhenotypeStatementWarehouse phenotypeStatement) {
        return getTomcatPopupLink(popupUri, String.valueOf(phenotypeStatement.getId()),
                "Phenotype definitions and synonyms");
    }

    public static String getTagNote(PhenotypeStatementWarehouse statement) {
        switch (statement.getTag()) {
            case "normal":
                return "The \"normal\" tag is used when the annotation of a normal phenotype is notable. " +
                        "For annotations curated prior to March 2016, it could alternatively indicate a recovered " +
                        "normal phenotype, such as that resulting from the addition of a sequence targeting reagent " +
                        "or the creation of a complex mutant genotype.";
            case "ameliorated":
                return "The \"ameliorated\" tag is used to describe a partially- or fully-rescued phenotype " +
                        "resulting from the addition of a reagent or additional mutation to a mutant genotype.";
            default:
                return "";
        }
    }

}
