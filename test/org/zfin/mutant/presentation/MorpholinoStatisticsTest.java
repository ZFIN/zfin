package org.zfin.mutant.presentation;

import org.junit.Before;
import org.junit.Test;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.Morpholino;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

public class MorpholinoStatisticsTest {

    private GenotypeExperiment genoxOne;
    private AnatomyItem item;

    @Test
    public void orderWithinSingleMoStatistics() {
        MorpholinoStatistics stat = new MorpholinoStatistics(genoxOne, item.createGenericTerm());
        String targetGeneOrdering = stat.getTargetGeneOrder();
        assertEquals("slit1a, slit1b", targetGeneOrdering);
    }

    @Before
    public void createFirstExperiment() {
        genoxOne = new GenotypeExperiment();
        Experiment experimentOne = new Experiment();
        experimentOne.setName("One");
        genoxOne.setExperiment(experimentOne);
        ExperimentCondition conditionTwo = new ExperimentCondition();
        Morpholino morpholinoTwo = new Morpholino();
        morpholinoTwo.setAbbreviation("MO-slit1b");
        morpholinoTwo.setZdbID("ZDB-MRPHlNO-090311-1");

        conditionTwo.setMorpholino(morpholinoTwo);

        ExperimentCondition conditionOne = new ExperimentCondition();
        Morpholino morpholinoOne = new Morpholino();
        morpholinoOne.setAbbreviation("MO-slit1a");
        morpholinoOne.setZdbID("ZDB-MRPHlNO-090311-2");
        conditionOne.setMorpholino(morpholinoOne);
        conditionTwo.setMorpholino(morpholinoTwo);

        Set<ExperimentCondition> conditions = new HashSet<ExperimentCondition>();
        conditions.add(conditionOne);
        conditions.add(conditionTwo);
        experimentOne.setExperimentConditions(conditions);

        // create markers for each MO
        createMarkerForMo("slit1a", morpholinoOne);
        createMarkerForMo("slit1b", morpholinoTwo);

        createAnatomyTerm();
    }

    private Marker createMarkerForMo(String name, Marker morpholino) {
        Marker marker = new Marker();
        marker.setAbbreviation(name);
        marker.setZdbID("ZDB-GENE-" + name);
        MarkerRelationship rel = new MarkerRelationship();
        rel.setFirstMarker(morpholino);
        rel.setSecondMarker(marker);
        Set<MarkerRelationship> rels = new HashSet<MarkerRelationship>();
        rels.add(rel);
        morpholino.setFirstMarkerRelationships(rels);
        return marker;
    }

    private void createAnatomyTerm() {
        item = new AnatomyItem();
        item.setTermName("eye");
        item.setZdbID("ZDB-ANAT-010921-532");
    }
}