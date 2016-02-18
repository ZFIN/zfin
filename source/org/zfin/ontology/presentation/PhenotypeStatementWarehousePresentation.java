package org.zfin.ontology.presentation;

import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.PhenotypeStatementWarehouse;


/**
 * Presentation class for displaying phenotype_statements,
 * doesn't currently make it's own links, just aggregates term
 * links.
 */
public class PhenotypeStatementWarehousePresentation extends TermPresentation {

    private static final String PHENOTYPE_STATEMENT_SEPARATOR = " ";
    private static final String COMMA = ", ";
    //todo: replace with non popup link
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
        phenotypeLink.append(getNormalTagNote(phenotypeStatement));
        phenotypeLink.append(getTomcatLink(uri, String.valueOf(phenotypeStatement.getId()), getNameWithoutNormalText(phenotypeStatement)));
        if (!suppressPopupLink) {
            phenotypeLink.append(getPopupLink(phenotypeStatement));
        }

        return phenotypeLink.toString();
    }

    private static String getCurationLink(PhenotypeStatementWarehouse phenotypeStatement) {
        return getWebdriverLink(CURATION_URI + "&pubcur_c_tab=PHENO&OID=", phenotypeStatement.getPhenotypeExperiment().getFigure().getPublication().getZdbID(),
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
        StringBuilder phenotypeName = new StringBuilder(100);
        phenotypeName.append(getNormalTagNote(phenotypeStatement));
        phenotypeName.append(getNameWithoutNormalText(phenotypeStatement));

        return phenotypeName.toString();
    }

    /**
     * Splitting the tag notes out from the rest of the phenotypeStatement allows one part to be linked
     * and the other not, while still keeping the display consistent between getName and getLink
     *
     * @param phenotypeStatement
     * @return
     */
    private static String getNormalTagNote(PhenotypeStatementWarehouse phenotypeStatement) {
        if (phenotypeStatement == null) {
            return null;
        }
        StringBuilder phenotypeName = new StringBuilder(100);
        if (phenotypeStatement.getTag().equals(PhenotypeStatement.Tag.NORMAL.toString())) {
            phenotypeName.append("(normal&nbsp;or&nbsp;recovered) ");
        }
        return phenotypeName.toString();
    }


    /**
     * We don't want to link the (normal or recovered) part, so this method creates
     * the bit without the normal explanation
     *
     * @param phenotypeStatement
     * @return
     */
    public static String getNameWithoutNormalText(PhenotypeStatementWarehouse phenotypeStatement) {
        if (phenotypeStatement == null) {
            return null;
        }
        StringBuilder phenotypeName = new StringBuilder(100);
        if (phenotypeStatement.isMorphologicalPhenotype()) {
            phenotypeName.append(getName(phenotypeStatement.getEntity()));
            phenotypeName.append(PHENOTYPE_STATEMENT_SEPARATOR);
            phenotypeName.append(getName(phenotypeStatement.getQuality()));
            if (phenotypeStatement.getRelatedEntity() != null) {
                phenotypeName.append(PHENOTYPE_STATEMENT_SEPARATOR);
                phenotypeName.append(getName(phenotypeStatement.getRelatedEntity()));
            }
        } else {
            phenotypeName.append(getName(phenotypeStatement.getEntity()));
            phenotypeName.append(PHENOTYPE_STATEMENT_SEPARATOR);
            phenotypeName.append(MarkerPresentation.getAbbreviation(phenotypeStatement.getGene()));
            phenotypeName.append(PHENOTYPE_STATEMENT_SEPARATOR);
            phenotypeName.append(phenotypeStatement.getMarkerRelationship());
            phenotypeName.append(COMMA);
            phenotypeName.append(phenotypeStatement.getQuality().getTermName());
        }
        if (phenotypeStatement.getTag().equals(PhenotypeStatement.Tag.ABNORMAL.toString())) {
            phenotypeName.append(COMMA);
            phenotypeName.append("abnormal");
        }
        return phenotypeName.toString();
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
        StringBuilder sb = new StringBuilder(100);
        sb.append(getTomcatPopupLink(popupUri, String.valueOf(phenotypeStatement.getId()),
                "Phenotype definitions and synonyms"));
        return sb.toString();

    }

}
