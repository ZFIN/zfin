package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.Term;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains a list of ExpressionStageAnatomy objects
 * Combinations of Stage,Anatomy,Figure must be added tuples
 * and the ordering must match the order that the tuples
 * were added:
 * stage1  anat1  fig1
 * stage1  anat2  fig2
 * stage2  anat2  fig1
 * <p/>
 * will result in
 * xsa1 is [stage1 & [anat1, anat2] & [fig1, fig2]]
 * xsa2 is [stage2 & [anat2] & [fig1]]
 */
public class ExpressionStageAnatomyContainer {
    List<ExpressionStageAnatomy> xsaList;

    public List<ExpressionStageAnatomy> getXsaList() {
        return xsaList;
    }

    public void add(DevelopmentStage stage, Term anat, Figure fig) {
        if (xsaList == null)
            xsaList = new ArrayList<ExpressionStageAnatomy>();

        //will create an xsa for new stages
        ExpressionStageAnatomy xsa = getXSA(stage);

        //add the figure and anat, the xsa handles duplication internally
        xsa.addAnatomyTerm(anat);
        xsa.addFigure(fig);
    }

    private ExpressionStageAnatomy getXSA(DevelopmentStage stage) {

        //if we have the stage, return it, otherwise make a new xsa
        for (ExpressionStageAnatomy xsa : xsaList) {
            if (stage == xsa.getStage()) {
                return xsa;
            }
        }

        //since an xsa for this stage wasn't found, make one
        //and add it to the list
        ExpressionStageAnatomy xsa = new ExpressionStageAnatomy();
        xsa.setStage(stage);
        xsaList.add(xsa);
        return xsa;
    }

}
