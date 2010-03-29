package org.zfin.gwt.root.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
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
        ExperimentDTO one = new ExperimentDTO();
        one.setGene(getMarkerDTO("alcam"));
        ExperimentDTO two = new ExperimentDTO();
        List<ExperimentDTO> experiments = new ArrayList<ExperimentDTO>(2);
        experiments.add(one);
        experiments.add(two);

        Collections.sort(experiments);
        assertNull(experiments.get(0).getGene());
        assertEquals("alcam", experiments.get(1).getGene().getAbbreviation());

    }

    /**
     * same gene, different fish
     */
    @Test
    public void sortExperimentDTOsFish() {
        ExperimentDTO one = new ExperimentDTO();
        one.setGene(getMarkerDTO("alcam"));
        one.setFishName("AB");
        ExperimentDTO two = new ExperimentDTO();
        two.setGene(null);
        two.setFishName("WT");
        List<ExperimentDTO> experiments = new ArrayList<ExperimentDTO>(2);
        experiments.add(one);
        experiments.add(two);
        Collections.sort(experiments);
        assertNull(experiments.get(0).getGene());
        assertEquals("alcam", experiments.get(1).getGene().getAbbreviation());

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
        ExperimentDTO one = new ExperimentDTO();
        one.setGene(getMarkerDTO("alcam"));
        one.setFishName("AB");
        EnvironmentDTO envDto = new EnvironmentDTO();
        envDto.setName(EnvironmentDTO.STANDARD);
        one.setEnvironment(envDto);
        ExperimentDTO two = new ExperimentDTO();
        two.setGene(null);
        two.setFishName("AB");
        EnvironmentDTO envDtoTwo = new EnvironmentDTO();
        envDtoTwo.setName(EnvironmentDTO.GENERIC_CONTROL);
        two.setEnvironment(envDtoTwo);
        List<ExperimentDTO> experiments = new ArrayList<ExperimentDTO>(2);
        experiments.add(one);
        experiments.add(two);
        Collections.sort(experiments);
        assertEquals(EnvironmentDTO.GENERIC_CONTROL, experiments.get(0).getEnvironment().getName());
        assertEquals(EnvironmentDTO.STANDARD, experiments.get(1).getEnvironment().getName());

    }

    /**
     * same gene and fish, environment different assay
     */
    @Test
    public void sortExperimentDTOsAssay() {
        ExperimentDTO one = new ExperimentDTO();
        one.setGene(getMarkerDTO("alcam"));
        one.setFishName("AB");
        EnvironmentDTO envDto = new EnvironmentDTO();
        envDto.setName(EnvironmentDTO.STANDARD);
        one.setEnvironment(envDto);
        one.setAssay("Immunohisto");
        ExperimentDTO two = new ExperimentDTO();
        two.setGene(getMarkerDTO("alcam"));
        two.setFishName("AB");
        EnvironmentDTO envDtoTwo = new EnvironmentDTO();
        envDtoTwo.setName(EnvironmentDTO.STANDARD);
        two.setEnvironment(envDtoTwo);
        two.setAssay("other");
        List<ExperimentDTO> experiments = new ArrayList<ExperimentDTO>(2);
        experiments.add(one);
        experiments.add(two);
        Collections.sort(experiments);
        assertEquals("Immunohisto", experiments.get(0).getAssay());
        assertEquals("other", experiments.get(1).getAssay());

    }

    private MarkerDTO getMarkerDTO(String name) {

        MarkerDTO markerDTO = new MarkerDTO();
        markerDTO.setAbbreviation(name);
        return markerDTO;
    }

}
