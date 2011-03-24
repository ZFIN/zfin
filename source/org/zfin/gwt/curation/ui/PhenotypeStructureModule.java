package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.PhenotypeExperimentDTO;
import org.zfin.gwt.root.dto.PhenotypePileStructureDTO;
import org.zfin.gwt.root.dto.PhenotypeStatementDTO;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

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


    public void updateFigureAnnotations(List<PhenotypeExperimentDTO> figureAnnotations) {
        if (figureAnnotations == null || figureAnnotations.size() == 0){
            selectUnSelectStructuresOnPile(null);
            return;
        }

        // get the intersection of all structure, i.e. all structures common to selected mutants.
        List<PhenotypeStatementDTO> intersectionOfStructures = new ArrayList<PhenotypeStatementDTO>(5);
        // the first element goes in first
        intersectionOfStructures.addAll(figureAnnotations.get(0).getExpressedTerms());
        int index = 0;
        for (PhenotypeExperimentDTO figureAnnotation : figureAnnotations) {
            if (index++ == 0)
                continue;
            List<PhenotypeStatementDTO> overlapStructures = new ArrayList<PhenotypeStatementDTO>(4);
            // assumes that a single figure annotation cannot have duplicate records.
            for (PhenotypeStatementDTO newTermDTO : figureAnnotation.getExpressedTerms()) {
                for (PhenotypeStatementDTO termDTO : intersectionOfStructures) {
                    if (newTermDTO.equalsByNameOnly(termDTO))
                        overlapStructures.add(termDTO);
                }
            }
            intersectionOfStructures = new ArrayList<PhenotypeStatementDTO>(overlapStructures);
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
                if (!structure.getPhenotypeTerm().getEntity().getSuperTerm().getTermName().equals(StructurePile.UNSPECIFIED))
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
