package org.zfin.gwt.root.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit test class
 */
@SuppressWarnings({"FeatureEnvy"})
public class ExperimentDTOTest {

    /**
     * null gene and alcam
     */
    @Test
    public void sortExperimentDTOsGenes() {
        ExpressionExperimentDTO one = new ExpressionExperimentDTO();
        one.setGene(getMarkerDTO("alcam"));
        ExpressionExperimentDTO two = new ExpressionExperimentDTO();
        List<ExpressionExperimentDTO> experiments = new ArrayList<ExpressionExperimentDTO>(2);
        experiments.add(one);
        experiments.add(two);

        Collections.sort(experiments);
        assertNull(experiments.get(0).getGene());
        assertEquals("alcam", experiments.get(1).getGene().getName());

    }

    /**
     * same gene, different fish
     */
    @Test
    public void sortExperimentDTOsFish() {
        ExpressionExperimentDTO one = new ExpressionExperimentDTO();
        one.setGene(getMarkerDTO("alcam"));
        one.setFishName("AB");
        ExpressionExperimentDTO two = new ExpressionExperimentDTO();
        two.setGene(null);
        two.setFishName("WT");
        List<ExpressionExperimentDTO> experiments = new ArrayList<ExpressionExperimentDTO>(2);
        experiments.add(one);
        experiments.add(two);
        Collections.sort(experiments);
        assertNull(experiments.get(0).getGene());
        assertEquals("alcam", experiments.get(1).getGene().getName());

        two.setGene(getMarkerDTO("alcam"));
        Collections.sort(experiments);
        assertEquals("AB", experiments.get(0).getFishName());
        assertEquals("WT", experiments.get(1).getFishName());
    }

    /**
     * same gene and fish different environment
     */
    @Test
    public void sortExperimentDTOsEnv() {
        ExpressionExperimentDTO one = new ExpressionExperimentDTO();
        one.setGene(getMarkerDTO("alcam"));
        one.setFishName("AB");
        ExperimentDTO envDto = new ExperimentDTO();
        envDto.setName(ExperimentDTO.STANDARD);
        one.setEnvironment(envDto);
        ExpressionExperimentDTO two = new ExpressionExperimentDTO();
        two.setGene(null);
        two.setFishName("AB");
        ExperimentDTO envDtoTwo = new ExperimentDTO();
        envDtoTwo.setName(ExperimentDTO.GENERIC_CONTROL);
        two.setEnvironment(envDtoTwo);
        List<ExpressionExperimentDTO> experiments = new ArrayList<ExpressionExperimentDTO>(2);
        experiments.add(one);
        experiments.add(two);
        Collections.sort(experiments);
        assertEquals(ExperimentDTO.GENERIC_CONTROL, experiments.get(0).getEnvironment().getName());
        assertEquals(ExperimentDTO.STANDARD, experiments.get(1).getEnvironment().getName());

    }

    /**
     * same gene and fish, environment different assay
     */
    @Test
    public void sortExperimentDTOsAssay() {
        ExpressionExperimentDTO one = new ExpressionExperimentDTO();
        one.setGene(getMarkerDTO("alcam"));
        one.setFishName("AB");
        ExperimentDTO envDto = new ExperimentDTO();
        envDto.setName(ExperimentDTO.STANDARD);
        one.setEnvironment(envDto);
        one.setAssay("Immunohisto");
        ExpressionExperimentDTO two = new ExpressionExperimentDTO();
        two.setGene(getMarkerDTO("alcam"));
        two.setFishName("AB");
        ExperimentDTO envDtoTwo = new ExperimentDTO();
        envDtoTwo.setName(ExperimentDTO.STANDARD);
        two.setEnvironment(envDtoTwo);
        two.setAssay("other");
        List<ExpressionExperimentDTO> experiments = new ArrayList<ExpressionExperimentDTO>(2);
        experiments.add(one);
        experiments.add(two);
        Collections.sort(experiments);
        assertEquals("Immunohisto", experiments.get(0).getAssay());
        assertEquals("other", experiments.get(1).getAssay());

    }

    private MarkerDTO getMarkerDTO(String name) {

        MarkerDTO markerDTO = new MarkerDTO();
        markerDTO.setName(name);
        return markerDTO;
    }

}
