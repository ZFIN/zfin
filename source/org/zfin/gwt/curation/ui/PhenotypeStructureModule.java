package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.PhenotypeFigureStageDTO;
import org.zfin.gwt.root.dto.PhenotypePileStructureDTO;
import org.zfin.gwt.root.dto.PhenotypeTermDTO;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * phenotype structure module that displays all structures on the pile.
 */
public class PhenotypeStructureModule extends AbstractStructureModule {

    private PileStructuresRPCAsync pileStructureRPC = PileStructuresRPC.App.getInstance();

    public PhenotypeStructureModule(String publicationID) {
        super(publicationID);
    }

    @Override
    protected void retrievePileStructures() {
        //loadingImage.setVisible(true);
        pileStructureRPC.getPhenotypePileStructures(publicationID, new RetrieveStructuresCallback());
    }


    public void updateFigureAnnotations(List<PhenotypeFigureStageDTO> figureAnnotations) {
        if (figureAnnotations == null)
            return;

        // get the intersection of all structure, i.e. all structures common to selected mutants.
        List<PhenotypeTermDTO> intersectionOfStructures = new ArrayList<PhenotypeTermDTO>(figureAnnotations.size());
        // needed to filter out the first element of the collection.
        int index = 0;
        for (PhenotypeFigureStageDTO figureAnnotation : figureAnnotations) {
            if (index == 0)
                intersectionOfStructures.addAll(figureAnnotation.getExpressedTerms());
            else {
                intersectionOfStructures = (List<PhenotypeTermDTO>) CollectionUtils.intersection(intersectionOfStructures, figureAnnotation.getExpressedTerms());
            }
            index++;
        }
        selectUnSelectStructuresOnPile(intersectionOfStructures);

    }

    private class RetrieveStructuresCallback extends ZfinAsyncCallback<List<PhenotypePileStructureDTO>> {

        public RetrieveStructuresCallback() {
            super("Error while reading Structures", null);
        }

        public void onSuccess(List<PhenotypePileStructureDTO> list) {

            displayedStructures.clear();
            for (PhenotypePileStructureDTO structure : list) {
                // do not add 'unspecified'
                if (!structure.getPhenotypeTerm().getSuperterm().getTermName().equals(StructurePile.UNSPECIFIED))
                    displayedStructures.add(structure);
            }
            displayTable.createStructureTable();
            //Window.alert("SIZE: " + experiments.size());
/*
            updateFigureAnnotations(expressionSection.getSelectedExpressions());
            loadingImage.setVisible(false);
*/
        }

        public void onFailureCleanup() {
            //loadingImage.setVisible(true);
        }
    }


}
