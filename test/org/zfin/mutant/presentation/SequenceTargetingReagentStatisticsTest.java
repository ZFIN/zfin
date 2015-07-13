package org.zfin.mutant.presentation;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SequenceTargetingReagentStatisticsTest {

    private FishExperiment genoxOne;
    private GenericTerm item;

    @Test
    @Ignore
    public void orderWithinSingleMoStatistics() {
        SequenceTargetingReagentStatistics stat = new SequenceTargetingReagentStatistics(genoxOne, item);
        String targetGeneOrdering = stat.getTargetGeneOrder();
        assertEquals("slit1a, slit1b", targetGeneOrdering);
    }

    @Before
    public void createFirstExperiment() {
        Fish fish = new Fish();
        fish.setZdbID("ZDB-FISH-150701-1");
        genoxOne = new FishExperiment();
        Experiment experimentOne = new Experiment();
        experimentOne.setName("One");
        genoxOne.setFish(fish);
        genoxOne.setExperiment(experimentOne);
        ExperimentCondition conditionTwo = new ExperimentCondition();
        SequenceTargetingReagent sequenceTargetingReagentTwo = new SequenceTargetingReagent();
        sequenceTargetingReagentTwo.setAbbreviation("MO-slit1b");
        sequenceTargetingReagentTwo.setZdbID("ZDB-MRPHlNO-090311-1");
        //conditionTwo.setSequenceTargetingReagent(sequenceTargetingReagentTwo);

        ExperimentCondition conditionOne = new ExperimentCondition();
        SequenceTargetingReagent sequenceTargetingReagentOne = new SequenceTargetingReagent();
        sequenceTargetingReagentOne.setAbbreviation("MO-slit1a");
        sequenceTargetingReagentOne.setZdbID("ZDB-MRPHlNO-090311-2");
        List<SequenceTargetingReagent> strs = new ArrayList<>(2);
        strs.add(sequenceTargetingReagentTwo);
        strs.add(sequenceTargetingReagentOne);
        fish.setStrList(strs);
        //conditionOne.setSequenceTargetingReagent(sequenceTargetingReagentOne);
        //conditionTwo.setSequenceTargetingReagent(sequenceTargetingReagentTwo);

        Set<ExperimentCondition> conditions = new HashSet<ExperimentCondition>();
        conditions.add(conditionOne);
        conditions.add(conditionTwo);
        experimentOne.setExperimentConditions(conditions);

        // create markers for each MO
        //createMarkerForMo("slit1a", sequenceTargetingReagentOne);
        //createMarkerForMo("slit1b", sequenceTargetingReagentTwo);

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
        item = new GenericTerm();
        item.setTermName("eye");
        item.setZdbID("ZDB-ANAT-010921-532");
    }
}