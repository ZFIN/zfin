package org.zfin.ontology.presentation;

import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.presentation.TermPresentation;


/**
 * Presentation class for displaying phenotype_statements,
 * doesn't currently make it's own links, just aggregates term
 * links.
 */
public class PhenotypePresentation extends TermPresentation {

    private static final String PHENOTYPE_STATEMENT_SEPARATOR = " ";
    //todo: replace with non popup link
    private static final String uri = "phenotype/phenotype-statement?id=";
    private static final String popupUri = "phenotype/phenotype-statement-popup?id=";


    public static String getLink(PhenotypeStatement phenotypeStatement, boolean suppressPopupLink) {
        if (phenotypeStatement == null)
            return null;
        StringBuilder phenotypeLink = new StringBuilder(100);
        phenotypeLink.append(getNormalTagNote(phenotypeStatement));
        phenotypeLink.append(getTomcatLink(uri, String.valueOf(phenotypeStatement.getId()), 
               getNameWithoutNormalText(phenotypeStatement)));
        if (!suppressPopupLink) {
            phenotypeLink.append(getPopupLink(phenotypeStatement));
        }
        return phenotypeLink.toString();
    }

    /**
     * In the context of a phenotype statement, this name sounds a little funny,
     * but the idea is to show the names of all the entities in the correct formatting.
     *
     * That means it should look like the getLink method in terms of syntax and order,
     * and just differ in that nothing is a link.
     * 
     * @param phenotypeStatement
     * @return
     */
    public static String getName(PhenotypeStatement phenotypeStatement) {
        if (phenotypeStatement == null)
            return null;
        StringBuilder phenotypeName = new StringBuilder(100);
        phenotypeName.append(getNormalTagNote(phenotypeStatement));
        phenotypeName.append(getNameWithoutNormalText(phenotypeStatement));

        return phenotypeName.toString();
    }

    /**
     * Splitting the tag notes out from the rest of the phenotypeStatement allows one part to be linked
     * and the other not, while still keeping the display consistent between getName and getLink
     * @param phenotypeStatement
     * @return
     */
    private static String getNormalTagNote(PhenotypeStatement phenotypeStatement) {
        if (phenotypeStatement == null)
            return null;
        StringBuilder phenotypeName = new StringBuilder(100);
        if (phenotypeStatement.getTag().equals(PhenotypeStatement.Tag.NORMAL.toString()))
            phenotypeName.append("(normal&nbsp;or&nbsp;recovered) ");
        return phenotypeName.toString();
    }



    /**
     * We don't want to link the (normal or recovered) part, so this method creates
     * the bit without the normal explanation
     * @param phenotypeStatement
     * @return
     */
    private static String getNameWithoutNormalText(PhenotypeStatement phenotypeStatement) {
        if (phenotypeStatement == null)
            return null;
        StringBuilder phenotypeName = new StringBuilder(100);
                phenotypeName.append(getName(phenotypeStatement.getEntity()));
        phenotypeName.append(PHENOTYPE_STATEMENT_SEPARATOR);
        phenotypeName.append(getName(phenotypeStatement.getQuality()));
        if (phenotypeStatement.getRelatedEntity() != null) {
            phenotypeName.append(PHENOTYPE_STATEMENT_SEPARATOR);
            phenotypeName.append(getName(phenotypeStatement.getRelatedEntity()));
        }
        if (phenotypeStatement.getTag().equals(PhenotypeStatement.Tag.ABNORMAL.toString()))
            phenotypeName.append(",&nbsp;abnormal");
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
    public static String getPopupLink(PhenotypeStatement phenotypeStatement) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(getTomcatPopupLink(popupUri, String.valueOf(phenotypeStatement.getId()),
                "Phenotype definitions and synonyms" ));
        return sb.toString();

    }

}
