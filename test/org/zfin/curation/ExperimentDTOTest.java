package org.zfin.curation;

import org.junit.Test;
import org.zfin.framework.presentation.dto.ExperimentDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Unit test class
 */
public class ExperimentDTOTest {

    /**
     * null gene and alcam
     */
    @Test
    public void  sortExperimentDTOsGenes(){
        ExperimentDTO one = new ExperimentDTO();
        one.setGeneName("alcam");
        ExperimentDTO two = new ExperimentDTO();
        List<ExperimentDTO> experiments = new ArrayList<ExperimentDTO>();
        experiments.add(one);
        experiments.add(two);

        Collections.sort(experiments);
        assertEquals(null, experiments.get(0).getGeneName());
        assertEquals("alcam", experiments.get(1).getGeneName());

    }

    /**
     * same gene, different fish
     */
    @Test
    public void  sortExperimentDTOsFish(){
        ExperimentDTO one = new ExperimentDTO();
        one.setGeneName("alcam");
        one.setFishName("AB");
        ExperimentDTO two = new ExperimentDTO();
        two.setGeneName(null);
        two.setFishName("WT");
        List<ExperimentDTO> experiments = new ArrayList<ExperimentDTO>();
        experiments.add(one);
        experiments.add(two);
        Collections.sort(experiments);
        assertEquals(null, experiments.get(0).getGeneName());
        assertEquals("alcam", experiments.get(1).getGeneName());

        two.setGeneName("alcam");
        Collections.sort(experiments);
        assertEquals("AB", experiments.get(0).getFishName());
        assertEquals("WT", experiments.get(1).getFishName());
    }

    /**
     * same gene and fish different environment
     */
    @Test
    public void  sortExperimentDTOsEnv(){
        ExperimentDTO one = new ExperimentDTO();
        one.setGeneName("alcam");
        one.setFishName("AB");
        one.setEnvironment("Standard");
        ExperimentDTO two = new ExperimentDTO();
        two.setGeneName(null);
        two.setFishName("AB");
        two.setEnvironment("Generic-control");
        List<ExperimentDTO> experiments = new ArrayList<ExperimentDTO>();
        experiments.add(one);
        experiments.add(two);
        Collections.sort(experiments);
        assertEquals("Generic-control", experiments.get(0).getEnvironment());
        assertEquals("Standard", experiments.get(1).getEnvironment());

    }

    /**
     * same gene and fish, environment different assay
     */
    @Test
    public void  sortExperimentDTOsAssay(){
        ExperimentDTO one = new ExperimentDTO();
        one.setGeneName("alcam");
        one.setFishName("AB");
        one.setEnvironment("Standard");
        one.setAssay("Immunohisto");
        ExperimentDTO two = new ExperimentDTO();
        two.setGeneName("alcam");
        two.setFishName("AB");
        two.setEnvironment("Standard");
        two.setAssay("other");
        List<ExperimentDTO> experiments = new ArrayList<ExperimentDTO>();
        experiments.add(one);
        experiments.add(two);
        Collections.sort(experiments);
        assertEquals("Immunohisto", experiments.get(0).getAssay());
        assertEquals("other", experiments.get(1).getAssay());

    }
}
